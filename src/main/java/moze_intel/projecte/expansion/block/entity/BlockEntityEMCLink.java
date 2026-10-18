package moze_intel.projecte.expansion.block.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;
import javax.annotation.Nullable;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.expansion.block.BlockEMCLink;
import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.registries.ExpansionBlockEntityTypes;
import moze_intel.projecte.expansion.registries.ExpansionCapabilities;
import moze_intel.projecte.expansion.util.*;
import moze_intel.projecte.gameObjs.block_entities.ItemHandlerResourceAdapter;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.inventory.ItemHandlerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@SuppressWarnings("unused")
public class BlockEntityEMCLink extends BlockEntityNBTFilterable implements IHasMatter {
	public static final ICapabilityProvider<BlockEntityEMCLink, @org.jetbrains.annotations.Nullable Direction, IEmcStorage> EMC_STORAGE_PROVIDER = (link, side) -> link.getEMCHandler();
	public static final ICapabilityProvider<BlockEntityEMCLink, @org.jetbrains.annotations.Nullable Direction, IEmcStorageBigInteger> BIG_EMC_STORAGE_PROVIDER = (link, side) -> link.getEMCHandler();
	public static final ICapabilityProvider<BlockEntityEMCLink, @org.jetbrains.annotations.Nullable Direction, ResourceHandler<ItemResource>> ITEM_HANDLER_PROVIDER = (link, side) -> ItemHandlerResourceAdapter.of(link.getItemHandler());
	public static final ICapabilityProvider<BlockEntityEMCLink, @org.jetbrains.annotations.Nullable Direction, ResourceHandler<FluidResource>> FLUID_HANDLER_PROVIDER = (link, side) -> link.getFluidHandler();
	public BigInteger emc = BigInteger.ZERO;
	public ItemStack itemStack;
	public Matter matter;
	public BigInteger remainingEMC = BigInteger.ZERO;
	public int remainingImport = 0;
	public int remainingExport = 0;
	public int remainingFluid = 0;

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(PECapabilities.EMC_STORAGE_CAPABILITY, ExpansionBlockEntityTypes.EMC_LINK.get(), EMC_STORAGE_PROVIDER);
		event.registerBlockEntity(ExpansionCapabilities.BIG_EMC_STORAGE_CAPABILITY, ExpansionBlockEntityTypes.EMC_LINK.get(), BIG_EMC_STORAGE_PROVIDER);
		event.registerBlockEntity(Capabilities.Item.BLOCK, ExpansionBlockEntityTypes.EMC_LINK.get(), ITEM_HANDLER_PROVIDER);
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ExpansionBlockEntityTypes.EMC_LINK.get(), FLUID_HANDLER_PROVIDER);
	}

	public BlockEntityEMCLink(BlockPos pos, BlockState state) {
		super(ExpansionBlockEntityTypes.EMC_LINK.get(), pos, state);
		itemStack = ItemStack.EMPTY;
	}

	private EMCHandler getEMCHandler() {
		return new EMCHandler();
	}

	private ItemHandler getItemHandler() {
		return new ItemHandler();
	}

	private FluidHandler getFluidHandler() {
		return new FluidHandler();
	}

	/*******
	 * NBT *
	 *******/

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		emc = new BigInteger(input.getStringOr(TagNames.STORED_EMC, "0"));
		ItemStack storedItem = input.read(TagNames.ITEM, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		itemStack = IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(storedItem)).createStack();
		remainingEMC = new BigInteger(input.getStringOr(TagNames.REMAINING_EMC, "0"));
		remainingImport = input.getIntOr(TagNames.REMAINING_IMPORT, 0);
		remainingExport = input.getIntOr(TagNames.REMAINING_EXPORT, 0);
		remainingFluid = input.getIntOr(TagNames.REMAINING_FLUID, 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString(TagNames.STORED_EMC, emc.toString());
		output.store(TagNames.ITEM, ItemStack.OPTIONAL_CODEC, itemStack);
		output.putString(TagNames.REMAINING_EMC, remainingEMC.toString());
		output.putInt(TagNames.REMAINING_IMPORT, remainingImport);
		output.putInt(TagNames.REMAINING_EXPORT, remainingExport);
		output.putInt(TagNames.REMAINING_FLUID, remainingFluid);
	}

	/********
	 * MISC *
	 ********/

	public static void tickServer(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (blockEntity instanceof BlockEntityEMCLink be) be.tickServer(level, pos, state, be);
	}

	public void tickServer(Level level, BlockPos pos, BlockState state, BlockEntityEMCLink blockEntity) {
		// due to the nature of per second this block follows, using the config value isn't really possible
		if (level.isClientSide() || (level.getGameTime() % 20L) != Util.mod(hashCode(), 20)) return;
		resetLimits();
		if (emc.equals(BigInteger.ZERO)) return;
		ServerPlayer player = Util.getPlayer(level, owner);
		@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
		if (provider == null) return;

		BigInteger toAdd = getMatter() == Matter.FINAL ? emc : remainingEMC.min(emc);
		provider.setEmc(provider.getEmc().add(toAdd));
		emc = emc.subtract(toAdd).max(BigInteger.ZERO);
		if (player != null) provider.syncEmc(player);
		markDirty(level, pos);
		emc = BigInteger.ZERO;
	}

	private void resetLimits() {
		Matter m = getMatter();
		remainingEMC    = m.getEMCLinkEMCLimit();
		remainingImport = remainingExport = m.getEMCLinkItemLimit();
		remainingFluid  = m.getEMCLinkFluidLimit();
	}

	private void setInternalItem(ItemStack stack) {
		itemStack = IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(stack.copyWithCount(1))).createStack();
		markDirty();
	}

	@Override
	public void handlePlace(@Nullable LivingEntity livingEntity, ItemStack stack) {
		super.handlePlace(livingEntity, stack);
		resetLimits();
	}

	@Override
	public Matter getMatter() {
		if (level != null) {
			BlockEMCLink block = (BlockEMCLink) getBlockState().getBlock();
			if (block.getMatter() != matter) setMatter(block.getMatter());
			return matter;
		}
		return Matter.BASIC;
	}

	private void setMatter(Matter matter) {
		this.matter = matter;
	}

	public InteractionResult handleActivation(Player player, InteractionHand hand) {
		ItemStack inHand = player.getItemInHand(hand);
		ItemHandler itemHandler = getItemHandlerCapability();
		FluidHandler fluidHandler = getFluidHandlerCapability();

		if(!super.handleActivation(player, ActivationType.CHECK_OWNERSHIP)) return InteractionResult.CONSUME;

		if (player.isCrouching()) {
			if (itemStack.isEmpty()) {
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_NOT_SET.translateColored(ChatFormatting.RED));
				return InteractionResult.CONSUME;
			}
			if (inHand.isEmpty()) {
				setInternalItem(ItemStack.EMPTY);
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_CLEARED.translateColored(ChatFormatting.RED));
				return InteractionResult.SUCCESS;
			}
		}

		if (itemStack.isEmpty()) {
			if (inHand.isEmpty()) {
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_NOT_SET.translateColored(ChatFormatting.RED));
				return InteractionResult.CONSUME;
			}
			if (!itemHandler.isItemValid(0, inHand)) {
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_EMPTY_HAND.translateColored(ChatFormatting.RED, Component.translatable(itemStack.getItem().toString()).setStyle(ColorStyle.BLUE)));
				return InteractionResult.CONSUME;
			}
			setInternalItem(inHand);
			player.sendOverlayMessage(Lang.Blocks.EMC_LINK_SET.translateColored(ChatFormatting.GREEN, Component.literal(itemStack.getItem().toString()).setStyle(ColorStyle.BLUE)));
			return InteractionResult.SUCCESS;
		}

		Fluid fluid = fluidHandler.getFluid();
		if(fluid != null && fluidHandler.isValid() && inHand.getItem() instanceof BucketItem && ((BucketItem) inHand.getItem()).content == Fluids.EMPTY) {
			if(Config.server.limitEmcLinkVendor.get() && remainingFluid < 1000) {
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_NO_EXPORT_REMAINING.translateColored(ChatFormatting.RED));
				return InteractionResult.CONSUME;
			}
			long cost = fluidHandler.getFluidCost(1000);
			@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
			if(provider == null) {
				player.sendOverlayMessage(Lang.FAILED_TO_GET_KNOWLEDGE_PROVIDER.translateColored(ChatFormatting.RED, Util.getPlayer(owner) == null ? owner : Objects.requireNonNull(Util.getPlayer(owner)).getDisplayName()));
				return InteractionResult.FAIL;
			}
			BigInteger playerEmc = provider.getEmc();
			if(playerEmc.compareTo(BigInteger.valueOf(cost)) < 0) {
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_NOT_ENOUGH_EMC.translateColored(ChatFormatting.RED, Component.literal(EMCFormat.format(BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(itemStack)))).setStyle(ColorStyle.GREEN)));
				return InteractionResult.CONSUME;
			}
			//26.1 has no FluidUtil#tryFillContainer, the transfer API based interaction helper does the whole job
			// (it fills the bucket in the player's hand, plays the sounds and only applies the emc cost on commit)
			try (Transaction transaction = Transaction.openRoot()) {
				if (!FluidUtil.interactWithFluidHandler(player, hand, worldPosition, fluidHandler, transaction)) {
					return InteractionResult.FAIL;
				}
				provider.setEmc(playerEmc.subtract(BigInteger.valueOf(cost)));
				//Note: the fluid itself is only drained when the transaction commits, the handler does that
				markDirty();
				transaction.commit();
			}
			if(player instanceof ServerPlayer) provider.syncEmc((ServerPlayer) player);
			return InteractionResult.CONSUME;
		}

		if (inHand.isEmpty() || itemStack.is(inHand.getItem())) {
			if (Config.server.limitEmcLinkVendor.get() && remainingExport <= 0) {
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_NO_EXPORT_REMAINING.translateColored(ChatFormatting.RED));
				return InteractionResult.CONSUME;
			}
			ItemStack extract = itemHandler.extractItemInternal(0, itemStack.getMaxStackSize(), false, Config.server.limitEmcLinkVendor.get());
			if (extract.isEmpty()) {
				player.sendOverlayMessage(Lang.Blocks.EMC_LINK_NOT_ENOUGH_EMC.translateColored(ChatFormatting.RED, Component.literal(EMCFormat.format(BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(itemStack)))).setStyle(ColorStyle.GREEN)));
				return InteractionResult.CONSUME;
			}
			ItemHandlerHelper.giveItemToPlayer(player, extract);
			return InteractionResult.SUCCESS;
		}

		player.sendOverlayMessage(Lang.Blocks.EMC_LINK_EMPTY_HAND.translateColored(ChatFormatting.RED));
		return InteractionResult.CONSUME;
	}

	/****************
	 * Capabilities *
	 ****************/

	private class EMCHandler implements IEmcStorageBigInteger {
		@Override
		public BigInteger getStoredEmcBigInteger() {
			return BigInteger.ZERO;
		}
		@Override
		public BigInteger getMaximumEmcBigInteger() {
			return getMatter().getEMCLinkEMCLimit();
		}

		@Override
		public BigInteger extractEmcBigInteger(BigInteger value, EmcAction action) {
			return emc.compareTo(BigInteger.ZERO) < 0 ? insertEmcBigInteger(value.negate(), action) : value;
		}

		@Override
		public BigInteger insertEmcBigInteger(BigInteger value, EmcAction action) {
			boolean isFinal = getMatter() == Matter.FINAL;
			BigInteger v = isFinal ? value : remainingEMC.min(value);

			if (value.compareTo(BigInteger.ZERO) < 0) return BigInteger.ZERO;
			if (action.execute()) {
				if (!isFinal) remainingEMC = remainingEMC.subtract(v);
				emc = emc.add(v);
				markDirty();
			}

			return v;
		}
	}

	EMCHandler getEMCHandlerCapability() {
		return (EMCHandler) WorldHelper.getCapability(level, PECapabilities.EMC_STORAGE_CAPABILITY, worldPosition, getBlockState(), this, null);
	}

	//26.1 keeps the link's item view on the legacy IItemHandler, so the emc/knowledge side effects are applied
	//directly (guarded by the simulate flag) instead of being deferred to a transaction root commit
	private class ItemHandler implements IItemHandler {
		@Override
		public int getSlots() {
			return getMatter().getEMCLinkInventorySize();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot != 0 || itemStack.isEmpty()) return ItemStack.EMPTY;
			@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
			if (provider == null) return ItemStack.EMPTY;
			BigInteger val = BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(itemStack));
			if(val.equals(BigInteger.ZERO)) return ItemStack.EMPTY;
			BigInteger maxCount = provider.getEmc().divide(val).min(BigInteger.valueOf(Integer.MAX_VALUE));
			int count = maxCount.intValueExact();
			if (count <= 0) return ItemStack.EMPTY;

			return itemStack.copyWithCount(count);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			boolean isFinal = getMatter() == Matter.FINAL;
			if (slot == 0 || (!isFinal && remainingImport <= 0) || owner == null || stack.isEmpty() || !isItemValid(slot, stack) || Util.getPlayer(owner) == null) return stack;

			int count = stack.getCount();
			stack = stack.copyWithCount(1);

			if (count <= 0) return stack;

			ItemInfo info = ItemInfo.fromStack(stack);
			if(getFilterStatus() && !IEMCProxy.INSTANCE.getPersistentInfo(info).equals(info)) return stack;

			int insertCount = isFinal ? count : Math.min(count, remainingImport);
			if (!simulate) {
				long itemValue = IEMCProxy.INSTANCE.getSellValue(stack);
				@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
				if (provider == null) return stack;
				BigInteger totalValue = BigInteger.valueOf(itemValue).multiply(BigInteger.valueOf(insertCount));
				provider.setEmc(provider.getEmc().add(totalValue));
				ServerPlayer player = Util.getPlayer(owner);
				if (player != null) {
					if (provider.addKnowledge(stack))
						provider.syncKnowledgeChange(player, IEMCProxy.INSTANCE.getPersistentInfo(info), true);
					provider.syncEmc(player);
				}
				if(!isFinal) remainingImport -= insertCount;
				markDirty();
			}

			if (insertCount == count) return ItemStack.EMPTY;

			stack.setCount(count - insertCount);
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return extractItemInternal(slot, amount, simulate, true);
		}

		public ItemStack extractItemInternal(int slot, int amount, boolean simulate, boolean limit) {
			boolean isFinal = getMatter() == Matter.FINAL;
			if (slot != 0 || (!isFinal && remainingExport <= 0) || owner == null || itemStack.isEmpty() || Util.getPlayer(owner) == null) return ItemStack.EMPTY;

			BigInteger itemValue = BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(itemStack));
			if(itemValue.equals(BigInteger.ZERO)) return ItemStack.EMPTY;
			@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
			if (provider == null) return ItemStack.EMPTY;
			BigInteger maxCount = provider.getEmc().divide(itemValue).min(BigInteger.valueOf(Integer.MAX_VALUE));
			int extractCount = Math.min(amount, limit && !isFinal ? Math.min(maxCount.intValueExact(), remainingExport) : maxCount.intValueExact());
			if (extractCount <= 0) return ItemStack.EMPTY;

			ItemStack r = IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(itemStack.copy())).createStack();
			r.setCount(extractCount);
			if (simulate) return r;

			BigInteger totalPrice = itemValue.multiply(BigInteger.valueOf(extractCount));
			provider.setEmc(provider.getEmc().subtract(totalPrice));
			ServerPlayer player = Util.getPlayer(owner);
			if (player != null) provider.syncEmc(player);

			if (limit && !isFinal) remainingExport -= extractCount;
			markDirty();
			return r;
		}

		@Override
		public int getSlotLimit(int slot) {
			return getMatter().getEMCLinkItemLimit();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return IEMCProxy.INSTANCE.hasValue(stack);
		}
	}

	ItemHandler getItemHandlerCapability() {
		//Note: The registered capability is an ItemHandlerResourceAdapter wrapping a fresh ItemHandler, so it can no
		// longer be cast back to the inner class. The inner class is stateless and reads the block entity's fields
		//live, so handing out a new one is equivalent to the old capability lookup.
		return getItemHandler();
	}

	//26.1 fluid capabilities are ResourceHandler<FluidResource>, the emc side effect is applied on the root commit of
	//the surrounding transaction through a RootCommitJournal
	private class FluidHandler implements ResourceHandler<FluidResource> {
		public @Nullable Fluid getFluid() {
			if(!itemStack.isEmpty() && itemStack.getItem() instanceof BucketItem bucketItem) return bucketItem.content;
			else return null;
		}

		private double getFluidCostPer() {
			try {
				long fullCost = IEMCProxy.INSTANCE.getValue(itemStack);
				long bucketCost = IEMCProxy.INSTANCE.getValue(net.minecraft.world.item.Items.BUCKET);
				if (bucketCost == 0 && fullCost == 0) return 0D;
				return (fullCost - ((bucketCost * getMatter().getFluidEfficiencyPercentage()) / 100F))  / 1000D;
			} catch(ArithmeticException ignore) {
				return Long.MAX_VALUE;
			}
		}

		private boolean isFreeFluid() {
			return getFluidCostPer() == 0D && Config.server.zeroEmcFluidsAreFree.get();
		}

		private boolean isValid() {
			return getFluid() != null && (getFluidCostPer() != 0D || isFreeFluid());
		}

		private long getFluidCost(double amount) {
			try {
				double cost = getFluidCostPer();
				return (long) Math.ceil(cost * amount);
			} catch(ArithmeticException ignore) {
				return Long.MAX_VALUE;
			}
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public FluidResource getResource(int index) {
			if (index != 0) return FluidResource.EMPTY;
			Fluid fluid = getFluid();
			if(fluid == null || !isValid()) return FluidResource.EMPTY;
			return FluidResource.of(fluid);
		}

		@Override
		public long getAmountAsLong(int index) {
			if (index != 0 || getFluid() == null || !isValid()) return 0;
			return remainingFluid;
		}

		@Override
		public long getCapacityAsLong(int index, FluidResource resource) {
			return index == 0 ? remainingFluid : 0;
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			//The link never accepts fluids, it only ever hands them out
			return false;
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			return extractInternal(index, resource, amount, transaction);
		}

		private int extractInternal(int index, FluidResource resource, int amount, @Nullable TransactionContext transaction) {
			boolean isFinal = getMatter() == Matter.FINAL;
			Fluid fluid = getFluid();
			if (index != 0 || fluid == null || !isValid() || Util.getPlayer(owner) == null) return 0;
			if (!resource.isEmpty() && resource.value() != fluid) return 0;
			int maxDrain = amount;
			if(!isFinal && maxDrain > remainingFluid) maxDrain = remainingFluid;
			if(maxDrain > remainingFluid) maxDrain = remainingFluid;
			long cost = getFluidCost(maxDrain);
			@Nullable IKnowledgeProvider provider = Util.getKnowledgeProvider(owner);
			if(provider == null) return 0;
			BigInteger emc = provider.getEmc();
			BigDecimal dEMC = new BigDecimal(emc);
			if(dEMC.compareTo(BigDecimal.valueOf(getFluidCostPer())) < 0) return 0;
			if(emc.compareTo(BigInteger.valueOf(cost)) < 0) {
				// this is a bad way to estimate, it rounds up so we'll usually say less than what's really possible
				BigDecimal max = dEMC.divide(BigDecimal.valueOf(getFluidCostPer()), RoundingMode.FLOOR);
				maxDrain = Util.safeIntValue(max);
				if(!isFinal &&maxDrain > remainingFluid) maxDrain = remainingFluid;
				if(maxDrain < 1) return 0;
				cost = getFluidCost(maxDrain);
			}
			if (transaction != null) {
				//Note: The journal runs when the transaction commits, so it has to capture the values as they are now,
				// not whatever the local variables may hold later on
				final int drained = maxDrain;
				final long emcCost = cost;
				new RootCommitJournal(() -> {
					if(!isFinal) remainingFluid -= drained;
					markDirty();
					if(!isFreeFluid()) {
						provider.setEmc(emc.subtract(BigInteger.valueOf(emcCost)));
						provider.syncEmc(Objects.requireNonNull(Util.getPlayer(owner)));
					}
				}).updateSnapshots(transaction);
			} else {
				if(!isFinal) remainingFluid -= maxDrain;
				markDirty();
				if(!isFreeFluid()) {
					provider.setEmc(emc.subtract(BigInteger.valueOf(cost)));
					provider.syncEmc(Objects.requireNonNull(Util.getPlayer(owner)));
				}
			}
			return maxDrain;
		}
	}

	FluidHandler getFluidHandlerCapability() {
		return (FluidHandler) WorldHelper.getCapability(level, Capabilities.Fluid.BLOCK, worldPosition, getBlockState(), this, null);
	}
}
