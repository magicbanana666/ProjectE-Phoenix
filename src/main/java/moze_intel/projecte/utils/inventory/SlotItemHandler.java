package moze_intel.projecte.utils.inventory;

import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Menu slot backed by a slot of an {@link IItemHandler}.
 */
public class SlotItemHandler extends Slot {

	private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

	private final IItemHandler itemHandler;
	protected final int index;

	public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(EMPTY_INVENTORY, index, xPosition, yPosition);
		this.itemHandler = itemHandler;
		this.index = index;
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return !stack.isEmpty() && itemHandler.isItemValid(index, stack);
	}

	@NotNull
	@Override
	public ItemStack getItem() {
		return getItemHandler().getStackInSlot(index);
	}

	//Override if the item handler does not implement IItemHandlerModifiable
	@Override
	public void set(@NotNull ItemStack stack) {
		((IItemHandlerModifiable) getItemHandler()).setStackInSlot(index, stack);
		setChanged();
	}

	//Override if the item handler does not implement IItemHandlerModifiable
	public void initialize(@NotNull ItemStack stack) {
		((IItemHandlerModifiable) getItemHandler()).setStackInSlot(index, stack);
		setChanged();
	}

	@Override
	public void onQuickCraft(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
	}

	@Override
	public int getMaxStackSize() {
		return itemHandler.getSlotLimit(index);
	}

	@Override
	public int getMaxStackSize(@NotNull ItemStack stack) {
		return Math.min(stack.getMaxStackSize(), itemHandler.getSlotLimit(index));
	}

	@Override
	public boolean mayPickup(@NotNull Player player) {
		return !getItemHandler().extractItem(index, 1, true).isEmpty();
	}

	@NotNull
	@Override
	public ItemStack remove(int amount) {
		return getItemHandler().extractItem(index, amount, false);
	}

	public IItemHandler getItemHandler() {
		return itemHandler;
	}

	@Override
	public boolean isSameInventory(@NotNull Slot other) {
		return other instanceof SlotItemHandler slot && slot.itemHandler == itemHandler;
	}
}
