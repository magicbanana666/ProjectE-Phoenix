package moze_intel.projecte.utils.inventory;

import moze_intel.projecte.api.inventory.IItemHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for working with {@link IItemHandler}s.
 */
public final class ItemHandlerHelper {

	private ItemHandlerHelper() {
	}

	/**
	 * Inserts the stack into the first slots that accept it.
	 *
	 * @return The remainder that could not be inserted.
	 */
	@NotNull
	public static ItemStack insertItem(@Nullable IItemHandler dest, @NotNull ItemStack stack, boolean simulate) {
		if (dest == null || stack.isEmpty()) {
			return stack;
		}
		for (int i = 0; i < dest.getSlots(); i++) {
			stack = dest.insertItem(i, stack, simulate);
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		return stack;
	}

	/**
	 * Inserts the stack into the slots that already contain the same item first, and only then into empty slots.
	 *
	 * @return The remainder that could not be inserted.
	 */
	@NotNull
	public static ItemStack insertItemStacked(@Nullable IItemHandler inventory, @NotNull ItemStack stack, boolean simulate) {
		if (inventory == null || stack.isEmpty()) {
			return stack;
		} else if (!stack.isStackable()) {
			return insertItem(inventory, stack, simulate);
		}
		int slots = inventory.getSlots();
		//Fill up the slots that already contain the item
		for (int i = 0; i < slots && !stack.isEmpty(); i++) {
			if (ItemStack.isSameItemSameComponents(inventory.getStackInSlot(i), stack)) {
				stack = inventory.insertItem(i, stack, simulate);
			}
		}
		//Then put the remainder into empty slots
		for (int i = 0; i < slots && !stack.isEmpty(); i++) {
			if (inventory.getStackInSlot(i).isEmpty()) {
				stack = inventory.insertItem(i, stack, simulate);
			}
		}
		return stack;
	}

	/**
	 * Gives the stack to the player, dropping anything that does not fit into the main inventory at the player's position.
	 */
	public static void giveItemToPlayer(Player player, @NotNull ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		Level level = player.level();
		ItemStack remainder = insertItemStacked(new PlayerMainInvWrapper(player.getInventory()), stack, false);
		if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
			level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
					((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
		}
		if (!remainder.isEmpty() && !level.isClientSide()) {
			ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
			item.setPickUpDelay(40);
			item.setDeltaMovement(item.getDeltaMovement().multiply(0, 1, 0));
			level.addFreshEntity(item);
		}
	}

	/**
	 * Calculates the comparator signal of an inventory the same way vanilla does for containers.
	 */
	public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
		if (inv == null || inv.getSlots() == 0) {
			return 0;
		}
		int itemsFound = 0;
		float proportion = 0.0F;
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				proportion += (float) stack.getCount() / (float) Math.min(inv.getSlotLimit(slot), stack.getMaxStackSize());
				itemsFound++;
			}
		}
		proportion /= (float) inv.getSlots();
		return Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
	}
}
