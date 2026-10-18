package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.expansion.util.InfiniteFuelBurnTime;
import moze_intel.projecte.gameObjs.registration.DeferredCodecHolder;
import moze_intel.projecte.gameObjs.registration.DeferredCodecRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

/**
 * Since 26.3 furnace burn times are {@link ContextIntProvider}s that are referenced by the cooking fuel data component, the providers themselves are data driven
 * and live in {@code data/projecte/context_int_provider}.
 */
public class PEContextIntProviders {

	private PEContextIntProviders() {
	}

	public static final DeferredCodecRegister<ContextIntProvider> PROVIDER_TYPES = new DeferredCodecRegister<>(Registries.CONTEXT_INT_PROVIDER_TYPE, PECore.MODID);

	public static final DeferredCodecHolder<ContextIntProvider, InfiniteFuelBurnTime> INFINITE_FUEL_TYPE = PROVIDER_TYPES.registerUnit("infinite_fuel", () -> InfiniteFuelBurnTime.INSTANCE);

	public static final ResourceKey<ContextIntProvider> COOKING_TIME_ALCHEMICAL_COAL = createKey("cooking/time_alchemical_coal");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_ALCHEMICAL_COAL_BLOCK = createKey("cooking/time_alchemical_coal_block");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_MOBIUS_FUEL = createKey("cooking/time_mobius_fuel");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_MOBIUS_FUEL_BLOCK = createKey("cooking/time_mobius_fuel_block");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_AETERNALIS_FUEL = createKey("cooking/time_aeternalis_fuel");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_AETERNALIS_FUEL_BLOCK = createKey("cooking/time_aeternalis_fuel_block");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_INFINITE_FUEL = createKey("cooking/time_infinite_fuel");

	private static ResourceKey<ContextIntProvider> createKey(String name) {
		return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, PECore.rl(name));
	}
}
