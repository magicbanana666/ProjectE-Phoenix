package moze_intel.projecte.expansion.block.entity;

import java.util.HashMap;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.event.PlayerAttemptCondenserSetEvent;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.expansion.client.HitDirectionSource;
import moze_intel.projecte.expansion.gui.container.ContainerCondenserMK3Input;
import moze_intel.projecte.expansion.gui.container.ContainerCondenserMK3Output;
import moze_intel.projecte.expansion.registries.ExpansionBlockEntityTypes;
import moze_intel.projecte.expansion.util.IChestLike;
import moze_intel.projecte.expansion.util.TagNames;
import moze_intel.projecte.expansion.util.Util;
import moze_intel.projecte.gameObjs.block_entities.ItemHandlerResourceAdapter;
import moze_intel.projecte.gameObjs.block_entities.WrappedItemHandler;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.inventory.ItemHandlerHelper;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class BlockEntityCondenserMK3 extends BlockEntityBase implements IChestLike, MenuProvider {
	//26.1 slots and the exposed automation views stay on the legacy IItemHandler, the capability itself is a
	//ResourceHandler<ItemResource> again, so the sided views get adapted on the way out
	public static final ICapabilityProvider<BlockEntityCondenserMK3, @Nullable Direction, ResourceHandler<ItemResource>> ITEM_HANDLER_CAPABILITY = (be, side) -> ItemHandlerResourceAdapter.of(be.getAutomationSidedItemHandler(side));
	public static final ICapabilityProvider<BlockEntityCondenserMK3, @Nullable Direction, IEmcStorage> EMC_STORAGE_PROVIDER = BlockEntityCondenserMK3::getSidedHandler;
	public static final Direction OUTPUT_DIRECTION = Direction.DOWN;
	private static final int INPUT_SIZE = 91;
	private static final int OUTPUT_SIZE = 180;
	private final HashMap<Direction, SidedHandler> handlers = new HashMap<>();
	private final ChestLidController lidController = new ChestLidController();

	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F,
				level.getRandom().nextFloat() * 0.1F + 0.9F);
		}

		@Override
		protected void onClose(Level level, BlockPos pos, BlockState state) {
			level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F,
				level.getRandom().nextFloat() * 0.1F + 0.9F);
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int openCount) {
			level.blockEvent(pos, state.getBlock(), 1, openCount);
		}

		@Override
		public boolean isOwnContainer(Player player) {
			return player.containerMenu instanceof ContainerCondenserMK3Input input && input.blockEntityMatches(BlockEntityCondenserMK3.this) ||
					player.containerMenu instanceof ContainerCondenserMK3Output output && output.blockEntityMatches(BlockEntityCondenserMK3.this);
		}
	};

	public BlockEntityCondenserMK3(BlockPos pos, BlockState state) {
		super(ExpansionBlockEntityTypes.CONDENSER_MK3.get(), pos, state);
		for (Direction dir : Direction.values()) {
			handlers.put(dir, createSidedHandler(dir));
		}
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, ExpansionBlockEntityTypes.CONDENSER_MK3.get(), ITEM_HANDLER_CAPABILITY);
		event.registerBlockEntity(PECapabilities.EMC_STORAGE_CAPABILITY, ExpansionBlockEntityTypes.CONDENSER_MK3.get(), EMC_STORAGE_PROVIDER);
	}

	protected SidedHandler createSidedHandler(Direction direction) {
		boolean isOutput = direction == OUTPUT_DIRECTION;
		StackHandler inventory = new StackHandler(isOutput ? OUTPUT_SIZE : INPUT_SIZE);
		return new SidedHandler(direction, inventory, isOutput);
	}


	public SidedHandler getSidedHandler(@Nullable Direction direction) {
		if (direction == null) direction = Direction.UP;
		SidedHandler handler = handlers.get(direction);
		if (handler == null) {
			handler = createSidedHandler(direction);
			this.handlers.put(direction, handler);
		}

		return handler;
	}

	public SidedHandler getOutput() {
		return getSidedHandler(OUTPUT_DIRECTION);
	}

	protected StackHandler getOutputHandler() {
		return getOutput().getInventory();
	}

	public WrappedItemHandler getAutomationSidedItemHandler(@Nullable Direction direction) {
		if (direction == null) direction = Direction.UP;

		SidedHandler handler = getSidedHandler(direction);
		return handler.getAutomationInventory();
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (blockEntity instanceof BlockEntityCondenserMK3 be) be.tickServer(level, pos, state);
	}

	@SuppressWarnings("unused")
	public void tickServer(Level level, BlockPos pos, BlockState state) {
		for (Direction direction : Direction.values()) {
			if (direction == OUTPUT_DIRECTION) {
				continue;
			}

			SidedHandler handler = getSidedHandler(direction);
			handler.checkLockAndUpdate(false);
			handler.displayEmc = handler.getStoredEmc();
			if (handler.getLockInfo() != null) {
				handler.condense();
			}
		}
		updateComparators(level, pos);
	}



	@SuppressWarnings("unused")
	public static void tickClient(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if(blockEntity instanceof BlockEntityCondenserMK3 be) {
			be.lidController.tickLid();
		}
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		if (id == 1) {
			lidController.shouldBeOpen(type > 0);
			return true;
		}
		return super.triggerEvent(id, type);
	}

	/**
	 * 26.1 replaced {@code Block#onRemove} with {@link BlockEntity#preRemoveSideEffects}
	 */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		for (Direction direction : Direction.values()) {
			WorldHelper.dropInventory(getAutomationSidedItemHandler(direction), level, pos);
		}
		super.preRemoveSideEffects(pos, state);
	}

	public void startOpen(Player player) {
		if (!isRemoved() && !player.isSpectator() && level != null) {
			//26.1 incrementOpeners additionally takes the maximum interaction range
			openersCounter.incrementOpeners(player, level, getBlockPos(), getBlockState(), player.getContainerInteractionRange());
		}
	}

	public void stopOpen(Player player) {
		if (!isRemoved() && !player.isSpectator() && level != null) {
			openersCounter.decrementOpeners(player, level, getBlockPos(), getBlockState());
		}
	}

	public void recheckOpen() {
		if (!isRemoved() && level != null) {
			openersCounter.recheckOpeners(level, getBlockPos(), getBlockState());
		}
	}

	@Override
	public float getOpenNess(float partialTicks) {
		return lidController.getOpenness(partialTicks);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		for (Direction dir : Direction.values()) {
			SidedHandler handler = handlers.get(dir);
			handler.load(input.childOrEmpty(Util.ucwords(dir.getName())));
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		for (Direction dir : Direction.values()) {
			SidedHandler handler = handlers.get(dir);
			handler.save(output.child(Util.ucwords(dir.getName())));
		}
	}

	@Override
	public Component getDisplayName() {
		return TextComponentUtil.build(PEBlocks.CONDENSER);
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		//The side the player is looking at can only be known on the client, the common side reads it through the
		//client installed hook instead of Minecraft#hitResult
		Direction direction = HitDirectionSource.getDirection();

		if (direction == OUTPUT_DIRECTION) {
			return new ContainerCondenserMK3Output(windowId, inventory, this);
		}

		return new ContainerCondenserMK3Input(windowId, inventory, this);
	}

	public class SidedHandler implements IEmcStorage {
		@SuppressWarnings({"FieldCanBeLocal", "unused"})
		private final Direction direction;
		private final StackHandler inventory;
		private final WrappedItemHandler automationInventory;
		private final boolean isOutput;
		private @Nullable ItemInfo lockInfo = null;
		private boolean isAcceptingEmc;
		//Start at one less than actual just to ensure we run initially after loading
		private int loadIndex = EMCMappingHandler.getLoadIndex() - 1;
		private long emc;
		public long displayEmc;
		public long requiredEmc;

		protected SidedHandler(Direction direction, StackHandler inventory, boolean isOutput) {
			this.direction = direction;
			this.inventory = inventory;
			this.isOutput = isOutput;
			this.automationInventory = createAutomationInventory();
		}

		public StackHandler getInventory() {
			return inventory;
		}
		public WrappedItemHandler getAutomationInventory() {
			return automationInventory;
		}

		public void setLockInfoFromPacket(@Nullable ItemInfo lockInfo) {
			this.lockInfo = lockInfo;
		}

		public boolean attemptCondenserSet(Player player) {
			return level != null && attemptCondenserSet(level, worldPosition, player);
		}

		private boolean attemptCondenserSet(Level level, BlockPos pos, Player player) {
			if (level.isClientSide()) {
				return false;
			}
			if (getLockInfo() == null) {
				ItemStack stack = player.containerMenu.getCarried();
				if (!stack.isEmpty()) {
					ItemInfo sourceInfo = ItemInfo.fromStack(stack);
					ItemInfo reducedInfo = IEMCProxy.INSTANCE.getPersistentInfo(sourceInfo);
					if (!NeoForge.EVENT_BUS.post(new PlayerAttemptCondenserSetEvent(player, sourceInfo, reducedInfo)).isCanceled()) {
						lockInfo = reducedInfo;
						checkLockAndUpdate(true);
						markDirty(level, pos, false);
						return true;
					}
					return false;
				}
				//If the lock item is actually null and the player didn't carry anything don't do anything
				// otherwise just fall through as we need to update it to actually being empty
				if (lockInfo == null) {
					return false;
				}
			}
			lockInfo = null;
			checkLockAndUpdate(true);
			markDirty(level, pos, false);
			return true;
		}

		public @Nullable ItemInfo getLockInfo() {
			return lockInfo;
		}

		public boolean isStackEqualToLock(ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			ItemInfo lockInfo = getLockInfo();
			if (lockInfo == null) {
				return false;
			}
			//Compare our lock to the persistent item that the stack would have
			return lockInfo.equals(IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(stack)));
		}

		protected WrappedItemHandler createAutomationInventory() {
			if (isOutput) {
				return new WrappedItemHandler(inventory, WrappedItemHandler.WriteMode.OUT);
			}

			return new WrappedItemHandler(inventory, WrappedItemHandler.WriteMode.IN) {
				@Override
				public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
					return SlotPredicates.HAS_EMC.test(stack) && !isStackEqualToLock(stack) ? super.insertItem(slot, stack, simulate) : stack;
				}
			};
		}

		@SuppressWarnings("SameReturnValue")
		protected boolean emcAffectsComparators() {
			return true;
		}

		protected boolean hasSpace() {
			IItemHandler output = getOutputHandler();
			for (int i = 0, slots = output.getSlots(); i < slots; i++) {
				ItemStack stack = output.getStackInSlot(i);
				if (stack.isEmpty() || (isStackEqualToLock(stack) && stack.getCount() < stack.getMaxStackSize())) {
					return true;
				}
			}
			return false;
		}

		protected final void pushStack() {
			ItemInfo lockInfo = getLockInfo();
			IItemHandler output = getOutputHandler();
			if (lockInfo != null) {
				ItemHandlerHelper.insertItemStacked(output, lockInfo.createStack(), false);
			}
		}

		public void condense() {
			while (this.hasSpace() && this.getStoredEmc() >= requiredEmc) {
				pushStack();
				forceExtractEmc(requiredEmc, EmcAction.EXECUTE);
			}
			if (this.hasSpace()) {
				StackHandler input = getInventory();
				for (int i = 0, slots = input.getSlots(); i < slots; i++) {
					ItemStack stack = input.getStackInSlot(i);
					if (!stack.isEmpty()) {
						forceInsertEmc(IEMCProxy.INSTANCE.getSellValue(stack) * stack.getCount(), EmcAction.EXECUTE);
						input.setStackInSlot(i, ItemStack.EMPTY);
						break;
					}
				}
			}
		}

		@Override
		public long getStoredEmc() {
			return emc;
		}

		@Override
		public long getMaximumEmc() {
			return Long.MAX_VALUE;
		}

		@SuppressWarnings("SameReturnValue")
		public boolean canProvideEmc() {
			return false;
		}

		public boolean canAcceptEmc() {
			return isAcceptingEmc;
		}

		@Override
		public long extractEmc(long toExtract, EmcAction action) {
			if (toExtract < 0) {
				return insertEmc(-toExtract, action);
			}
			if (canProvideEmc()) {
				return forceExtractEmc(Math.min(getStoredEmc(), toExtract), action);
			}
			return 0;
		}

		@Override
		public long insertEmc(long toAccept, EmcAction action) {
			if (toAccept < 0) {
				return extractEmc(-toAccept, action);
			}
			if (canAcceptEmc()) {
				return forceInsertEmc(Math.min(getNeededEmc(), toAccept), action);
			}
			return 0;
		}

		protected long forceExtractEmc(long toExtract, EmcAction action) {
			if (toExtract < 0) {
				return forceInsertEmc(-toExtract, action);
			}
			long toRemove = Math.min(getStoredEmc(), toExtract);
			if (action.execute()) {
				emc -= toRemove;
				storedEmcChanged();
			}
			return toRemove;
		}

		protected long forceInsertEmc(long toAccept, EmcAction action) {
			if (toAccept < 0) {
				return forceExtractEmc(-toAccept, action);
			}
			long toAdd = Math.min(getNeededEmc(), toAccept);
			if (action.execute()) {
				emc += toAdd;
				storedEmcChanged();
			}
			return toAdd;
		}


		protected void storedEmcChanged() {
			if (level != null) {
				markDirty(level, worldPosition, emcAffectsComparators());
			}
		}

		private void checkLockAndUpdate(boolean force) {
			if (!force && loadIndex == EMCMappingHandler.getLoadIndex()) {
				// Only update if we are forcing it or are on a different load index
				return;
			}
			loadIndex = EMCMappingHandler.getLoadIndex();
			ItemInfo lockInfo = getLockInfo();
			if (lockInfo != null) {
				long lockEmc = IEMCProxy.INSTANCE.getValue(lockInfo);
				if (lockEmc > 0) {
					if (requiredEmc != lockEmc) {
						requiredEmc = lockEmc;
						isAcceptingEmc = true;
					}
					return;
				}
				//Don't reset the lockInfo just because it has no EMC, as if a reload makes it have EMC again
				// then we want to allow it to happen again
			}
			displayEmc = 0;
			requiredEmc = 0;
			isAcceptingEmc = false;
		}

		public void load(ValueInput input) {
			if (isOutput) {
				input.readChild(TagNames.OUTPUT, getInventory());
			} else {
				input.readChild(TagNames.INPUT, getInventory());
				lockInfo = input.read(TagNames.LOCK, ItemInfo.CODEC).orElse(null);
				emc = input.getLongOr(TagNames.STORED_EMC, 0);
			}
		}

		public void save(ValueOutput output) {
			if (isOutput) {
				output.putChild(TagNames.OUTPUT, getInventory());
			} else {
				output.putChild(TagNames.INPUT, getInventory());
				if (lockInfo != null) {
					output.store(TagNames.LOCK, ItemInfo.CODEC, lockInfo);
				}
				output.putLong(TagNames.STORED_EMC, emc);
			}
		}
	}
}
