package moze_intel.projecte.gameObjs;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalCauldronBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalCrucibleBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.HomunculusFlaskBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.PulseStakeBlockEntity;
import moze_intel.projecte.gameObjs.customRecipes.athanor.OpusStage;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronLiquid;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.ConfigSync;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A playtest of the alchemy of ProjectE Phoenix on a running server: a logged in survival player, real blocks, real ticks.
 * Every step is checked, and every failure collected, so one broken step does not hide the rest. The steps print what they
 * find, prefixed with PLAYTEST, for reading the run afterwards.
 */
@ExtendWith(EphemeralTestServerProvider.class)
class PlaytestTest {

	private final List<String> failures = new ArrayList<>();
	private MinecraftServer server;
	private ServerLevel level;
	private ServerPlayer player;

	private <T> T onServer(Callable<T> task) {
		return server.submit(() -> {
			try {
				return task.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).orTimeout(60, java.util.concurrent.TimeUnit.SECONDS).join();
	}

	private void run(Runnable task) {
		onServer(() -> {
			task.run();
			return null;
		});
	}

	/**
	 * Lets the game run for that many ticks, as fast as it can.
	 */
	private void ticks(int ticks) {
		long start = onServer(() -> level.getGameTime());
		run(() -> server.tickRateManager().requestGameToSprint(ticks));
		long deadline = System.currentTimeMillis() + 120_000;
		while (onServer(() -> server.tickRateManager().isSprinting())) {
			if (System.currentTimeMillis() > deadline) {
				throw new IllegalStateException("The server did not finish sprinting " + ticks + " ticks");
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		System.out.println("PLAYTEST: time  asked " + ticks + " ticks, the overworld ran " + (onServer(() -> level.getGameTime()) - start));
	}

	private void check(String step, boolean ok, Supplier<String> detail) {
		String line = "PLAYTEST: " + (ok ? "ok   " : "FAIL ") + step + (ok ? "" : " (" + detail.get() + ")");
		System.out.println(line);
		if (!ok) {
			failures.add(line);
		}
	}

	private void place(BlockPos pos, Block block) {
		level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
	}

	@SuppressWarnings("unchecked")
	private <T extends BlockEntity> T blockEntity(BlockPos pos) {
		return (T) level.getBlockEntity(pos);
	}

	/**
	 * Holds the stack in the main hand and uses it on the block entity, the way a right click does.
	 */
	private void use(ItemStack stack, Runnable use) {
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		use.run();
	}

	private int countItems(BlockPos around, Item item) {
		int count = 0;
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(around).inflate(4))) {
			if (entity.getItem().is(item)) {
				count += entity.getItem().getCount();
			}
		}
		return count;
	}

	private void clearItems(BlockPos around) {
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(around).inflate(6))) {
			entity.discard();
		}
	}

	private BigInteger emc() {
		IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		return provider == null ? BigInteger.ZERO : provider.getEmc();
	}

	private void setEmc(BigInteger emc) {
		IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (provider != null) {
			provider.setEmc(emc);
		}
	}

	/**
	 * The test server has no services, but a player joining looks itself up in the name cache. A cache in memory is enough.
	 */
	private void giveNameCache() throws ReflectiveOperationException {
		Services services = server.services();
		if (services.nameToIdCache() != null) {
			return;
		}
		Map<UUID, NameAndId> known = new HashMap<>();
		UserNameToIdResolver cache = new UserNameToIdResolver() {
			@Override
			public void add(NameAndId nameAndId) {
				known.put(nameAndId.id(), nameAndId);
			}

			@Override
			public Optional<NameAndId> get(String name) {
				return known.values().stream().filter(nameAndId -> nameAndId.name().equals(name)).findFirst();
			}

			@Override
			public Optional<NameAndId> get(UUID uuid) {
				return Optional.ofNullable(known.get(uuid));
			}

			@Override
			public void resolveOfflineUsers(boolean offline) {
			}

			@Override
			public void save() {
			}
		};
		Field field = MinecraftServer.class.getDeclaredField("services");
		field.setAccessible(true);
		field.set(server, new Services(services.sessionService(), services.servicesKeySet(), services.profileRepository(), cache, services.profileResolver()));
	}

