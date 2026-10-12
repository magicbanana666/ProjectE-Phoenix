package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.customRecipes.PERecipeSerializer;
import moze_intel.projecte.gameObjs.customRecipes.PEShieldSpecialRecipe;
import moze_intel.projecte.gameObjs.customRecipes.PhiloStoneSmeltingRecipe;
import moze_intel.projecte.gameObjs.customRecipes.RecipeShapelessKleinStar;
import moze_intel.projecte.gameObjs.customRecipes.RecipesCovalenceRepair;
import moze_intel.projecte.gameObjs.registration.PEDeferredHolder;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class PERecipeSerializers {

	public static final PEDeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = new PEDeferredRegister<>(Registries.RECIPE_SERIALIZER, PECore.MODID);

	public static final PEDeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipesCovalenceRepair>> COVALENCE_REPAIR = RECIPE_SERIALIZERS.register("covalence_repair", () -> simpleSerializer(RecipesCovalenceRepair::new));
	public static final PEDeferredHolder<RecipeSerializer<?>, RecipeSerializer<PEShieldSpecialRecipe>> SHIELD_DECORATION = RECIPE_SERIALIZERS.register("shield_decoration", () -> simpleSerializer(PEShieldSpecialRecipe::new));
	public static final PEDeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeShapelessKleinStar>> KLEIN = RECIPE_SERIALIZERS.register("crafting_shapeless_kleinstar", () -> PERecipeSerializer.wrapped(RecipeShapelessKleinStar::new));
	public static final PEDeferredHolder<RecipeSerializer<?>, RecipeSerializer<PhiloStoneSmeltingRecipe>> PHILO_STONE_SMELTING = RECIPE_SERIALIZERS.register("philo_stone_smelting", () -> simpleSerializer(PhiloStoneSmeltingRecipe::new));

	/**
	 * Replacement for the removed {@code SimpleCraftingRecipeSerializer}: a codec-less serializer for recipes
	 * whose payload carries no data.
	 */
	private static <T extends Recipe<?>> RecipeSerializer<T> simpleSerializer(java.util.function.Supplier<T> factory) {
		//One shared instance: StreamCodec.unit only encodes the exact instance it was given, so a fresh one per decode breaks recipe sync
		T instance = factory.get();
		return new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance));
	}
}