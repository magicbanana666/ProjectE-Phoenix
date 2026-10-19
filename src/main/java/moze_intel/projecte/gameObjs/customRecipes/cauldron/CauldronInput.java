package moze_intel.projecte.gameObjs.customRecipes.cauldron;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * Everything about an Alchemical Cauldron a recipe can ask for: the items in it, what it is filled with, its heat, which of
 * its circles are drawn and the souls it holds.
 *
 * @param circles how many of its circles are drawn, counted from the smallest outwards without gaps
 */
public record CauldronInput(List<ItemStack> contents, CauldronLiquid liquid, CauldronHeat heat, int circles, int souls) implements RecipeInput {

	@NotNull
	@Override
	public ItemStack getItem(int index) {
		return contents.get(index);
	}

	@Override
	public int size() {
		return contents.size();
	}
}
