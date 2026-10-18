package moze_intel.projecte.utils.inventory;

import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Simple list backed inventory. Saves as an "Items" list of slotted stacks plus the "Size", the same layout NeoForge's removed ItemStackHandler used, so
 * existing worlds keep their contents.
 */
public class ItemStackHandler implements IItemHandlerModifiable, ValueIOSerializable {

	protected NonNullList<ItemStack> stacks;

	public ItemStackHandler() {
		this(1);
	}

	public ItemStackHandler(int size) {
		stacks = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	public ItemStackHandler(NonNullList<ItemStack> stacks) {
		this.stacks = stacks;
	}

	public void setSize(int size) {
		stacks = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		validateSlotIndex(slot);
		stacks.set(slot, stack);
		onContentsChanged(slot);
	}

	@Override
	public int getSlots() {
		return stacks.size();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		return stacks.get(slot);
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		} else if (!isItemValid(slot, stack)) {
			return stack;
		}
		validateSlotIndex(slot);
		ItemStack existing = stacks.get(slot);
		int limit = getStackLimit(slot, stack);
		if (!existing.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(stack, existing)) {
				return stack;
			}
			limit -= existing.getCount();
		}
		if (limit <= 0) {
			return stack;
		}
		boolean reachedLimit = stack.getCount() > limit;
		if (!simulate) {
			if (existing.isEmpty()) {
				stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
			}
			onContentsChanged(slot);
		}
		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}
		validateSlotIndex(slot);
		ItemStack existing = stacks.get(slot);
		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (existing.getCount() <= toExtract) {
			if (simulate) {
				return existing.copy();
			}
			stacks.set(slot, ItemStack.EMPTY);
			onContentsChanged(slot);
			return existing;
		}
		if (!simulate) {
			stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
			onContentsChanged(slot);
		}
		return existing.copyWithCount(toExtract);
	}

	@Override
	public int getSlotLimit(int slot) {
		return Item.ABSOLUTE_MAX_STACK_SIZE;
	}

	protected int getStackLimit(int slot, @NotNull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return true;
	}

	@Override
	public void serialize(ValueOutput output) {
		ValueOutput.TypedOutputList<ItemStackWithSlot> itemList = output.list("Items", ItemStackWithSlot.CODEC);
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			if (!stack.isEmpty()) {
				itemList.add(new ItemStackWithSlot(i, stack));
			}
		}
		output.putInt("Size", stacks.size());
	}

	@Override
	public void deserialize(ValueInput input) {
		setSize(input.getIntOr("Size", stacks.size()));
		input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(slot -> {
			if (slot.isValidInContainer(stacks.size())) {
				stacks.set(slot.slot(), slot.stack());
			}
		});
	}

	protected void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= stacks.size()) {
			throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
		}
	}

	protected void onLoad() {
	}

	protected void onContentsChanged(int slot) {
	}
}
