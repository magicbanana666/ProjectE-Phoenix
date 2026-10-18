package moze_intel.projecte.gameObjs.block_entities;

import java.math.BigInteger;
import java.util.Locale;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.config.ServerConfig;
import moze_intel.projecte.config.value.CachedIntValue;
import moze_intel.projecte.gameObjs.CrucibleStructure;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The centre of the Alchemical Crucible, where three False Philosopher's Stones and a catalyst pass through the four
 * stages of the Magnum Opus and become the real Philosopher's Stone. The ritual only runs while the whole crucible, see
 * {@link CrucibleStructure}, is in place.
 * <p>
 * Everything that matters is decided on the server: the ingredients are checked when the ritual starts and again right
 * before the stone is made, the owner pays the EMC of a stage bit by bit while it progresses, and the state is saved so a
 * restart or an unloaded chunk only pauses the ritual. The ingredients are never handed to item handlers, so automation
 * can not take them out halfway.
 */
public class AlchemicalCrucibleBlockEntity extends BlockEntity {

	public static final int STONES = 3;
	private static final DustParticleOptions FALSE_GLOW = new DustParticleOptions(0x5A0A12, 0.7F);
	private static final DustParticleOptions SILVER = new DustParticleOptions(0xE8ECF2, 1.0F);
	private static final DustParticleOptions GOLD = new DustParticleOptions(0xFFC83A, 1.0F);
	private static final DustParticleOptions SCARLET = new DustParticleOptions(0xFF1E3C, 1.2F);
	private static final DustParticleOptions WHITE_HOT = new DustParticleOptions(0xFFF0E0, 1.4F);

	private final ItemStack[] stones = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
	private ItemStack catalyst = ItemStack.EMPTY;
	private ItemStack result = ItemStack.EMPTY;
	private Stage stage = Stage.IDLE;
	private Stall stall = Stall.NONE;
	private int progress;
	/**
	 * The EMC the owner already paid for the current stage.
	 */
	private long paid;
	@Nullable
	private UUID owner;
	/**
	 * Only used on the client, to keep the animation smooth between the updates of the server.
	 */
	private float clientProgress;
	/**
	 * The length of the current stage as the server sent it, the client does not have the server config.
	 */
	private int clientDuration;
	/**
	 * Whether every block of the crucible is in place, checked once a second.
	 */
	private boolean formed;

	public AlchemicalCrucibleBlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.ALCHEMICAL_CRUCIBLE.get(), pos, state);
	}

	public static boolean isCatalyst(ItemStack stack) {
		Identifier id = Identifier.tryParse(ProjectEConfig.server.ritual.catalyst.get());
		Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(id);
		return item != Items.AIR && stack.is(item);
	}

	public static Item getCatalyst() {
		Identifier id = Identifier.tryParse(ProjectEConfig.server.ritual.catalyst.get());
		Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(id);
		return item == Items.AIR ? Items.NETHER_STAR : item;
	}

	private int stoneCount() {
		int count = 0;
		for (ItemStack stone : stones) {
			if (!stone.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	private boolean hasIngredients() {
		for (ItemStack stone : stones) {
			if (!stone.is(PEItems.FALSE_PHILOSOPHERS_STONE)) {
				return false;
			}
		}
		return isCatalyst(catalyst);
	}

	/**
	 * Handles a player using the crucible, on the server.
	 */
	public InteractionResult use(Player player, ItemStack held) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (stage == Stage.COMPLETE) {
			if (!result.isEmpty()) {
				player.getInventory().placeItemBackInInventory(result);
				result = ItemStack.EMPTY;
			}
			reset();
			return InteractionResult.SUCCESS;
		}
		if (stage.isActive()) {
			if (player.isShiftKeyDown() && held.isEmpty() && (player.getUUID().equals(owner) || player.isCreative())) {
				//Stopping keeps the ingredients, only the EMC that was paid is lost
				reset();
				player.sendOverlayMessage(PELang.RITUAL_CANCELLED.translateColored(ChatFormatting.GRAY));
				serverLevel.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
			} else if (player.isShiftKeyDown() && held.isEmpty()) {
				player.sendOverlayMessage(PELang.RITUAL_NOT_OWNER.translateColored(ChatFormatting.RED));
			} else {
				player.sendOverlayMessage(getStatus());
			}
			return InteractionResult.SUCCESS;
		}
		//Idle: put ingredients in, take them out or start the ritual
		if (held.is(PEItems.FALSE_PHILOSOPHERS_STONE)) {
			for (int i = 0; i < STONES; i++) {
				if (stones[i].isEmpty()) {
					stones[i] = held.split(1);
					serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 0.6F);
					changed();
					return InteractionResult.SUCCESS;
				}
			}
			player.sendOverlayMessage(getIngredientMessage());
			return InteractionResult.SUCCESS;
		} else if (catalyst.isEmpty() && isCatalyst(held)) {
			catalyst = held.split(1);
			serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
			changed();
			return InteractionResult.SUCCESS;
		} else if (held.isEmpty()) {
			if (player.isShiftKeyDown()) {
				ItemStack taken = takeLast();
				if (!taken.isEmpty()) {
					player.getInventory().placeItemBackInInventory(taken);
					changed();
				}
			} else if (hasIngredients()) {
				checkStructure();
				if (!formed) {
					player.sendOverlayMessage(getStructureMessage());
					return InteractionResult.SUCCESS;
				}
				owner = player.getUUID();
				setStage(Stage.NIGREDO);
				player.sendOverlayMessage(PELang.RITUAL_STARTED.translateColored(ChatFormatting.DARK_PURPLE));
			} else {
				player.sendOverlayMessage(getIngredientMessage());
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private ItemStack takeLast() {
		if (!catalyst.isEmpty()) {
			ItemStack taken = catalyst;
			catalyst = ItemStack.EMPTY;
			return taken;
		}
		for (int i = STONES - 1; i >= 0; i--) {
			if (!stones[i].isEmpty()) {
				ItemStack taken = stones[i];
				stones[i] = ItemStack.EMPTY;
				return taken;
			}
		}
		return ItemStack.EMPTY;
	}

	private Component getStructureMessage() {
		CrucibleStructure.Part missing = CrucibleStructure.findMissing(level, worldPosition);
		if (missing == null) {
			return Component.empty();
		}
		BlockPos at = worldPosition.offset(missing.offset());
		return PELang.RITUAL_INCOMPLETE.translateColored(ChatFormatting.RED, CrucibleStructure.countMissing(level, worldPosition),
				missing.block().getBlock().getName(), at.getX(), at.getY(), at.getZ());
	}

	private Component getIngredientMessage() {
		return PELang.RITUAL_INGREDIENTS.translateColored(ChatFormatting.GRAY, stoneCount(), STONES, getCatalyst().getDefaultInstance().getHoverName());
	}

	public Component getStatus() {
		Component status = PELang.RITUAL_PROGRESS.translateColored(stage.color, stage.getName(), Mth.floor(100F * progress / Math.max(1, stage.duration())));
		return switch (stall) {
			case NONE -> status;
			case EMC -> status.copy().append(" ").append(PELang.RITUAL_STALLED_EMC.translateColored(ChatFormatting.RED));
			case OFFLINE -> status.copy().append(" ").append(PELang.RITUAL_STALLED_OFFLINE.translateColored(ChatFormatting.RED));
			case SKY -> status.copy().append(" ").append(PELang.RITUAL_STALLED_SKY.translateColored(ChatFormatting.RED));
			case STRUCTURE -> status.copy().append(" ").append(PELang.RITUAL_STALLED_STRUCTURE.translateColored(ChatFormatting.RED));
		};
	}

	private void reset() {
		owner = null;
		progress = 0;
		paid = 0;
		stall = Stall.NONE;
		setStage(Stage.IDLE);
	}

	private void setStage(Stage newStage) {
		stage = newStage;
		progress = 0;
		paid = 0;
		if (level != null) {
			updateGlow();
			if (newStage.startSound != null) {
				level.playSound(null, worldPosition, newStage.startSound, SoundSource.BLOCKS, 1.5F, newStage.pitch);
			}
		}
		changed();
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, AlchemicalCrucibleBlockEntity core) {
		if ((level.getGameTime() + pos.asLong()) % 20 == 0) {
			core.checkStructure();
		}
		if (core.stage.isActive()) {
			core.tickRitual((ServerLevel) level);
		}
	}

	private void checkStructure() {
		if (level == null || level.isClientSide()) {
			return;
		}
		boolean nowFormed = CrucibleStructure.findMissing(level, worldPosition) == null;
		if (nowFormed != formed) {
			formed = nowFormed;
			changed();
		}
		//Also lights up parts that were replaced since the last check
		updateGlow();
	}

	private void updateGlow() {
		if (level != null && !level.isClientSide()) {
			CrucibleStructure.update(level, worldPosition, formed ? stage.ordinal() + 1 : 0);
		}
	}

	private void tickRitual(ServerLevel level) {
		ServerConfig.Ritual settings = ProjectEConfig.server.ritual;
		int duration = stage.duration();
		long cost = stage.cost();
		//What the owner should have paid once this tick is done, so the EMC drains evenly over the whole stage
		long target = (long) ((double) cost * (progress + 1) / duration);
		long due = Math.max(0, target - paid);
		Stall newStall = Stall.NONE;
		ServerPlayer player = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
		IKnowledgeProvider provider = null;
		if (!formed) {
			newStall = Stall.STRUCTURE;
		} else if (player == null) {
			newStall = Stall.OFFLINE;
		} else if (stage == Stage.CITRINITAS && settings.citrinitasNeedsSky.get() && !level.canSeeSky(worldPosition.above())) {
			newStall = Stall.SKY;
		} else if (due > 0) {
			provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (provider == null || provider.getEmc().compareTo(BigInteger.valueOf(due)) < 0) {
				newStall = Stall.EMC;
			}
		}
		if (newStall != stall) {
			stall = newStall;
			if (stall != Stall.NONE) {
				if (stage == Stage.RUBEDO && settings.rubedoResetsWhenStalled.get()) {
					progress = 0;
					paid = 0;
				}
				if (player != null) {
					player.sendOverlayMessage(getStatus());
				}
				level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 0.5F);
			}
			changed();
		}
		if (stall != Stall.NONE) {
			return;
		}
		if (provider != null) {
			provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(due)));
			paid += due;
			if (progress % 10 == 0) {
				provider.syncEmc(player);
			}
		}
		progress++;
		setChanged();
		playAmbience(level);
		if (progress >= duration) {
			if (provider != null) {
				provider.syncEmc(player);
			}
			Stage next = stage.next();
			if (next == Stage.COMPLETE) {
				complete(level, player);
			} else {
				setStage(next);
			}
		} else if (progress % 20 == 0) {
			//Keep the clients roughly in step, they animate the rest on their own
			changed();
		}
	}

	private void complete(ServerLevel level, @Nullable ServerPlayer player) {
		//Check once more before anything is used up, if something is off the ingredients stay where they are
		if (!hasIngredients()) {
			reset();
			return;
		}
		for (int i = 0; i < STONES; i++) {
			stones[i] = ItemStack.EMPTY;
		}
		catalyst = ItemStack.EMPTY;
		result = new ItemStack(PEItems.PHILOSOPHERS_STONE.get());
		owner = null;
		setStage(Stage.COMPLETE);
		double x = worldPosition.getX() + 0.5;
		double y = worldPosition.getY() + 1.2;
		double z = worldPosition.getZ() + 0.5;
		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
		level.sendParticles(WHITE_HOT, x, y, z, 80, 0.6, 0.6, 0.6, 0.2);
		level.sendParticles(SCARLET, x, worldPosition.getY() + 0.2, z, 120, 2.5, 0.05, 2.5, 0);
		level.sendParticles(ParticleTypes.END_ROD, x, y + 2, z, 60, 0.05, 2.5, 0.05, 0.02);
		level.playSound(null, worldPosition, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
		level.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.8F, 1.4F);
		if (player != null) {
			player.sendOverlayMessage(PELang.RITUAL_COMPLETE.translateColored(ChatFormatting.GOLD));
		}
	}

	private void playAmbience(ServerLevel level) {
		if (stage.ambience != null && progress % stage.ambienceInterval == 0) {
			//Rubedo beats faster as it gets closer to the end
			level.playSound(null, worldPosition, stage.ambience, SoundSource.BLOCKS, 0.8F, stage.pitch + 0.4F * progress / stage.duration());
		}
	}

	public static void tickClient(Level level, BlockPos pos, BlockState state, AlchemicalCrucibleBlockEntity core) {
		if (core.stage.isActive() && core.stall == Stall.NONE) {
			core.clientProgress = Math.min(core.clientProgress + 1, core.clientDuration);
		}
		double density = ProjectEConfig.client.ritualParticles.get();
		if (density > 0) {
			core.spawnParticles(level, pos, density);
		}
	}

	private void spawnParticles(Level level, BlockPos pos, double density) {
		RandomSource random = level.getRandom();
		double cx = pos.getX() + 0.5;
		double cy = pos.getY() + 1.5;
		double cz = pos.getZ() + 0.5;
		float fraction = getProgressFraction();
		if (stage.isActive() && stall == Stall.NONE) {
			ParticleOptions stream = switch (stage) {
				case NIGREDO -> ParticleTypes.ASH;
				case ALBEDO -> SILVER;
				case CITRINITAS -> GOLD;
				default -> SCARLET;
			};
			fromPillars(level, random, stream, cx, cy, cz, count(1 + 2 * fraction, density, random));
		}
		switch (stage) {
			case IDLE -> {
				if (stoneCount() > 0 && random.nextFloat() < 0.1F * density) {
					level.addParticle(FALSE_GLOW, cx + random.nextGaussian() * 0.3, cy, cz + random.nextGaussian() * 0.3, 0, 0, 0);
				}
			}
			case NIGREDO -> {
				//Thick smoke, and darkness pulled out of the stones into the star
				for (int i = 0; i < count(3, density, random); i++) {
					level.addParticle(random.nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SQUID_INK,
							cx + random.nextGaussian() * 0.4, pos.getY() + 0.3 + random.nextFloat() * 0.6, cz + random.nextGaussian() * 0.4, 0, 0.02, 0);
				}
				pullIntoCentre(level, random, ParticleTypes.ASH, cx, cy, cz, count(3, density, random));
			}
			case ALBEDO -> {
				for (int i = 0; i < count(3, density, random); i++) {
					level.addParticle(random.nextBoolean() ? SILVER : ParticleTypes.WHITE_ASH,
							cx + random.nextGaussian() * 0.5, pos.getY() + 0.15, cz + random.nextGaussian() * 0.5, 0, 0.06, 0);
				}
				beams(level, random, ParticleTypes.END_ROD, cx, cy, cz, count(1, density, random));
			}
			case CITRINITAS -> {
				beams(level, random, GOLD, cx, cy, cz, count(3, density, random));
				if (random.nextFloat() < 0.3F * density) {
					double angle = random.nextDouble() * Math.PI * 2;
					level.addParticle(ParticleTypes.WAX_ON, cx, cy, cz, Math.cos(angle) * 0.3, 0.02, Math.sin(angle) * 0.3);
				}
			}
			case RUBEDO -> {
				beams(level, random, SCARLET, cx, cy, cz, count(3 + 4 * fraction, density, random));
				if (random.nextFloat() < fraction * density) {
					level.addParticle(ParticleTypes.FLAME, cx + random.nextGaussian() * 0.1, cy, cz + random.nextGaussian() * 0.1, 0, 0.05, 0);
				}
				if (fraction > 0.85F) {
					//The beam that breaks out of the crucible right before the fusion
					for (int i = 0; i < count(4, density, random); i++) {
						level.addParticle(WHITE_HOT, cx, cy + random.nextFloat() * 6, cz, 0, 0.2, 0);
					}
				}
			}
			case COMPLETE -> {
				if (random.nextFloat() < 0.3F * density) {
					level.addParticle(random.nextBoolean() ? SCARLET : GOLD, cx + random.nextGaussian() * 0.2, cy + 0.2, cz + random.nextGaussian() * 0.2, 0, 0.01, 0);
				}
			}
		}
	}

	/**
	 * Particles somewhere on the line from the top of one of the four corner pillars of the crucible to the centre.
	 */
	private static void fromPillars(Level level, RandomSource random, ParticleOptions particle, double cx, double cy, double cz, int count) {
		for (int i = 0; i < count; i++) {
			double angle = Math.PI / 4 + random.nextInt(4) * Math.PI / 2;
			double along = random.nextDouble();
			double x = cx + Math.cos(angle) * 2.83 * (1 - along);
			double z = cz + Math.sin(angle) * 2.83 * (1 - along);
			double y = cy + 1.4 * (1 - along);
			level.addParticle(particle, x, y, z, 0, 0, 0);
		}
	}

	private static int count(float base, double density, RandomSource random) {
		double amount = base * density;
		int whole = (int) amount;
		return whole + (random.nextDouble() < amount - whole ? 1 : 0);
	}

	private static void pullIntoCentre(Level level, RandomSource random, ParticleOptions particle, double cx, double cy, double cz, int count) {
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			level.addParticle(particle, cx + Math.cos(angle) * 0.8, cy + random.nextGaussian() * 0.2, cz + Math.sin(angle) * 0.8, 0, 0, 0);
		}
	}

	/**
	 * Particles that run from one of the three stones to the catalyst in the centre.
	 */
	private void beams(Level level, RandomSource random, ParticleOptions particle, double cx, double cy, double cz, int count) {
		for (int i = 0; i < count; i++) {
			double angle = getStoneAngle(random.nextInt(STONES), level.getGameTime());
			double radius = getOrbitRadius() * random.nextDouble();
			level.addParticle(particle, cx + Math.cos(angle) * radius, cy, cz + Math.sin(angle) * radius, 0, 0, 0);
		}
	}

	/**
	 * @return how far the current stage is, from 0 to 1
	 */
	public float getProgressFraction() {
		boolean client = level != null && level.isClientSide();
		int duration = client ? clientDuration : stage.duration();
		if (duration <= 0) {
			return 0;
		}
		return Mth.clamp((client ? clientProgress : progress) / duration, 0, 1);
	}

	/**
	 * @return the distance of the stones from the catalyst, they spiral inwards during Rubedo
	 */
	public float getOrbitRadius() {
		return stage == Stage.RUBEDO ? Mth.lerp(getProgressFraction(), 0.75F, 0.08F) : 0.75F;
	}

	/**
	 * @return the angle of a stone around the catalyst. The stones form a triangle that starts to turn during Citrinitas.
	 */
	public double getStoneAngle(int stone, double time) {
		double base = stone * Math.PI * 2 / STONES + Math.PI / 2;
		return base + switch (stage) {
			case IDLE, NIGREDO -> time * 0.01;
			case ALBEDO -> time * 0.02;
			case CITRINITAS -> time * (0.05 + 0.1 * getProgressFraction());
			case RUBEDO -> time * (0.15 + 0.5 * getProgressFraction());
			case COMPLETE -> 0;
		};
	}

	public Stage getStage() {
		return stage;
	}

	public boolean isFormed() {
		return formed;
	}

	public boolean isStalled() {
		return stall != Stall.NONE;
	}

	public ItemStack getStone(int index) {
		return stones[index];
	}

	public ItemStack getCatalystStack() {
		return catalyst;
	}

	public ItemStack getResult() {
		return result;
	}

	/**
	 * Drops everything that is in the crucible. Called when it is broken.
	 */
	public void dropContents(Level level, BlockPos pos) {
		for (int i = 0; i < STONES; i++) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stones[i]);
			stones[i] = ItemStack.EMPTY;
		}
		Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, catalyst);
		Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, result);
		catalyst = ItemStack.EMPTY;
		result = ItemStack.EMPTY;
	}

	@Override
	public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
		if (level != null && !level.isClientSide()) {
			dropContents(level, pos);
			//Without its centre the rest of the crucible goes dark
			CrucibleStructure.update(level, pos, 0);
		}
		super.preRemoveSideEffects(pos, state);
	}

	@Override
	protected void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		for (int i = 0; i < STONES; i++) {
			stones[i] = input.read("stone_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY);
		}
		catalyst = input.read("catalyst", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		result = input.read("result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		stage = Stage.byName(input.getStringOr("stage", Stage.IDLE.getSerializedName()));
		stall = Stall.byName(input.getStringOr("stall", Stall.NONE.name()));
		progress = input.getIntOr("progress", 0);
		paid = input.getLongOr("paid", 0);
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
		clientProgress = progress;
		clientDuration = input.getIntOr("duration", 0);
		formed = input.getBooleanOr("formed", false);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		for (int i = 0; i < STONES; i++) {
			if (!stones[i].isEmpty()) {
				output.store("stone_" + i, ItemStack.CODEC, stones[i]);
			}
		}
		if (!catalyst.isEmpty()) {
			output.store("catalyst", ItemStack.CODEC, catalyst);
		}
		if (!result.isEmpty()) {
			output.store("result", ItemStack.CODEC, result);
		}
		output.putString("stage", stage.getSerializedName());
		output.putString("stall", stall.name());
		output.putInt("progress", progress);
		output.putLong("paid", paid);
		output.putInt("duration", stage.duration());
		output.putBoolean("formed", formed);
		if (owner != null) {
			output.store("owner", UUIDUtil.CODEC, owner);
		}
	}

	@NotNull
	@Override
	public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private enum Stall {
		NONE,
		EMC,
		OFFLINE,
		SKY,
		STRUCTURE;

		private static Stall byName(String name) {
			for (Stall stall : values()) {
				if (stall.name().equals(name)) {
					return stall;
				}
			}
			return NONE;
		}
	}

	public enum Stage {
		IDLE(ChatFormatting.GRAY, null, null, 0, 1.0F),
		NIGREDO(ChatFormatting.DARK_PURPLE, SoundEvents.WITHER_SPAWN, SoundEvents.DEEPSLATE_BREAK, 30, 0.5F),
		ALBEDO(ChatFormatting.WHITE, SoundEvents.BEACON_ACTIVATE, SoundEvents.AMETHYST_BLOCK_CHIME, 15, 1.2F),
		CITRINITAS(ChatFormatting.GOLD, SoundEvents.BEACON_POWER_SELECT, SoundEvents.BEACON_AMBIENT, 40, 1.0F),
		RUBEDO(ChatFormatting.RED, SoundEvents.END_PORTAL_SPAWN, SoundEvents.WARDEN_HEARTBEAT, 20, 0.8F),
		COMPLETE(ChatFormatting.GOLD, null, null, 0, 1.0F);

		private final ChatFormatting color;
		@Nullable
		private final SoundEvent startSound;
		@Nullable
		private final SoundEvent ambience;
		private final int ambienceInterval;
		private final float pitch;

		Stage(ChatFormatting color, @Nullable SoundEvent startSound, @Nullable SoundEvent ambience, int ambienceInterval, float pitch) {
			this.color = color;
			this.startSound = startSound;
			this.ambience = ambience;
			this.ambienceInterval = ambienceInterval;
			this.pitch = pitch;
		}

		public boolean isActive() {
			return this == NIGREDO || this == ALBEDO || this == CITRINITAS || this == RUBEDO;
		}

		public Stage next() {
			return values()[Math.min(ordinal() + 1, COMPLETE.ordinal())];
		}

		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}

		public Component getName() {
			return switch (this) {
				case NIGREDO -> PELang.RITUAL_NIGREDO.translate();
				case ALBEDO -> PELang.RITUAL_ALBEDO.translate();
				case CITRINITAS -> PELang.RITUAL_CITRINITAS.translate();
				case RUBEDO -> PELang.RITUAL_RUBEDO.translate();
				default -> PELang.RITUAL_READY.translate();
			};
		}

		public int duration() {
			ServerConfig.Ritual settings = ProjectEConfig.server.ritual;
			CachedIntValue value = switch (this) {
				case NIGREDO -> settings.nigredoDuration;
				case ALBEDO -> settings.albedoDuration;
				case CITRINITAS -> settings.citrinitasDuration;
				case RUBEDO -> settings.rubedoDuration;
				default -> null;
			};
			return value == null ? 0 : value.get();
		}

		public long cost() {
			ServerConfig.Ritual settings = ProjectEConfig.server.ritual;
			CachedIntValue value = switch (this) {
				case NIGREDO -> settings.nigredoEmc;
				case ALBEDO -> settings.albedoEmc;
				case CITRINITAS -> settings.citrinitasEmc;
				case RUBEDO -> settings.rubedoEmc;
				default -> null;
			};
			return value == null ? 0 : value.get();
		}

		/**
		 * @return the variant of the False Philosopher's Stone model that shows the stones in this stage
		 */
		@Nullable
		public String stoneModel() {
			return switch (this) {
				case NIGREDO, ALBEDO, CITRINITAS, RUBEDO -> getSerializedName();
				default -> null;
			};
		}

		public static Stage byName(String name) {
			for (Stage stage : values()) {
				if (stage.getSerializedName().equals(name)) {
					return stage;
				}
			}
			return IDLE;
		}
	}
}
