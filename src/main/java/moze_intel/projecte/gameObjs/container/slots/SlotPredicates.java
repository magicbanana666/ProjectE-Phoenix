package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Predicate;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class SlotPredicates {

	public static final Predicate<ItemStack> ALWAYS_FALSE = input -> false;

	public static final Predicate<ItemStack> HAS_EMC = IEMCProxy.INSTANCE::hasValue;

	public static final Predicate<ItemStack> COLLECTOR_LOCK = FuelMapper::isStackFuel;

	public static final Predicate<ItemStack> COLLECTOR_INV = input -> input.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY) != null ||
																	  (FuelMapper.isStackFuel(input) && !FuelMapper.isStackMaxFuel(input));

	// slotrelayklein, slotmercurialklein
	public static final Predicate<ItemStack> EMC_HOLDER = input -> input.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY) != null;

	// slotrelayinput
	public static final Predicate<ItemStack> RELAY_INV = input -> EMC_HOLDER.test(input) || HAS_EMC.test(input);

	public static final Predicate<ItemStack> FURNACE_FUEL = input -> EMC_HOLDER.test(input) || isFuel(input);

	public static final Predicate<ItemStack> MERCURIAL_TARGET = input -> {
		if (input.isEmpty()) {
			return false;
		}
		BlockState state = ItemHelper.stackToState(input, null);
		return state != null && !state.hasBlockEntity() && IEMCProxy.INSTANCE.hasValue(input);
	};

	//26.3: Burn times are resolved from the cooking fuel component when the fuel is used, so like vanilla furnaces any item with the component is accepted
	private static boolean isFuel(ItemStack input) {
		return input.has(DataComponents.COOKING_FUEL);
	}

	private SlotPredicates() {
	}
}