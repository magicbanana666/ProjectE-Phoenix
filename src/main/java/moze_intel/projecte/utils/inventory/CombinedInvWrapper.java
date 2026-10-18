package moze_intel.projecte.utils.inventory;

import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Combines several inventories into one, the slots of each inventory follow the slots of the previous one.
 */
public class CombinedInvWrapper implements IItemHandlerModifiable {

	protected final IItemHandlerModifiable[] itemHandler;
	//The exclusive end slot of each handler
	protected final int[] baseIndex;
	protected final int slotCount;

	public CombinedInvWrapper(IItemHandlerModifiable... itemHandler) {
		this.itemHandler = itemHandler;
		this.baseIndex = new int[itemHandler.length];
		int index = 0;
		for (int i = 0; i < itemHandler.length; i++) {
			index += itemHandler[i].getSlots();
			baseIndex[i] = index;
		}
		this.slotCount = index;
	}

	/**
	 * @return The index of the handler that contains the slot, or {@code -1} if no handler does.
	 */
	protected int getIndexForSlot(int slot) {
		if (slot >= 0) {
			for (int i = 0; i < baseIndex.length; i++) {
				if (slot < baseIndex[i]) {
					return i;
				}
			}
		}
		return -1;
	}

	protected IItemHandlerModifiable getHandlerFromIndex(int index) {
		if (index < 0 || index >= itemHandler.length) {
			return EmptyItemHandler.INSTANCE;
		}
		return itemHandler[index];
	}

	protected int getSlotFromIndex(int slot, int index) {
		if (index <= 0 || index >= baseIndex.length) {
			return slot;
		}
		return slot - baseIndex[index - 1];
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		int index = getIndexForSlot(slot);
		getHandlerFromIndex(index).setStackInSlot(getSlotFromIndex(slot, index), stack);
	}

	@Override
	public int getSlots() {
		return slotCount;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		int index = getIndexForSlot(slot);
		return getHandlerFromIndex(index).getStackInSlot(getSlotFromIndex(slot, index));
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		int index = getIndexForSlot(slot);
		return getHandlerFromIndex(index).insertItem(getSlotFromIndex(slot, index), stack, simulate);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		int index = getIndexForSlot(slot);
		return getHandlerFromIndex(index).extractItem(getSlotFromIndex(slot, index), amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		int index = getIndexForSlot(slot);
		return getHandlerFromIndex(index).getSlotLimit(getSlotFromIndex(slot, index));
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		int index = getIndexForSlot(slot);
		return getHandlerFromIndex(index).isItemValid(getSlotFromIndex(slot, index), stack);
	}
}
