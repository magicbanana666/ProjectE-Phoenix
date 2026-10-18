package moze_intel.projecte.expansion.block.entity;

import moze_intel.projecte.expansion.util.Util;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.inventory.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

public class BlockEntityBase extends BlockEntity {
	private boolean updateComparators;
	public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	/***************
	 * Comparators *
	 ***************/

	protected void updateComparators(Level level, BlockPos pos) {
		//Only update the comparator state if we need to update comparators
		//Note: We call this at the end of child implementations to try and update any changes immediately instead
		// of them having to be delayed a tick
		if (updateComparators) {
			BlockState state = getBlockState();
			if (!state.isAir()) {
				level.updateNeighbourForOutputSignal(pos, state.getBlock());
			}
			updateComparators = false;
		}
	}

	@Override
	public final void setChanged() {
		if (level != null) {
			markDirty(level, worldPosition, true);
		}
	}

	/*********
	 * Dirty *
	 *********/

	public void markDirty() {
		if (level != null) {
			markDirty(level, worldPosition);
		}
	}

	public void markDirty(Level level, BlockPos pos) {
		markDirty(level, pos, false);
	}

	public void markDirty(Level level, BlockPos pos, boolean recheckComparators) {
		Util.markDirty(level, pos);
		if (recheckComparators && !level.isClientSide()) {
			updateComparators = true;
		}
	}

	/********
	 * Data *
	 ********/

	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public final ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	//26.1 still exposes the legacy IItemHandlerModifiable to slots and containers, so the backing stores stay on ItemStackHandler
	protected class StackHandler extends ItemStackHandler {

		protected StackHandler(int size) {
			super(size);
		}

		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	}

	@SuppressWarnings("unused")
	protected class CompactableStackHandler extends StackHandler {

		//Start as needing to check for compacting when loaded
		private boolean needsCompacting = true;
		private boolean empty;

		protected CompactableStackHandler(int size) {
			super(size);
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			needsCompacting = true;
		}

		public void compact() {
			if (needsCompacting) {
				if (level != null && !level.isClientSide()) {
					empty = ItemHelper.compactInventory(this);
				}
				needsCompacting = false;
			}
		}

		/**
		 * 26.1's {@link ItemStackHandler#deserialize(ValueInput)} no longer calls {@code onLoad()}, so the post load
		 * bookkeeping has to be hooked up explicitly here, otherwise {@link #isEmpty()} would keep its initial value.
		 */
		@Override
		public void deserialize(ValueInput input) {
			super.deserialize(input);
			onLoad();
		}

		@Override
		protected void onLoad() {
			super.onLoad();
			empty = true;
			for (int slot = 0, slots = getSlots(); slot < slots; slot++) {
				if (!getStackInSlot(slot).isEmpty()) {
					empty = false;
					break;
				}
			}
			needsCompacting = true;
		}

		/**
		 * @apiNote Only use this on the server
		 */
		public boolean isEmpty() {
			return empty;
		}
	}
}