	@SuppressWarnings("unchecked")
	private void spawnPlayer() throws ReflectiveOperationException {
		giveNameCache();
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "playtester"), false);
		player = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		NetworkRegistry.configureMockConnection(connection);
		//A real client gets the configs while it logs in; this one skipped that, so there is nothing left to send it
		Field configsToSync = ConfigSync.class.getDeclaredField("configsToSync");
		configsToSync.setAccessible(true);
		((Map<Connection, Map<String, byte[]>>) configsToSync.get(null)).put(connection, new HashMap<>());
		server.getPlayerList().placeNewPlayer(connection, player, cookie);
		player.setGameMode(GameType.SURVIVAL);
		//The server ticks a player through its connection, and this connection is not one the server knows about
		ServerPlayer tester = player;
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
			if (!tester.isRemoved()) {
				tester.doTick();
			}
		});
	}

	/**
	 * A floor of stone the size of a cauldron with all its circles, high in the air under open sky.
	 */
	private void floor(BlockPos centre, int radius) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				place(centre.offset(dx, -1, dz), Blocks.STONE);
			}
		}
	}

	private void forceChunks(BlockPos origin, int radius) {
		for (int cx = (origin.getX() - radius) >> 4; cx <= (origin.getX() + radius) >> 4; cx++) {
			for (int cz = (origin.getZ() - radius) >> 4; cz <= (origin.getZ() + radius) >> 4; cz++) {
				level.setChunkForced(cx, cz, true);
				level.getChunk(cx, cz);
			}
		}
	}

	private void kill(EntityType<? extends LivingEntity> type, Vec3 at) {
		LivingEntity entity = type.create(level, EntitySpawnReason.COMMAND);
		if (entity == null) {
			return;
		}
		entity.snapTo(at);
		level.addFreshEntity(entity);
		entity.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
	}

	@Test
	@DisplayName("Playtest: the alchemy from the first cauldron to the Phoenix")
	void playtest(MinecraftServer testServer) {
		server = testServer;
		run(() -> {
			if (server.overworld() == null) {
				//The test server starts without worlds; make them the way a normal server start does
				try {
					java.lang.reflect.Method createLevels = MinecraftServer.class.getDeclaredMethod("createLevels");
					createLevels.setAccessible(true);
					createLevels.invoke(server);
					//The server keeps a copy of its list of worlds to tick, made while there were none
					server.markWorldsDirty();
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
			level = server.overworld();
			forceChunks(new BlockPos(0, 200, 15), 48);
			try {
				spawnPlayer();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
			player.snapTo(new Vec3(0.5, 201, 0.5));
			setEmc(BigInteger.valueOf(10_000_000_000L));
			//Time only passes when a step asks for it, so a step knows exactly how far the world has come
			server.tickRateManager().setFrozen(true);
		});
		cauldron();
		athanor();
		grandAthanorAndGate();
		pulseArray();
		bossDrop();
		chimera();
		homunculus();
		phoenix();
		Assertions.assertTrue(failures.isEmpty(), () -> "Playtest failures:\n" + String.join("\n", failures));
	}

	private void cauldron() {
		BlockPos cauldron = new BlockPos(20, 200, 0);
		run(() -> {
			floor(cauldron, 4);
			place(cauldron.below(), Blocks.MAGMA_BLOCK);
			place(cauldron, PEBlocks.ALCHEMICAL_CAULDRON.getBlock());
		});
		AlchemicalCauldronBlockEntity pot = onServer(() -> blockEntity(cauldron));
		run(() -> pot.setOwner(player.getUUID()));
		run(() -> use(new ItemStack(Items.WATER_BUCKET), () -> pot.use(player, InteractionHand.MAIN_HAND, player.getMainHandItem())));
		check("a water bucket fills the cauldron", onServer(pot::getLiquid) == CauldronLiquid.WATER, () -> "liquid " + pot.getLiquid());
		check("the bucket comes back empty", onServer(() -> player.getMainHandItem().is(Items.BUCKET)), () -> "hand " + player.getMainHandItem());
		//Salt: low heat, no circles
		run(() -> use(new ItemStack(PEItems.LOW_COVALENCE_DUST.get(), 8), () -> pot.use(player, InteractionHand.MAIN_HAND, player.getMainHandItem())));
		ticks(140);
		check("8 Low Covalence Dust on low heat brew Alchemical Salt", onServer(() -> countItems(cauldron, PEItems.ALCHEMICAL_SALT.get())) == 1,
				() -> "salt " + countItems(cauldron, PEItems.ALCHEMICAL_SALT.get()) + ", contents " + pot.getContents());
		//The circles
		for (Item chalk : List.of(PEItems.SALT_CHALK.get(), PEItems.SULPHUR_CHALK.get(), PEItems.MERCURY_CHALK.get())) {
			run(() -> use(new ItemStack(chalk), () -> pot.use(player, InteractionHand.MAIN_HAND, player.getMainHandItem())));
		}
		check("the three chalks draw three circles", onServer(pot::getCircleCount) == 3, () -> "circles " + pot.getCircleCount());
		check("drawing wears the chalk", onServer(() -> player.getMainHandItem().getDamageValue()) > 0, () -> "damage " + player.getMainHandItem().getDamageValue());
		//Sulphur and quicksilver on high heat
		run(() -> {
			clearItems(cauldron);
			level.setBlock(cauldron.below(), Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
		});
		run(() -> use(new ItemStack(PEItems.MEDIUM_COVALENCE_DUST.get(), 8), () -> pot.use(player, InteractionHand.MAIN_HAND, player.getMainHandItem())));
		ticks(180);
		check("8 Medium Covalence Dust on high heat in a circle brew Sulphur", onServer(() -> countItems(cauldron, PEItems.ALCHEMICAL_SULPHUR.get())) == 1,
				() -> "heat " + pot.getHeat() + ", contents " + pot.getContents());
		run(() -> use(new ItemStack(PEItems.HIGH_COVALENCE_DUST.get(), 8), () -> pot.use(player, InteractionHand.MAIN_HAND, player.getMainHandItem())));
		ticks(220);
		check("8 High Covalence Dust brew Quicksilver", onServer(() -> countItems(cauldron, PEItems.QUICKSILVER.get())) == 1, () -> "contents " + pot.getContents());
		//Souls: pigs die inside the outer circle
		run(() -> {
			for (int i = 0; i < 24; i++) {
				kill(EntityType.PIG, Vec3.atBottomCenterOf(cauldron.offset(2, 0, (i % 5) - 2)));
			}
		});
		check("24 pigs dying in the circle leave 24 souls", onServer(pot::getSouls) == 24, () -> "souls " + pot.getSouls());
		//Red Water on soul heat
		run(() -> {
			clearItems(cauldron);
			level.setBlock(cauldron.below(), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL);
		});
		for (ItemStack stack : List.of(new ItemStack(Items.REDSTONE, 4), new ItemStack(Items.NETHER_WART, 2), new ItemStack(Items.GLOWSTONE_DUST, 4))) {
			run(() -> use(stack, () -> pot.use(player, InteractionHand.MAIN_HAND, player.getMainHandItem())));
		}
		ticks(240);
		check("redstone, nether wart and glowstone on soul heat with souls brew Red Water", onServer(pot::getLiquid) == CauldronLiquid.RED_WATER,
				() -> "liquid " + pot.getLiquid() + ", heat " + pot.getHeat() + ", souls " + pot.getSouls());
		check("Red Water used 4 souls", onServer(pot::getSouls) == 20, () -> "souls " + pot.getSouls());
		//The False Philosopher's Stone, three of them for the Athanor
		run(() -> level.setBlock(cauldron.below(), Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL));
		run(() -> use(new ItemStack(Items.DIAMOND), () -> pot.use(player, InteractionHand.MAIN_HAND, player.getMainHandItem())));
		ticks(340);
		check("a diamond in Red Water on high heat becomes a False Philosopher's Stone", onServer(() -> countItems(cauldron, PEItems.FALSE_PHILOSOPHERS_STONE.get())) == 1,
				() -> "liquid " + pot.getLiquid() + ", souls " + pot.getSouls() + ", contents " + pot.getContents());
		check("the stone uses up the Red Water", onServer(pot::getLiquid) == CauldronLiquid.EMPTY, () -> "liquid " + pot.getLiquid());
		run(() -> clearItems(cauldron));
	}

	private void buildAthanor(BlockPos centre) {
		for (CrucibleStructure.Part part : CrucibleStructure.getParts()) {
			place(centre.offset(part.offset()), part.block().getBlock());
		}
	}

	private void athanor() {
		BlockPos centre = new BlockPos(0, 200, 30);
		run(() -> buildAthanor(centre));
		ticks(40);
		AlchemicalCrucibleBlockEntity crucible = onServer(() -> blockEntity(centre));
		check("the built Athanor is formed", onServer(crucible::isFormed), () -> "missing " + CrucibleStructure.findMissing(level, centre));
		check("it is a tier 1 Athanor", onServer(crucible::getTier) == CrucibleStructure.ATHANOR, () -> "tier " + crucible.getTier());
		BigInteger before = onServer(this::emc);
		for (int i = 0; i < 3; i++) {
			run(() -> use(new ItemStack(PEItems.FALSE_PHILOSOPHERS_STONE.get()), () -> crucible.use(player, player.getMainHandItem())));
		}
		run(() -> use(new ItemStack(Items.NETHER_STAR), () -> crucible.use(player, player.getMainHandItem())));
		check("the Nether Star goes to the centre", onServer(() -> crucible.getCatalystStack().is(Items.NETHER_STAR)), () -> "centre " + crucible.getCatalystStack());
		run(() -> use(ItemStack.EMPTY, () -> crucible.use(player, ItemStack.EMPTY)));
		check("the ritual starts in Nigredo", onServer(crucible::getStage) == OpusStage.NIGREDO, () -> "stage " + crucible.getStage());
		ticks(1600);
		check("after the four stages the Philosopher's Stone is made", onServer(() -> crucible.getResult().is(PEItems.PHILOSOPHERS_STONE.get())),
				() -> "stage " + crucible.getStage() + ", result " + crucible.getResult() + ", status " + crucible.getStatus().getString());
		BigInteger paid = before.subtract(onServer(this::emc));
		check("the ritual took the owner's EMC", paid.signum() > 0 && paid.compareTo(BigInteger.valueOf(245_760)) <= 0, () -> "paid " + paid);
		run(() -> use(ItemStack.EMPTY, () -> crucible.use(player, ItemStack.EMPTY)));
		check("the stone can be taken out", onServer(() -> player.getInventory().contains(new ItemStack(PEItems.PHILOSOPHERS_STONE.get()))), () -> "not in inventory");
		//A rebound: the Athanor breaks during Nigredo and is not repaired
		for (int i = 0; i < 3; i++) {
			run(() -> use(new ItemStack(PEItems.FALSE_PHILOSOPHERS_STONE.get()), () -> crucible.use(player, player.getMainHandItem())));
		}
		run(() -> use(new ItemStack(Items.NETHER_STAR), () -> crucible.use(player, player.getMainHandItem())));
		run(() -> use(ItemStack.EMPTY, () -> crucible.use(player, ItemStack.EMPTY)));
		check("a second ritual starts", onServer(() -> crucible.getStage().isActive()),
				() -> "stage " + crucible.getStage() + ", inputs " + crucible.getInput(0) + ", status " + crucible.getStatus().getString());
		ticks(40);
		run(() -> level.removeBlock(centre.offset(2, 1, 2), false));
		ticks(200);
		check("a broken Athanor in Nigredo rebounds and burns the catalyst", onServer(() -> crucible.getStage() == OpusStage.IDLE && crucible.getCatalystStack().isEmpty()),
				() -> "stage " + crucible.getStage() + ", centre " + crucible.getCatalystStack());
		check("the rebound in Nigredo spares the ingredients", onServer(() -> !crucible.getInput(0).isEmpty()), () -> "inputs gone");
		run(() -> place(centre.offset(2, 1, 2), PEBlocks.CRUCIBLE_PILLAR.getBlock()));
		//Take the stones back for later
		for (int i = 0; i < 3; i++) {
			run(() -> use(ItemStack.EMPTY, () -> {
				player.setShiftKeyDown(true);
				crucible.use(player, ItemStack.EMPTY);
				player.setShiftKeyDown(false);
			}));
		}
	}

	private void grandAthanorAndGate() {
		BlockPos centre = new BlockPos(0, 200, 30);
		run(() -> {
			for (CrucibleStructure.Part part : CrucibleStructure.getParts()) {
				if (part.grand() != null) {
					place(centre.offset(part.offset()), part.grand().getBlock());
				}
			}
		});
		ticks(40);
		AlchemicalCrucibleBlockEntity crucible = onServer(() -> blockEntity(centre));
		check("gilding makes the Grand Athanor", onServer(crucible::getTier) == CrucibleStructure.GRAND_ATHANOR, () -> "tier " + crucible.getTier());
		check("the Grand Athanor holds five", onServer(crucible::getCapacity) == 5, () -> "capacity " + crucible.getCapacity());
		float healthBefore = onServer(player::getMaxHealth);
		for (Item input : List.of(Items.BONE_BLOCK, Items.COAL_BLOCK, Items.ROTTEN_FLESH, PEItems.ALCHEMICAL_SALT.get(), Items.REDSTONE_BLOCK)) {
			run(() -> use(new ItemStack(input), () -> crucible.use(player, player.getMainHandItem())));
		}
		run(() -> use(new ItemStack(PEItems.FILLED_SOUL_VIAL.get()), () -> crucible.use(player, player.getMainHandItem())));
		run(() -> use(ItemStack.EMPTY, () -> crucible.use(player, ItemStack.EMPTY)));
		check("the forbidden work starts", onServer(() -> crucible.getStage().isActive()), () -> "stage " + crucible.getStage() + ", inputs " + crucible.getInput(0));
		ticks(1000);
		check("the Gate takes two hearts", onServer(player::getMaxHealth) == healthBefore - 4, () -> "max health " + player.getMaxHealth());
		check("the player has seen the Truth", onServer(() -> player.getData(PEAttachmentTypes.TRUTH).seen()), () -> "not seen");
		check("a Malformed Homunculus is left", onServer(() -> countItems(centre, PEItems.MALFORMED_HOMUNCULUS.get())) == 1, () -> "none");
		//The clap: look at stone with empty hands
		BlockPos stone = new BlockPos(0, 201, 3);
		run(() -> {
			clearItems(centre);
			place(stone, Blocks.STONE);
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			player.snapTo(new Vec3(0.5, 200, 0.5), 0, 0);
			player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(stone));
		});
		boolean clapped = onServer(() -> TheGate.clap(player));
		check("clapping transmutes the stone it looks at", clapped && onServer(() -> level.getBlockState(stone).is(Blocks.COBBLESTONE)),
				() -> "clapped " + clapped + ", block " + level.getBlockState(stone));
	}

	private void pulseArray() {
		BlockPos centre = new BlockPos(-30, 200, 0);
		run(() -> {
			floor(centre, 6);
			place(centre, PEBlocks.PULSE_STAKE.getBlock());
			for (BlockPos offset : List.of(new BlockPos(5, 0, 0), new BlockPos(-5, 0, 0), new BlockPos(0, 0, 5), new BlockPos(0, 0, -5))) {
				place(centre.offset(offset), PEBlocks.PULSE_STAKE.getBlock());
			}
		});
		PulseStakeBlockEntity stake = onServer(() -> blockEntity(centre));
		run(() -> use(new ItemStack(PEItems.SOUL_STONE.get()), () -> stake.use(player, player.getMainHandItem())));
		ticks(60);
		check("five stakes and a focus form an array", onServer(stake::isFormed), () -> "not formed, focus " + stake.getFocus());
		int strength = onServer(() -> DragonsPulse.strength(level, centre));
		System.out.println("PLAYTEST: info  the Dragon's Pulse under the array is " + strength);
	}

	private void bossDrop() {
		BlockPos at = new BlockPos(30, 201, 30);
		run(() -> floor(at, 3));
		run(() -> kill(EntityType.WARDEN, Vec3.atBottomCenterOf(at)));
		ticks(5);
		check("a Warden drops its heart", onServer(() -> countItems(at, PEItems.WARDEN_HEART.get())) == 1, () -> "no heart");
	}

	private void chimera() {
		run(() -> player.getInventory().add(new ItemStack(PEItems.CHIMERA_OWL.get())));
		ticks(100);
		check("a Chimera of the Owl in the inventory gives night vision", onServer(() -> player.hasEffect(MobEffects.NIGHT_VISION)), () -> "no effect");
	}

	private void homunculus() {
		BlockPos flask = new BlockPos(-30, 201, 30);
		run(() -> {
			floor(flask, 5);
			place(flask.below(), Blocks.CHEST);
			place(flask, PEBlocks.HOMUNCULUS_FLASK.getBlock());
			this.<HomunculusFlaskBlockEntity>blockEntity(flask).setOwner(player.getUUID());
			for (int i = 0; i < 3; i++) {
				ItemEntity item = new ItemEntity(level, flask.getX() + 3, flask.getY(), flask.getZ() + i, new ItemStack(Items.COBBLESTONE, 4));
				item.setNoPickUpDelay();
				level.addFreshEntity(item);
			}
		});
		ticks(1300);
		int gathered = onServer(() -> {
			Container chest = (Container) level.getBlockEntity(flask.below());
			return chest == null ? -1 : chest.countItem(Items.COBBLESTONE);
		});
		check("a fed homunculus gathers items into the chest below", gathered == 12, () -> "cobblestone in chest " + gathered);
	}

	private void phoenix() {
		run(() -> Phoenix.rise(player));
		check("Phoenix Ash makes a Phoenix", onServer(() -> Phoenix.isPhoenix(player)), () -> "not a phoenix");
		check("the Phoenix gets back the hearts the Gate took", onServer(player::getMaxHealth) == 20, () -> "max health " + player.getMaxHealth());
		run(() -> player.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE));
		check("a Phoenix rises instead of dying", onServer(() -> player.isAlive() && player.getHealth() == player.getMaxHealth()),
				() -> "alive " + player.isAlive() + ", health " + player.getHealth());
		run(() -> {
			player.invulnerableTime = 0;
			player.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
		});
		check("the fire needs time to return", onServer(() -> !player.isAlive() || player.isDeadOrDying()), () -> "rose twice");
	}
}
