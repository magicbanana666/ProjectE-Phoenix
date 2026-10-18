package moze_intel.projecte.api.inventory;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

/**
 * A slot based inventory view, used by ProjectE for its own inventories (alchemical bags, the transmutation input slots, block entity inventories).
 * <p>
 * NeoForge removed its slot based item handler in favour of {@link ResourceHandler}{@code <}{@link ItemResource}{@code >}, ProjectE keeps this view for its
 * own inventories as their logic is written around it. Item capabilities of other mods can be viewed through {@link #of(ResourceHandler)}.
 */
public interface IItemHandler {

	/**
	 * Creates a view of a transfer API item handler, every insertion or extraction that is not simulated is committed right away.
	 *
	 * @param handler The handler to view.
	 */
	static IItemHandler of(@NotNull ResourceHandler<ItemResource> handler) {
		Objects.requireNonNull(handler, "handler");
		return new ResourceItemHandler(handler);
	}

	/**
	 * @return The number of slots in this inventory.
	 */
	int getSlots();

	/**
	 * @param slot Slot to query.
	 *
	 * @return The stack in the given slot, it must not be modified.
	 */
	@NotNull
	ItemStack getStackInSlot(int slot);

	/**
	 * Inserts a stack into the given slot.
	 *
	 * @param slot     Slot to insert into.
	 * @param stack    Stack to insert, it is not modified.
	 * @param simulate {@code true} to only simulate the insertion.
	 *
	 * @return The remainder that could not be inserted, or {@link ItemStack#EMPTY} if everything was inserted.
	 */
	@NotNull
	ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

	/**
	 * Extracts at most the given amount (and at most one max stack size) from the given slot.
	 *
	 * @param slot     Slot to extract from.
	 * @param amount   Amount to extract.
	 * @param simulate {@code true} to only simulate the extraction.
	 *
	 * @return The extracted stack, or {@link ItemStack#EMPTY} if nothing could be extracted.
	 */
	@NotNull
	ItemStack extractItem(int slot, int amount, boolean simulate);

	/**
	 * @param slot Slot to query.
	 *
	 * @return The maximum amount of items the slot can hold.
	 */
	int getSlotLimit(int slot);

	/**
	 * @param slot  Slot to query.
	 * @param stack Stack to test.
	 *
	 * @return {@code true} if the stack is allowed in the given slot.
	 */
	boolean isItemValid(int slot, @NotNull ItemStack stack);
}
