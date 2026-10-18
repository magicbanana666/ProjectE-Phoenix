package moze_intel.projecte.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import moze_intel.projecte.utils.inventory.ItemHandlerHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for Inventories, ItemStacks, Items, and the Ore Dictionary Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class ItemHelper {

	/**
	 * Gets an ActionResult based on a type
	 */
	public static InteractionResult actionResultFromType(InteractionResult type, ItemStack stack) {
		return type;
	}

	/**
	 * Compacts an inventory and returns if the inventory is/was empty.
	 *
	 * @return True if the inventory was empty.
	 */
	public static boolean compactInventory(IItemHandlerModifiable inventory) {
		List<ItemStack> temp = new ArrayList<>();
		for (int i = 0, slots = inventory.getSlots(); i < slots; i++) {
			ItemStack stackInSlot = inventory.getStackInSlot(i);
			if (!stackInSlot.isEmpty()) {
				temp.add(stackInSlot);
				inventory.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
		for (ItemStack s : temp) {
			ItemHandlerHelper.insertItemStacked(inventory, s, false);
		}
		return temp.isEmpty();
	}

	public static IItemHandlerModifiable immutableCopy(IItemHandler toCopy) {
		int slots = toCopy.getSlots();
		final List<ItemStack> list = new ArrayList<>(slots);
		for (int i = 0; i < slots; i++) {
			list.add(toCopy.getStackInSlot(i).copy());
		}
		return new IItemHandlerModifiable() {
			@Override
			public void setStackInSlot(int slot, @NotNull ItemStack stack) {
			}

			@Override
			public int getSlots() {
				return list.size();
			}

			@NotNull
			@Override
			public ItemStack getStackInSlot(int slot) {
				return list.get(slot);
			}

			@NotNull
			@Override
			public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				return stack;
			}

			@NotNull
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlotLimit(int slot) {
				return getStackInSlot(slot).getMaxStackSize();
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return true;
			}
		};
	}

	public static boolean setStack(ResourceHandler<ItemResource> handler, int index, ItemStack stack) {
		return setStack(handler, index, stack, null);
	}

	public static boolean setStack(ResourceHandler<ItemResource> handler, int index, ItemStack stack, @Nullable TransactionContext parent) {
		Objects.checkIndex(index, handler.size());
		ItemStack current = ItemUtil.getStack(handler, index);
		if (ItemStack.matches(current, stack)) {
			return true;
		}
		ItemResource currentResource = handler.getResource(index);
		int currentAmount = handler.getAmountAsInt(index);
		ItemResource newResource = ItemResource.of(stack);
		int newAmount = stack.getCount();
		try (Transaction transaction = Transaction.open(parent)) {
			//Note: Extracting an empty resource is rejected outright, so an empty slot is simply left alone
			if (currentAmount > 0 && handler.extract(index, currentResource, currentAmount, transaction) != currentAmount) {
				return false;
			}
			if (newAmount > 0 && handler.insert(index, newResource, newAmount, transaction) != newAmount) {
				return false;
			}
			transaction.commit();
			return true;
		}
	}

	public static boolean isRepairableDamagedItem(ItemStack stack) {
		//MC 26.1 removed ItemStack#isRepairable; repairability is now expressed via the REPAIRABLE
		// data component (repair with a matching material in an anvil) or combine-repairing with a copy
		return stack.isDamageableItem() && stack.getDamageValue() > 0 &&
			(stack.has(DataComponents.REPAIRABLE) || stack.getItem().isCombineRepairable(stack));
	}

	/**
	 * @return The amount of the given stack that could not fit. If it all fit, zero is returned
	 */
	public static int simulateFit(NonNullList<ItemStack> inv, ItemStack stack) {
		int remainder = stack.getCount();
		for (ItemStack invStack : inv) {
			if (invStack.isEmpty()) {
				//Slot is empty, just put it all there
				return 0;
			}
			if (ItemStack.isSameItemSameComponents(stack, invStack)) {
				int amountSlotNeeds = invStack.getMaxStackSize() - invStack.getCount();
				//Double check we don't have an over sized stack
				if (amountSlotNeeds > 0) {
					if (remainder <= amountSlotNeeds) {
						//If the slot can accept it all, return it all fit
						return 0;
					}
					//Otherwise take that many items out and
					remainder -= amountSlotNeeds;
				}
			}
		}
		return remainder;
	}

	public static ItemStack size(ItemStack stack, int size) {
		if (size <= 0 || stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return stack.copyWithCount(size);
	}

	public static BlockState stackToState(ItemStack stack, @Nullable BlockPlaceContext context) {
		if (stack.getItem() instanceof BlockItem blockItem) {
			if (context == null) {
				return blockItem.getBlock().defaultBlockState();
			}
			return blockItem.getBlock().getStateForPlacement(context);
		}
		return null;
	}
}
