package moze_intel.projecte.emc.mappers.recipe;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import moze_intel.projecte.utils.Constants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Gives the result of a ritual of the Athanor the value of what went into it: the ingredients, the catalyst when the ritual
 * uses it up, and the personal EMC the stages cost. So burning the result gives back exactly what was paid for it.
 */
@RecipeTypeMapper
public class AthanorRecipeMapper implements IRecipeTypeMapper {

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_ATHANOR.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_ATHANOR.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_ATHANOR.tooltip();
	}

	@Override
	public boolean canHandle(RecipeType<?> recipeType) {
		return recipeType == PERecipeTypes.ATHANOR.get();
	}

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess,
			INSSFakeGroupManager fakeGroupManager) {
		if (!(recipeHolder.value() instanceof AthanorRecipe recipe)) {
			return false;
		} else if (recipe.forbidden()) {
			//A forbidden work makes nothing, whatever its result says
			return true;
		}
		List<Ingredient> used = new ArrayList<>(recipe.inputs());
		if (!recipe.keepCatalyst()) {
			used.add(recipe.catalyst());
		}
		Object2IntMap<NormalizedSimpleStack> ingredients = new Object2IntOpenHashMap<>();
		for (Ingredient ingredient : used) {
			Set<NormalizedSimpleStack> options = new HashSet<>();
			ingredient.items().forEach(item -> options.add(NSSItem.createItem(new ItemStack(item))));
			if (options.isEmpty()) {
				//An ingredient nothing matches, the ritual can never run
				return true;
			}
			NormalizedSimpleStack stack = options.size() == 1 ? options.iterator().next() : fakeGroupManager.getOrCreateFakeGroup(options).dummy();
			ingredients.mergeInt(stack, 1, Constants.INT_SUM);
		}
		long emc = (long) (recipe.totalEmc() * ProjectEConfig.server.ritual.emcMultiplier.get());
		if (emc > 0) {
			NormalizedSimpleStack paid = NSSFake.create("Athanor ritual " + recipeHolder.id().identifier());
			mapper.setValueBefore(paid, emc);
			ingredients.mergeInt(paid, 1, Constants.INT_SUM);
		}
		ItemStack result = recipe.result().create();
		mapper.addConversion(result.getCount(), NSSItem.createItem(result), ingredients);
		return true;
	}
}
