package moze_intel.projecte.emc.mappers;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidType;

@EMCMapper
public class BrewingMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		//Add conversion for empty bottle + water to water bottle
		mapper.addConversion(1, NSSItem.createItem(PotionContents.createItemStack(Items.POTION, Potions.WATER)), EMCHelper.intMapOf(
				NSSItem.createItem(Items.GLASS_BOTTLE), 1,
				NSSFluid.createTag(FluidTags.WATER), FluidType.BUCKET_VOLUME / 3
		));

		//26.3: PotionBrewing and NeoForge's brewing recipe registry were replaced by minecraft:brewing recipes
		List<Holder<Potion>> potions = registryAccess.lookupOrThrow(Registries.POTION).listElements().map(potion -> (Holder<Potion>) potion).toList();
		int recipeCount = 0;
		for (RecipeHolder<BrewingRecipe> recipeHolder : serverResources.getRecipeManager().recipeMap().byType(RecipeType.BREWING)) {
			BrewingRecipe recipe = recipeHolder.value();
			List<ItemStack> validInputs = getMatchingStacks(recipe.getInput(), potions);
			List<ItemStack> validReagents = getMatchingStacks(recipe.getReagent(), potions);
			if (validInputs.isEmpty() || validReagents.isEmpty()) {
				continue;
			}
			ItemStack output = recipe.getOutput().create();
			if (output.isEmpty()) {
				continue;
			}
			NormalizedSimpleStack nssOut = NSSItem.createItem(output);
			for (ItemStack validInput : validInputs) {
				NormalizedSimpleStack nssInput = NSSItem.createItem(validInput);
				for (ItemStack validReagent : validReagents) {
					//Add the conversion, 3 input + x reagent = 3 y output as strictly speaking the only one of the three parts
					// in the recipe that are required to be one in stack size is the input
					mapper.addConversion(3 * output.getCount(), nssOut, EMCHelper.intMapOf(
							nssInput, 3,
							NSSItem.createItem(validReagent), validReagent.getCount()
					));
					recipeCount++;
				}
			}
		}

		PECore.debugLog("{} Statistics:", getName());
		PECore.debugLog("Found {} Brewing Recipes", recipeCount);
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_BREWING_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_BREWING_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_BREWING_MAPPER.tooltip();
	}

	private static List<ItemStack> getMatchingStacks(PotionIngredient ingredient, List<Holder<Potion>> potions) {
		List<ItemStack> stacks = new ArrayList<>();
		ingredient.ingredient().items().forEach(item -> {
			if (ingredient.potions().isEmpty()) {
				stacks.add(new ItemStack(item));
			} else {
				//Expand the potion predicate to every potion that it accepts
				PotionsPredicate predicate = ingredient.potions().get();
				for (Holder<Potion> potion : potions) {
					PotionContents contents = new PotionContents(potion);
					if (predicate.matches(contents)) {
						ItemStack stack = new ItemStack(item);
						stack.set(DataComponents.POTION_CONTENTS, contents);
						stacks.add(stack);
					}
				}
			}
		});
		return stacks;
	}
}
