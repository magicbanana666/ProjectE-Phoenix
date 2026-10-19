package moze_intel.projecte.gameObjs.block_entities;

import java.math.BigInteger;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A homunculus in a flask, the only good thing to come of a failed human transmutation. It gathers the items lying within eight
 * blocks into the container under its flask. It lives on its keeper's EMC, a thousand a day, and only while its keeper is
 * there to feed it; hungry, it sleeps.
 */
public class HomunculusFlaskBlockEntity extends BlockEntity {

	private static final int RANGE = 8;
	/**
	 * A thousand EMC a day, paid every minute.
	 */
	private static final long FEED_COST = 50;
	private static final int FEED_INTERVAL = 60 * 20;

	@Nullable
	private UUID owner;
	private boolean awake;

	public HomunculusFlaskBlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.HOMUNCULUS_FLASK.get(), pos, state);
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
		setChanged();
	}

	public boolean isAwake() {
		return awake;
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, HomunculusFlaskBlockEntity flask) {
		ServerLevel serverLevel = (ServerLevel) level;
		if ((level.getGameTime() + pos.asLong()) % FEED_INTERVAL == 0) {
			flask.feed(serverLevel);
		}
		if (!flask.awake || level.getGameTime() % 10 != 0) {
			return;
		}
		Container below = HopperBlockEntity.getContainerAt(level, pos.below());
		if (below == null) {
			return;
		}
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(RANGE), item -> item.isAlive() && !item.hasPickUpDelay())) {
			double x = item.getX();
			double y = item.getY();
			double z = item.getZ();
			if (HopperBlockEntity.addItem(below, item)) {
				serverLevel.sendParticles(ParticleTypes.WITCH, x, y + 0.2, z, 3, 0.1, 0.1, 0.1, 0);
			}
		}
	}

	private void feed(ServerLevel level) {
		boolean fed = false;
		ServerPlayer keeper = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
		if (keeper != null) {
			IKnowledgeProvider provider = keeper.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (provider != null && provider.getEmc().compareTo(BigInteger.valueOf(FEED_COST)) >= 0) {
				provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(FEED_COST)));
				provider.syncEmc(keeper);
				fed = true;
			}
		}
		if (fed != awake) {
			awake = fed;
			setChanged();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	public static void tickClient(Level level, BlockPos pos, BlockState state, HomunculusFlaskBlockEntity flask) {
		if (flask.awake && level.getRandom().nextInt(20) == 0) {
			level.addParticle(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 0, 0.02, 0);
		}
	}

	@Override
	protected void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
		awake = input.getBooleanOr("awake", false);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		if (owner != null) {
			output.store("owner", UUIDUtil.CODEC, owner);
		}
		output.putBoolean("awake", awake);
	}

	@NotNull
	@Override
	public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("awake", awake);
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
