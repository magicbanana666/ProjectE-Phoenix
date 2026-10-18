package moze_intel.projecte.expansion.item;

import moze_intel.projecte.expansion.util.Fuel;
import moze_intel.projecte.gameObjs.registries.PEContextIntProviders;
import net.minecraft.world.item.Item;

public class ItemFuel extends Item  {
	private final Fuel level;
	public ItemFuel(Properties properties, Fuel level) {
		//Since 26.1 the registry id has to be on the properties before the item is created, and BlockDeferredRegister
		//hands them in already carrying it
		//Since 26.3 burn times are a data component, the addon fuels burn as long as aeternalis fuel
		super(properties.rarity(level.getRarity()).cookingFuel(PEContextIntProviders.COOKING_TIME_AETERNALIS_FUEL));
		this.level = level;
	}
}
