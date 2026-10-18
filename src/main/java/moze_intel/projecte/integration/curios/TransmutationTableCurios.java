package moze_intel.projecte.integration.curios;

import java.util.Optional;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class TransmutationTableCurios {
    public static final String MOD_ID = "curios";
    public static final String TABLET_SLOT_ID = "transmutation_tablet";

    public static boolean modLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static Optional<IItemHandlerModifiable> getCuriosInventory(Player player) {
        if (modLoaded()) return Integrator.getCuriosInventory(player);
        return Optional.empty();
    }

    private static final class Integrator {
        private static Optional<IItemHandlerModifiable> getCuriosInventory(Player player) {
            Optional<ICuriosItemHandler> curiosItemHandler = CuriosApi.getCuriosInventory(player);
            if (curiosItemHandler.isEmpty())  return Optional.empty();
            Optional<ICurioStacksHandler> itemHandler = curiosItemHandler.get().getStacksHandler(TABLET_SLOT_ID);
            //Curios 17 moved its stack handler to the transfer API, but it still offers the slot based methods
            return itemHandler.map(handler -> new CuriosStacks(handler.getStacks()));
        }
    }

    private record CuriosStacks(IDynamicStackHandler stacks) implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            stacks.setStackInSlot(slot, stack);
        }

        @Override
        public int getSlots() {
            return stacks.getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return stacks.getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stacks.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return stacks.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return stacks.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stacks.isItemValid(slot, stack);
        }
    }
}
