package moze_intel.projecte.emc.mappers.recipe;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronLiquid;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Gives what the Alchemical Cauldron brews the worth of its ingredients. Water, heat and souls are free. A liquid other than
 * water, such as Red Water, is worth what it was brewed from, and so passes its worth on to what is brewed in it.
 */
@RecipeTypeMapper
public class CauldronRecipeMapper implements IRecipeTypeMapper {

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_CAULDRON.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_CAULDRON.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_CAULDRON.tooltip();
	}

	@Override
	public boolean canHandle(RecipeType<?> recipeType) {
		return recipeType == PERecipeTypes.CAULDRON.get();
	}

	private static NormalizedSimpleStack liquid(CauldronLiquid liquid) {
		return NSSFake.create("Alchemical Cauldron filled with " + liquid.getSerializedName());
	}

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess,
			INSSFakeGroupManager fakeGroupManager) {
		if (!(recipeHolder.value() instanceof CauldronRecipe recipe)) {
			return false;
		}
		Object2IntMap<NormalizedSimpleStack> ingredients = new Object2IntOpenHashMap<>();
		for (SizedIngredient sized : recipe.ingredients()) {
			Set<NormalizedSimpleStack> options = new HashSet<>();
			sized.ingredient().items().forEach(item -> options.add(NSSItem.createItem(new ItemStack(item))));
			if (options.isEmpty()) {
				return true;
			}
			NormalizedSimpleStack stack = options.size() == 1 ? options.iterator().next() : fakeGroupManager.getOrCreateFakeGroup(options).dummy();
			ingredients.mergeInt(stack, sized.count(), Integer::sum);
		}
		boolean keepsLiquid = recipe.resultLiquid().isEmpty() || recipe.resultLiquid().get() == recipe.liquid();
		if (recipe.liquid() != CauldronLiquid.WATER && !keepsLiquid) {
			//The liquid is used up by the brew
			ingredients.mergeInt(liquid(recipe.liquid()), 1, Integer::sum);
		}
		recipe.result().ifPresent(result -> {
			ItemStack stack = result.create();
			mapper.addConversion(stack.getCount(), NSSItem.createItem(stack), ingredients);
		});
		recipe.resultLiquid().ifPresent(result -> {
			if (result != CauldronLiquid.EMPTY && result != CauldronLiquid.WATER && result != recipe.liquid() && recipe.result().isEmpty()) {
				mapper.addConversion(1, liquid(result), ingredients);
			}
		});
		return true;
	}
}
