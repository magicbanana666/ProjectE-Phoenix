package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Predicate;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

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

	public static final Predicate<ItemStack> FURNACE_FUEL = input -> EMC_HOLDER.test(input) || getBurnTime(input) > 0;

	public static final Predicate<ItemStack> MERCURIAL_TARGET = input -> {
		if (input.isEmpty()) {
			return false;
		}
		BlockState state = ItemHelper.stackToState(input, null);
		return state != null && !state.hasBlockEntity() && IEMCProxy.INSTANCE.hasValue(input);
	};

	private static int getBurnTime(ItemStack input) {
		FuelValues fuelValues = getFuelValues();
		return fuelValues == null ? 0 : input.getBurnTime(RecipeType.SMELTING, fuelValues);
	}

	@Nullable
	private static FuelValues getFuelValues() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null) {
			return server.overworld().fuelValues();
		}
		if (FMLEnvironment.getDist().isClient()) {
			Level level = ClientLevelHelper.getLevel();
			if (level != null) {
				return level.fuelValues();
			}
		}
		return null;
	}

	//Note: Kept in a separate class so that loading SlotPredicates on a dedicated server does not try to load ClientLevel
	private static class ClientLevelHelper {

		@Nullable
		public static Level getLevel() {
			return Minecraft.getInstance().level;
		}
	}

	private SlotPredicates() {
	}
}