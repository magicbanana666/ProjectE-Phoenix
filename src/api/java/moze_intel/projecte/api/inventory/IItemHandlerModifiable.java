package moze_intel.projecte.api.inventory;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IItemHandler} whose slots can be set directly.
 */
public interface IItemHandlerModifiable extends IItemHandler {

	/**
	 * Replaces the stack in the given slot without any checks.
	 *
	 * @param slot  Slot to set.
	 * @param stack Stack to put into the slot.
	 */
	void setStackInSlot(int slot, @NotNull ItemStack stack);
}
