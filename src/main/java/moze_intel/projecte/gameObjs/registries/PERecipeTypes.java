package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import moze_intel.projecte.gameObjs.registration.PEDeferredHolder;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;

public class PERecipeTypes {

	public static final PEDeferredRegister<RecipeType<?>> RECIPE_TYPES = new PEDeferredRegister<>(Registries.RECIPE_TYPE, PECore.MODID);

	public static final PEDeferredHolder<RecipeType<?>, RecipeType<AthanorRecipe>> ATHANOR = RECIPE_TYPES.register("athanor", RecipeType::simple);
	public static final PEDeferredHolder<RecipeType<?>, RecipeType<CauldronRecipe>> CAULDRON = RECIPE_TYPES.register("cauldron", RecipeType::simple);
}
