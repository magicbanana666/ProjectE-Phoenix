package moze_intel.projecte.utils.inventory;

import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.world.inventory.StackCopySlot;
import org.jetbrains.annotations.NotNull;

/**
 * Menu slot for item handlers whose {@link IItemHandler#getStackInSlot(int)} returns a copy of the stored stack, such as handlers backed by a data component.
 */
public class ItemHandlerCopySlot extends StackCopySlot {

	private final SlotItemHandler slotItemHandler;

	public ItemHandlerCopySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(index, xPosition, yPosition);
		slotItemHandler = new SlotItemHandler(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return slotItemHandler.mayPlace(stack);
	}

	@NotNull
	@Override
	protected ItemStack getStackCopy() {
		return slotItemHandler.getItem();
	}

	@Override
	protected void setStackCopy(@NotNull ItemStack stack) {
		((IItemHandlerModifiable) slotItemHandler.getItemHandler()).setStackInSlot(slotItemHandler.index, stack);
	}

	@Override
	public void onQuickCraft(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
		slotItemHandler.onQuickCraft(oldStack, newStack);
	}

	@Override
	public int getMaxStackSize() {
		return slotItemHandler.getMaxStackSize();
	}

	@Override
	public int getMaxStackSize(@NotNull ItemStack stack) {
		return slotItemHandler.getMaxStackSize(stack);
	}

	@Override
	public boolean mayPickup(@NotNull Player player) {
		return slotItemHandler.mayPickup(player);
	}

	@Override
	public boolean isSameInventory(@NotNull Slot other) {
		return slotItemHandler.isSameInventory(other);
	}

	public IItemHandler getItemHandler() {
		return slotItemHandler.getItemHandler();
	}
}
