package moze_intel.projecte.utils.inventory;

import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * View of the main inventory (hotbar and the 27 storage slots) of a player.
 */
public class PlayerMainInvWrapper implements IItemHandlerModifiable {

	private final Inventory inventory;

	public PlayerMainInvWrapper(Inventory inventory) {
		this.inventory = inventory;
	}

	public Inventory getInventoryPlayer() {
		return inventory;
	}

	@Override
	public int getSlots() {
		return Inventory.INVENTORY_SIZE;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return checkSlot(slot) ? inventory.getItem(slot) : ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		} else if (!checkSlot(slot) || !inventory.canPlaceItem(slot, stack)) {
			return stack;
		}
		ItemStack stackInSlot = inventory.getItem(slot);
		int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
		if (!stackInSlot.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(stack, stackInSlot)) {
				return stack;
			}
			limit -= stackInSlot.getCount();
		}
		if (limit <= 0) {
			return stack;
		}
		int toInsert = Math.min(limit, stack.getCount());
		if (!simulate) {
			inventory.setItem(slot, stack.copyWithCount(stackInSlot.getCount() + toInsert));
			inventory.setChanged();
			//The stack in the slot changed, animate it
			ItemStack inSlot = inventory.getItem(slot);
			if (!inSlot.isEmpty()) {
				if (inventory.player.level().isClientSide()) {
					inSlot.setPopTime(5);
				} else if (inventory.player instanceof ServerPlayer) {
					inventory.player.containerMenu.broadcastChanges();
				}
			}
		}
		return toInsert == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - toInsert);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0 || !checkSlot(slot)) {
			return ItemStack.EMPTY;
		}
		ItemStack stackInSlot = inventory.getItem(slot);
		if (stackInSlot.isEmpty()) {
			return ItemStack.EMPTY;
		} else if (simulate) {
			return stackInSlot.copyWithCount(Math.min(stackInSlot.getCount(), amount));
		}
		ItemStack extracted = inventory.removeItem(slot, Math.min(stackInSlot.getCount(), amount));
		inventory.setChanged();
		return extracted;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		if (checkSlot(slot)) {
			inventory.setItem(slot, stack);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return checkSlot(slot) ? inventory.getMaxStackSize() : 0;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return checkSlot(slot) && inventory.canPlaceItem(slot, stack);
	}

	private boolean checkSlot(int slot) {
		return slot >= 0 && slot < Inventory.INVENTORY_SIZE;
	}
}
