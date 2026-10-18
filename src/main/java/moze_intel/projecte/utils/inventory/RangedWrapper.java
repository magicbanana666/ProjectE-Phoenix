package moze_intel.projecte.utils.inventory;

import com.google.common.base.Preconditions;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * View of a range of slots of another inventory.
 */
public class RangedWrapper implements IItemHandlerModifiable {

	private final IItemHandlerModifiable compose;
	private final int minSlot;
	private final int maxSlot;

	public RangedWrapper(IItemHandlerModifiable compose, int minSlot, int maxSlotExclusive) {
		Preconditions.checkArgument(maxSlotExclusive > minSlot, "Max slot must be greater than min slot");
		this.compose = compose;
		this.minSlot = minSlot;
		this.maxSlot = maxSlotExclusive;
	}

	@Override
	public int getSlots() {
		return maxSlot - minSlot;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return checkSlot(slot) ? compose.getStackInSlot(slot + minSlot) : ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return checkSlot(slot) ? compose.insertItem(slot + minSlot, stack, simulate) : stack;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return checkSlot(slot) ? compose.extractItem(slot + minSlot, amount, simulate) : ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		if (checkSlot(slot)) {
			compose.setStackInSlot(slot + minSlot, stack);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return checkSlot(slot) ? compose.getSlotLimit(slot + minSlot) : 0;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return checkSlot(slot) && compose.isItemValid(slot + minSlot, stack);
	}

	private boolean checkSlot(int localSlot) {
		return localSlot + minSlot < maxSlot;
	}
}
