package moze_intel.projecte.expansion.item;

import moze_intel.projecte.expansion.util.Fuel;
import moze_intel.projecte.gameObjs.registries.PEContextIntProviders;
import net.minecraft.world.item.BlockItem;

import java.util.Objects;

public class FuelBlockItem extends BlockItem {
	private final Fuel level;
	public FuelBlockItem(Fuel level, Properties properties) {
		//Since 26.1 the registry id has to be on the properties before the item is created, and BlockDeferredRegister
		//hands them in already carrying it
		//Since 26.3 burn times are a data component, the addon fuel blocks burn as long as aeternalis fuel blocks (nine times the fuel)
		super(Objects.requireNonNull(Objects.requireNonNull(level).getBlock()), properties.rarity(level.getRarity()).cookingFuel(PEContextIntProviders.COOKING_TIME_AETERNALIS_FUEL_BLOCK));
		this.level = level;
	}
}
