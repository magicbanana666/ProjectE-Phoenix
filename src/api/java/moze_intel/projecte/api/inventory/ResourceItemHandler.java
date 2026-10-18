package moze_intel.projecte.api.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

/**
 * {@link IItemHandler} view of a transfer API item handler, see {@link IItemHandler#of(ResourceHandler)}.
 */
final class ResourceItemHandler implements IItemHandler {

	private final ResourceHandler<ItemResource> handler;

	ResourceItemHandler(ResourceHandler<ItemResource> handler) {
		this.handler = handler;
	}

	@Override
	public int getSlots() {
		return handler.size();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemUtil.getStack(handler, slot);
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return ItemUtil.insertItemReturnRemaining(handler, slot, stack, simulate, null);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemResource resource = handler.getResource(slot);
		if (resource.isEmpty()) {
			return ItemStack.EMPTY;
		}
		try (Transaction transaction = Transaction.openRoot()) {
			int extracted = handler.extract(slot, resource, Math.min(amount, resource.getMaxStackSize()), transaction);
			if (!simulate) {
				transaction.commit();
			}
			return resource.toStack(extracted);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return handler.getCapacityAsInt(slot, ItemResource.EMPTY);
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return handler.isValid(slot, ItemResource.of(stack));
	}
}
