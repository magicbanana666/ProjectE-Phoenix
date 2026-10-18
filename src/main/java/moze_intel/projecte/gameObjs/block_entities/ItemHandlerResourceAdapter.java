package moze_intel.projecte.gameObjs.block_entities;

import java.util.Objects;
import moze_intel.projecte.api.inventory.IItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Adapts a legacy {@link IItemHandler} view to the {@link ResourceHandler}{@code <}{@link ItemResource}{@code >}
 * view that item capabilities expose in 26.1, so existing inventory views can back block item capabilities.
 */
public final class ItemHandlerResourceAdapter implements ResourceHandler<ItemResource> {

	public static ResourceHandler<ItemResource> of(IItemHandler handler) {
		return new ItemHandlerResourceAdapter(handler);
	}

	private final IItemHandler handler;

	private ItemHandlerResourceAdapter(IItemHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		Objects.checkIndex(index, size());
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		Objects.checkIndex(index, size());
		return handler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return isValid(index, resource) ? Math.min(handler.getSlotLimit(index), resource.getMaxStackSize()) : 0;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		Objects.checkIndex(index, size());
		return handler.isItemValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		Objects.checkIndex(index, size());
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		//Note: The backing handler is not transaction aware, so changes apply immediately like other legacy views
		int inserted = 0;
		while (inserted < amount) {
			int attempt = Math.min(amount - inserted, resource.getMaxStackSize());
			int rejected = handler.insertItem(index, resource.toStack(attempt), false).getCount();
			inserted += attempt - rejected;
			if (rejected > 0) {
				break;
			}
		}
		return inserted;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		Objects.checkIndex(index, size());
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		ItemStack stack = handler.getStackInSlot(index);
		if (!resource.matches(stack)) {
			return 0;
		}
		return handler.extractItem(index, amount, false).getCount();
	}
}
