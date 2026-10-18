package moze_intel.projecte.utils.inventory;

import com.google.common.base.Preconditions;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Inventory view that stores its contents in an {@link ItemContainerContents} component of a stack.
 */
public class ComponentItemHandler implements IItemHandlerModifiable {

	protected final MutableDataComponentHolder parent;
	protected final DataComponentType<ItemContainerContents> component;
	protected final int size;

	public ComponentItemHandler(MutableDataComponentHolder parent, DataComponentType<ItemContainerContents> component, int size) {
		Preconditions.checkArgument(size <= 256, "The max size of ItemContainerContents is 256 slots.");
		this.parent = parent;
		this.component = component;
		this.size = size;
	}

	@Override
	public int getSlots() {
		return size;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return getStackFromContents(getContents(), slot);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		validateSlotIndex(slot);
		if (!isItemValid(slot, stack)) {
			throw new IllegalArgumentException("Invalid stack " + stack + " for slot " + slot);
		}
		ItemContainerContents contents = getContents();
		if (!ItemStack.matches(stack, getStackFromContents(contents, slot))) {
			updateContents(contents, stack, slot);
		}
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack toInsert, boolean simulate) {
		validateSlotIndex(slot);
		if (toInsert.isEmpty()) {
			return ItemStack.EMPTY;
		} else if (!isItemValid(slot, toInsert)) {
			return toInsert;
		}
		ItemContainerContents contents = getContents();
		ItemStack existing = getStackFromContents(contents, slot);
		int insertLimit = Math.min(getSlotLimit(slot), toInsert.getMaxStackSize());
		if (!existing.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(toInsert, existing)) {
				return toInsert;
			}
			insertLimit -= existing.getCount();
		}
		if (insertLimit <= 0) {
			return toInsert;
		}
		int inserted = Math.min(insertLimit, toInsert.getCount());
		if (!simulate) {
			updateContents(contents, toInsert.copyWithCount(existing.getCount() + inserted), slot);
		}
		return toInsert.copyWithCount(toInsert.getCount() - inserted);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		validateSlotIndex(slot);
		if (amount == 0) {
			return ItemStack.EMPTY;
		}
		ItemContainerContents contents = getContents();
		ItemStack existing = getStackFromContents(contents, slot);
		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int toExtract = Math.min(amount, existing.getCount());
		if (!simulate) {
			updateContents(contents, existing.copyWithCount(existing.getCount() - toExtract), slot);
		}
		return existing.copyWithCount(toExtract);
	}

	@Override
	public int getSlotLimit(int slot) {
		return Item.ABSOLUTE_MAX_STACK_SIZE;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return stack.canFitInsideContainerItems();
	}

	protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack) {
	}

	protected ItemContainerContents getContents() {
		return parent.getOrDefault(component, ItemContainerContents.EMPTY);
	}

	protected ItemStack getStackFromContents(ItemContainerContents contents, int slot) {
		validateSlotIndex(slot);
		return contents.getSlots() <= slot ? ItemStack.EMPTY : contents.getTemplateInSlot(slot).map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
	}

	protected void updateContents(ItemContainerContents contents, ItemStack stack, int slot) {
		validateSlotIndex(slot);
		//Use the larger of the two sizes so that no stored stacks get truncated
		NonNullList<ItemStack> list = NonNullList.withSize(Math.max(contents.getSlots(), getSlots()), ItemStack.EMPTY);
		contents.copyInto(list);
		ItemStack oldStack = list.get(slot);
		list.set(slot, stack);
		parent.set(component, ItemContainerContents.fromItems(list));
		onContentsChanged(slot, oldStack, stack);
	}

	protected final void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= getSlots()) {
			throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
		}
	}
}
