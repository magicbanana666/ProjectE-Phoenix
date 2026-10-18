package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalBarrelBlockEntity;
import moze_intel.projecte.gameObjs.container.slots.InventoryContainerSlot;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class AlchemicalBarrelContainer extends PEContainer {

	private final AlchemicalBarrelBlockEntity blockEntity;

	public AlchemicalBarrelContainer(int windowId, Inventory playerInv, AlchemicalBarrelBlockEntity barrel) {
		super(PEContainerTypes.ALCHEMICAL_BARREL_CONTAINER, windowId, playerInv);
		this.blockEntity = barrel;
		this.blockEntity.startOpen(playerInv.player);
		IItemHandler inv = this.blockEntity.getInventory(null);
		//Barrel Inventory
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 13; j++) {
				this.addSlot(new InventoryContainerSlot(inv, j + i * 13, 12 + j * 18, 5 + i * 18));
			}
		}
		addPlayerInventory(48, 152);
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return Container.stillValidBlockEntity(blockEntity, player);
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		blockEntity.stopOpen(player);
	}

	public boolean blockEntityMatches(AlchemicalBarrelBlockEntity barrel) {
		return barrel == blockEntity;
	}
}
