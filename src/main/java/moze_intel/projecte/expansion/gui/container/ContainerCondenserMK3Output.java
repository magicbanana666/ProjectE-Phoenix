package moze_intel.projecte.expansion.gui.container;

import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.expansion.block.entity.BlockEntityCondenserMK3;
import moze_intel.projecte.expansion.registries.ExpansionMenus;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class ContainerCondenserMK3Output extends ContainerBase {
	private final BlockEntityCondenserMK3.SidedHandler handler;
	final BlockEntityCondenserMK3 blockEntity;
	public ContainerCondenserMK3Output(int windowId, Inventory playerInv, BlockEntityCondenserMK3 blockEntity) {
		this(ExpansionMenus.CONDENSER_MK3_OUTPUT.get(), windowId, playerInv, blockEntity);
	}

	public ContainerCondenserMK3Output(MenuType<?> type, int windowId, Inventory playerInv, BlockEntityCondenserMK3 blockEntity) {
		super(type, windowId, playerInv);
		this.blockEntity = blockEntity;
		this.handler = blockEntity.getOutput();
		blockEntity.startOpen(this.playerInv.player);
		initSlots();
	}

	protected void initSlots() {
		int rows = 9, columns = 20;
		IItemHandler inventory = handler.getInventory();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				this.addSlot(new ValidatedSlot(inventory,  j + i * columns, 12 + j * 18, 8 + i * 18, SlotPredicates.ALWAYS_FALSE));
			}
		}
		addPlayerInventory(111, 172);
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
}
