package moze_intel.projecte.gameObjs.customRecipes.athanor;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * What lies in the Athanor: the items on the dais, in any order, and the catalyst in the centre.
 * The catalyst is the last item of the input.
 */
public record AthanorInput(List<ItemStack> inputs, ItemStack catalyst) implements RecipeInput {

	@NotNull
	@Override
	public ItemStack getItem(int index) {
		return index == inputs.size() ? catalyst : inputs.get(index);
	}

	@Override
	public int size() {
		return inputs.size() + 1;
	}
}
