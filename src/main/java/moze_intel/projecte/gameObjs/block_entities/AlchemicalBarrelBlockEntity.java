package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.gameObjs.container.AlchemicalBarrelContainer;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemicalBarrelBlockEntity extends EmcBlockEntity implements MenuProvider {

	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
			playSound(level, pos, state, SoundEvents.BARREL_OPEN);
			level.setBlockAndUpdate(pos, state.setValue(BarrelBlock.OPEN, true));
		}

		@Override
		protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
			playSound(level, pos, state, SoundEvents.BARREL_CLOSE);
			level.setBlockAndUpdate(pos, state.setValue(BarrelBlock.OPEN, false));
		}

		private void playSound(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, SoundEvent sound) {
			Vec3 soundPos = Vec3.atCenterOf(pos).relative(state.getValue(BarrelBlock.FACING), 0.5);
			level.playSound(null, soundPos.x(), soundPos.y(), soundPos.z(), sound, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
		}

		@Override
		protected void openerCountChanged(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, int oldCount, int openCount) {
		}

		@Override
		public boolean isOwnContainer(Player player) {
			return player.containerMenu instanceof AlchemicalBarrelContainer container && container.blockEntityMatches(AlchemicalBarrelBlockEntity.this);
		}
	};
	private final BarrelInventory inventory = new BarrelInventory();
	private boolean inventoryChanged;

	public AlchemicalBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.ALCHEMICAL_BARREL, pos, state, 1_000);
	}

	public static void tickClient(Level level, BlockPos pos, BlockState state, AlchemicalBarrelBlockEntity barrel) {
		for (int i = 0, slots = barrel.inventory.getSlots(); i < slots; i++) {
			ItemStack stack = barrel.inventory.getStackInSlot(i);
			IAlchChestItem chestItem = stack.getCapability(PECapabilities.ALCH_CHEST_ITEM_CAPABILITY);
			if (chestItem != null) {
				chestItem.updateInAlchChest(level, pos, stack);
			}
		}
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, AlchemicalBarrelBlockEntity barrel) {
		for (int i = 0, slots = barrel.inventory.getSlots(); i < slots; i++) {
			ItemStack stack = barrel.inventory.getStackInSlot(i);
			IAlchChestItem chestItem = stack.getCapability(PECapabilities.ALCH_CHEST_ITEM_CAPABILITY);
			if (chestItem != null && chestItem.updateInAlchChest(level, pos, stack)) {
				barrel.inventory.onContentsChanged(i);
			}
		}
		if (barrel.inventoryChanged) {
			//If the inventory changed, resync so that the client can tick things properly
			barrel.inventoryChanged = false;
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
		}
		barrel.updateComparators(level, pos);
	}

	@Override
	public void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		input.readChild("inventory", inventory);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		output.putChild("inventory", inventory);
	}

	public void startOpen(Player player) {
		if (!isRemoved() && !player.isSpectator() && level != null) {
			openersCounter.incrementOpeners(player, level, getBlockPos(), getBlockState(), player.getContainerInteractionRange());
		}
	}

	public void stopOpen(Player player) {
		if (!isRemoved() && !player.isSpectator() && level != null) {
			openersCounter.decrementOpeners(player, level, getBlockPos(), getBlockState());
		}
	}

	public void recheckOpen() {
		if (!isRemoved() && level != null) {
			openersCounter.recheckOpeners(level, getBlockPos(), getBlockState());
		}
	}

	public IItemHandler getInventory(@Nullable Direction direction) {
		return inventory;
	}

	@NotNull
	@Override
	public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerIn) {
		return new AlchemicalBarrelContainer(windowId, playerInventory, this);
	}

	@NotNull
	@Override
	public Component getDisplayName() {
		return TextComponentUtil.build(PEBlocks.ALCHEMICAL_BARREL);
	}

	private class BarrelInventory extends StackHandler {

		protected BarrelInventory() {
			super(104);
		}

		@Override
		public void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			if (level != null && !level.isClientSide()) {
				inventoryChanged = true;
			}
		}
	}
}
