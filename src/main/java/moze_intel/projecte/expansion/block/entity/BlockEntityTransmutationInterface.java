package moze_intel.projecte.expansion.block.entity;

import java.math.BigInteger;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.registries.ExpansionBlockEntityTypes;
import moze_intel.projecte.expansion.util.Util;
import moze_intel.projecte.gameObjs.Comprehension;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class BlockEntityTransmutationInterface extends BlockEntityNBTFilterable {
	private static final ICapabilityProvider<BlockEntityTransmutationInterface, @Nullable Direction, ResourceHandler<ItemResource>> ITEM_HANDLER_CAPABILITY = (be, side) -> be.getItemHandler();

	private ItemInfo[] info;

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, ExpansionBlockEntityTypes.TRANSMUTATION_INTERFACE.get(), ITEM_HANDLER_CAPABILITY);
	}

	public BlockEntityTransmutationInterface(BlockPos pos, BlockState state) {
		super(ExpansionBlockEntityTypes.TRANSMUTATION_INTERFACE.get(), pos, state);
	}

	private ItemHandler getItemHandler() {
		return new ItemHandler();
	}

	private ItemInfo[] fetchKnowledge() {
		if (info != null) return info;
		@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
		if(provider == null) {
			return new ItemInfo[]{};
		}
		return info = provider.getKnowledge().toArray(new ItemInfo[0]);
	}

	private int getMaxCount(int slot) {
		@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
		if(provider == null) {
			return 0;
		}
		BigInteger playerEmc = provider.getEmc();
		if (playerEmc.compareTo(BigInteger.ZERO) < 1) return 0;
		BigInteger targetItemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(fetchKnowledge()[slot]));
		if (targetItemEmc.compareTo(BigInteger.ZERO) < 1) return 0;
		return playerEmc.divide(targetItemEmc).min(BigInteger.valueOf(Math.max(1, Config.server.transmutationInterfaceItemCount.get()))).intValue();
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (blockEntity instanceof BlockEntityTransmutationInterface be)
			be.tickServer(level, pos, state, be);
	}

	public void tickServer(Level level, BlockPos pos, BlockState state, BlockEntityTransmutationInterface blockEntity) {
		info = null;
	}

	/****************
	 * Capabilities *
	 ****************/

	//26.1 item capabilities are ResourceHandler<ItemResource>, so the two "virtual" inventories (the emc deposit slot
	// and the knowledge based output slots) are served through the transfer API. The emc/knowledge side effects are
	// applied through a RootCommitJournal so that they only happen when the surrounding transaction is really committed.
	private class ItemHandler implements ResourceHandler<ItemResource> {
		@Override
		public int size() {
			return fetchKnowledge().length + 1;
		}

		@Override
		public ItemResource getResource(int index) {
			if (index <= 0 || fetchKnowledge().length < index || getMaxCount(index - 1) <= 0) return ItemResource.EMPTY;
			return ItemResource.of(fetchKnowledge()[index - 1].createStack());
		}

		@Override
		public long getAmountAsLong(int index) {
			if (index <= 0 || fetchKnowledge().length < index) return 0;
			return getMaxCount(index - 1);
		}

		@Override
		public long getCapacityAsLong(int index, ItemResource resource) {
			return index <= 0 ? 0 : Integer.MAX_VALUE;
		}

		@Override
		public boolean isValid(int index, ItemResource resource) {
			return index == 0 && IEMCProxy.INSTANCE.hasValue(resource.toStack(1));
		}

		@Override
		public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (index != 0 || !isValid(index, resource) || amount <= 0 || Util.getPlayer(owner) == null) return 0;

			ItemInfo itemInfo = ItemInfo.fromStack(resource.toStack(1));
			if (getFilterStatus() && !IEMCProxy.INSTANCE.getPersistentInfo(itemInfo).equals(itemInfo)) return 0;

			@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
			if (provider == null) return 0;

			long emcValue = IEMCProxy.INSTANCE.getSellValue(resource.toStack(1));
			BigInteger totalEmcValue = BigInteger.valueOf(emcValue).multiply(BigInteger.valueOf(amount));

			new RootCommitJournal(() -> {
				provider.setEmc(provider.getEmc().add(totalEmcValue));
				ServerPlayer player = Util.getPlayer(level, owner);
				if (player != null) {
					ItemInfo learned = IEMCProxy.INSTANCE.getPersistentInfo(itemInfo);
					if (!provider.hasKnowledge(learned) && Comprehension.study(player, learned, amount, false) && provider.addKnowledge(resource.toStack(1))) {
						provider.syncKnowledgeChange(player, learned, true);
					}
					provider.syncEmc(player);
				}
			}).updateSnapshots(transaction);
			return amount;
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (index <= 0 || resource.isEmpty() || fetchKnowledge().length < index || Util.getPlayer(owner) == null) return 0;
			if (resource.getItem() != fetchKnowledge()[index - 1].createStack().getItem()) return 0;

			int maxCount = Math.min(amount, getMaxCount(index - 1));
			if (maxCount <= 0) return 0;

			long emcValue = IEMCProxy.INSTANCE.getValue(fetchKnowledge()[index - 1]);
			BigInteger totalEmcCost = BigInteger.valueOf(emcValue).multiply(BigInteger.valueOf(maxCount));

			@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
			if(provider == null) return 0;

			new RootCommitJournal(() -> {
				provider.setEmc(provider.getEmc().subtract(totalEmcCost));
				ServerPlayer player = Util.getPlayer(level, owner);
				if (player != null) provider.syncEmc(player);
			}).updateSnapshots(transaction);
			return maxCount;
		}
	}

	ItemHandler getItemHandlerCapability() {
		return (ItemHandler) WorldHelper.getCapability(level, Capabilities.Item.BLOCK, worldPosition, getBlockState(), this, null);
	}
}
