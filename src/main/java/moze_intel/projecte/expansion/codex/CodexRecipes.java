package moze_intel.projecte.expansion.codex;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

/**
 * The recipes the Alchemist's Codex shows. Since 26.1 the client no longer gets every recipe, so the server sends the
 * crafting recipes and the rituals of the Athanor (see {@code PECore#dataPackSync}), and they are kept here.
 */
public final class CodexRecipes {

	private static RecipeMap recipes = RecipeMap.EMPTY;

	private CodexRecipes() {
	}

	public static void onRecipesReceived(RecipesReceivedEvent event) {
		recipes = event.getRecipeMap();
	}

	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		recipes = RecipeMap.EMPTY;
	}

	/**
	 * @return the displays of every crafting recipe that makes the item, shaped and shapeless
	 */
	public static List<RecipeDisplay> craftingFor(Item item, Level level) {
		List<RecipeDisplay> found = new ArrayList<>();
		for (RecipeHolder<CraftingRecipe> holder : recipes.byType(RecipeType.CRAFTING)) {
			for (RecipeDisplay display : holder.value().display()) {
				if (makes(display.result(), item, level)) {
					found.add(display);
				}
			}
		}
		return found;
	}

	public static List<AthanorRecipe> rituals() {
		return recipes.byType(PERecipeTypes.ATHANOR.get()).stream().map(RecipeHolder::value).toList();
	}

	public static List<AthanorRecipe> ritualsFor(Item item) {
		List<AthanorRecipe> found = new ArrayList<>();
		for (RecipeHolder<AthanorRecipe> holder : recipes.byType(PERecipeTypes.ATHANOR.get())) {
			if (holder.value().result().item().value() == item) {
				found.add(holder.value());
			}
		}
		return found;
	}

	private static boolean makes(SlotDisplay result, Item item, Level level) {
		try {
			return result.resolveForFirstStack(SlotDisplayContext.fromLevel(level)).is(item);
		} catch (RuntimeException e) {
			//A display of another mod that can not be resolved without more context
			return false;
		}
	}
}
