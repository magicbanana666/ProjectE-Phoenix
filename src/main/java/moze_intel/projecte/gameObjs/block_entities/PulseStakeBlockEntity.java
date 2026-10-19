package moze_intel.projecte.gameObjs.block_entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import moze_intel.projecte.gameObjs.DragonsPulse;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * A Pulse Stake. Five of them close together make an array, and the stake that holds a focus is its centre. The array draws
 * on the Dragon's Pulse under the centre instead of on EMC: the stronger the pulse, the further and more often it works.
 * What it does depends on the focus:
 * <ul>
 *     <li>Harvest Goddess Band: crops grow</li>
 *     <li>Interdiction Torch: hostile mobs and projectiles are pushed out</li>
 *     <li>Soul Stone: players are healed</li>
 *     <li>Alchemical Salt: rain does not wash away the chalk of cauldrons</li>
 * </ul>
 */
public class PulseStakeBlockEntity extends BlockEntity {

	/**
	 * How far apart the stakes of one array may stand.
	 */
	public static final int ARRAY_RANGE = 16;
	private static final int STAKES_NEEDED = 4;
	/**
	 * Every loaded stake by dimension, so that finding the rest of an array does not mean searching the world.
	 */
	private static final Map<ResourceKey<Level>, Set<BlockPos>> LOADED = new HashMap<>();
	private static final Predicate<Entity> HOSTILE = entity -> WorldHelper.validRepelEntity(entity, PETags.Entities.BLACKLIST_INTERDICTION)
															   && (entity instanceof Enemy || entity instanceof Projectile);

	private ItemStack focus = ItemStack.EMPTY;
	private boolean formed;
	private int strength;

	public PulseStakeBlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.PULSE_STAKE.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide()) {
			LOADED.computeIfAbsent(level.dimension(), dimension -> new HashSet<>()).add(worldPosition.immutable());
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level != null && !level.isClientSide()) {
			Set<BlockPos> stakes = LOADED.get(level.dimension());
			if (stakes != null) {
				stakes.remove(worldPosition);
			}
		}
	}

	public ItemStack getFocus() {
		return focus;
	}

	public boolean isFormed() {
		return formed;
	}

	private int radius() {
		return 4 + strength / 2;
	}

	/**
	 * @return whether an array with a salt focus keeps the rain off the given position
	 */
	public static boolean keepsRainOff(ServerLevel level, BlockPos pos) {
		for (BlockPos stake : LOADED.getOrDefault(level.dimension(), Set.of())) {
			if (stake.closerThan(pos, ARRAY_RANGE) && level.getBlockEntity(stake) instanceof PulseStakeBlockEntity array && array.formed
				&& array.focus.is(PEItems.ALCHEMICAL_SALT)) {
				return true;
			}
		}
		return false;
	}

	public InteractionResult use(Player player, ItemStack held) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (held.isEmpty()) {
			if (player.isShiftKeyDown() && !focus.isEmpty()) {
				player.getInventory().placeItemBackInInventory(focus);
				focus = ItemStack.EMPTY;
				changed();
			} else {
				int stakes = countStakes(serverLevel);
				player.sendOverlayMessage(PELang.PULSE_STAKE_STATUS.translateColored(ChatFormatting.GRAY, Math.min(stakes, STAKES_NEEDED) + 1, STAKES_NEEDED + 1,
						DragonsPulse.describe(DragonsPulse.strength(serverLevel, worldPosition))));
			}
			return InteractionResult.SUCCESS;
		} else if (focus.isEmpty() && isFocus(held)) {
			focus = held.split(1);
			serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
			changed();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static boolean isFocus(ItemStack stack) {
		return stack.is(PEItems.HARVEST_GODDESS_BAND) || stack.is(PEBlocks.INTERDICTION_TORCH.asItem()) || stack.is(PEItems.SOUL_STONE)
			   || stack.is(PEItems.ALCHEMICAL_SALT);
	}

	private int countStakes(ServerLevel level) {
		int count = 0;
		for (BlockPos stake : LOADED.getOrDefault(level.dimension(), Set.of())) {
			if (!stake.equals(worldPosition) && stake.closerThan(worldPosition, ARRAY_RANGE)) {
				count++;
			}
		}
		return count;
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, PulseStakeBlockEntity stake) {
		if (stake.focus.isEmpty()) {
			if (stake.formed) {
				stake.formed = false;
				stake.changed();
			}
			return;
		}
		ServerLevel serverLevel = (ServerLevel) level;
		if ((level.getGameTime() + pos.asLong()) % 40 == 0) {
			boolean nowFormed = stake.countStakes(serverLevel) >= STAKES_NEEDED;
			int nowStrength = DragonsPulse.strength(serverLevel, pos);
			if (nowFormed != stake.formed || nowStrength != stake.strength) {
				stake.formed = nowFormed;
				stake.strength = nowStrength;
				stake.changed();
			}
		}
		if (!stake.formed || stake.strength == 0) {
			return;
		}
		AABB field = new AABB(pos).inflate(stake.radius());
		if (stake.focus.is(PEBlocks.INTERDICTION_TORCH.asItem())) {
			Vec3 centre = pos.getCenter();
			for (Entity entity : level.getEntitiesOfClass(Entity.class, field, HOSTILE)) {
				WorldHelper.repelEntity(centre, entity);
			}
			return;
		}
		//Everything else works in pulses, faster the stronger the pulse
		int interval = Math.max(20, 200 - stake.strength * 12);
		if (level.getGameTime() % interval != 0) {
			return;
		}
		if (stake.focus.is(PEItems.HARVEST_GODDESS_BAND)) {
			WorldHelper.growNearbyRandomly(true, level, field, null);
		} else if (stake.focus.is(PEItems.SOUL_STONE)) {
			for (Player player : level.getEntitiesOfClass(Player.class, field)) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, interval + 20, 0, true, true));
			}
		}
		serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 4, 0.1, 0.3, 0.1, 0.02);
	}

	public static void tickClient(Level level, BlockPos pos, BlockState state, PulseStakeBlockEntity stake) {
		if (stake.formed && level.getRandom().nextInt(10) == 0) {
			level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0, 0.03, 0);
		}
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
		if (level != null && !level.isClientSide()) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, focus);
			focus = ItemStack.EMPTY;
		}
		super.preRemoveSideEffects(pos, state);
	}

	@Override
	protected void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		focus = input.read("focus", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		formed = input.getBooleanOr("formed", false);
		strength = input.getIntOr("strength", 0);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		if (!focus.isEmpty()) {
			output.store("focus", ItemStack.CODEC, focus);
		}
		output.putBoolean("formed", formed);
		output.putInt("strength", strength);
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
}
