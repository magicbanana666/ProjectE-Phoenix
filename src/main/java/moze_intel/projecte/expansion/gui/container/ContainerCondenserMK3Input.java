package moze_intel.projecte.expansion.gui.container;

import java.util.Objects;
import java.util.function.Predicate;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.expansion.block.entity.BlockEntityCondenserMK3;
import moze_intel.projecte.expansion.client.HitDirectionSource;
import moze_intel.projecte.expansion.net.packets.to_client.PacketUpdateCondenserLock;
import moze_intel.projecte.expansion.registries.ExpansionMenus;
import moze_intel.projecte.gameObjs.container.PEContainer;
import moze_intel.projecte.gameObjs.container.slots.SlotCondenserLock;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerCondenserMK3Input extends ContainerBase {
	public static final int MAX_PROGRESS = 102;

	public final PEContainer.BoxedLong displayEmc = new PEContainer.BoxedLong();
	public final PEContainer.BoxedLong requiredEmc = new PEContainer.BoxedLong();
	private @Nullable ItemInfo lastLockInfo;
	private final BlockEntityCondenserMK3.SidedHandler handler;
	final BlockEntityCondenserMK3 blockEntity;
	public ContainerCondenserMK3Input(int windowId, Inventory playerInv, BlockEntityCondenserMK3 blockEntity) {
		this(ExpansionMenus.CONDENSER_MK3_INPUT.get(), windowId, playerInv, blockEntity);
	}

	public ContainerCondenserMK3Input(MenuType<?> type, int windowId, Inventory playerInv, BlockEntityCondenserMK3 blockEntity) {
		super(type, windowId, playerInv);
		this.blockEntity = blockEntity;
		this.handler = getHandler();
		blockEntity.startOpen(this.playerInv.player);
		this.longFields.add(displayEmc);
		this.longFields.add(requiredEmc);
		initSlots();
	}

	protected @Nullable Direction getDirection() {
		//Note: this used to read Minecraft#hitResult directly, which is impossible here: menus are common code and are
		//also built on a dedicated server, where the client classes do not even exist. The client installs the source
		return HitDirectionSource.getDirection();
	}

	protected BlockEntityCondenserMK3.SidedHandler getHandler() {
		return blockEntity.getSidedHandler(getDirection());
	}

	protected void initSlots() {
		this.addSlot(new SlotCondenserLock(handler::getLockInfo, 0, 12, 6));
		Predicate<ItemStack> validator = s -> SlotPredicates.HAS_EMC.test(s) && !handler.isStackEqualToLock(s);
		IItemHandler inventory = handler.getInventory();
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 13; j++) {
				this.addSlot(new ValidatedSlot(inventory,  j + i * 13, 12 + j * 18, 26 + i * 18, validator));
			}
		}
		addPlayerInventory(48, 154);
	}

	@Override
	protected void broadcastPX(boolean all) {
		this.displayEmc.set(handler.displayEmc);
		this.requiredEmc.set(handler.requiredEmc);
		ItemInfo lockInfo = handler.getLockInfo();
		if (all || !Objects.equals(lockInfo, lastLockInfo)) {
			lastLockInfo = lockInfo;
			syncDataChange(new PacketUpdateCondenserLock((short) containerId, lockInfo));
		}
		super.broadcastPX(all);
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		blockEntity.stopOpen(player);
	}

	public boolean blockEntityMatches(BlockEntityCondenserMK3 be) {
		return blockEntity == be;
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return ContainerBase.stillValid(player, blockEntity, () -> blockEntity.getBlockState().getBlock());
	}

	@Override
	public void clicked(int slot, int button, @NotNull ContainerInput flag, @NotNull Player player) {
		if (slot == 0) {
			if (handler.attemptCondenserSet(player)) {
				this.broadcastChanges();
			}
		} else {
			super.clicked(slot, button, flag, player);
		}
	}

	public int getProgressScaled() {
		if (requiredEmc.get() == 0) {
			return 0;
		}
		if (displayEmc.get() >= requiredEmc.get()) {
			return MAX_PROGRESS;
		}
		return (int) (MAX_PROGRESS * ((double) displayEmc.get() / requiredEmc.get()));
	}

	public void updateLockInfo(@Nullable ItemInfo lockInfo) {
		handler.setLockInfoFromPacket(lockInfo);
	}
}
