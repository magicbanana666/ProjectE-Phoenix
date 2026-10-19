package moze_intel.projecte.gameObjs;

import com.google.gson.JsonParseException;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorInput;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.customRecipes.athanor.OpusStage;
import moze_intel.projecte.gameObjs.customRecipes.athanor.RuneColor;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
class AthanorRecipeTest {

	private static AthanorRecipe philosophersStone(MinecraftServer server) {
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, PECore.rl("athanor/philosophers_stone"));
		RecipeHolder<?> holder = server.getRecipeManager().byKey(key).orElseThrow();
		Assertions.assertEquals(PERecipeTypes.ATHANOR.get(), holder.value().getType());
		return (AthanorRecipe) holder.value();
	}

	private static ItemStack falseStone() {
		return new ItemStack(PEItems.FALSE_PHILOSOPHERS_STONE.get());
	}

	@Test
	@DisplayName("The Philosopher's Stone ritual loads from the datapack with its four stages")
	void testPhilosophersStoneLoads(MinecraftServer server) {
		AthanorRecipe recipe = philosophersStone(server);
		Assertions.assertEquals(1, recipe.tier());
		Assertions.assertEquals(List.of(OpusStage.NIGREDO, OpusStage.ALBEDO, OpusStage.CITRINITAS, OpusStage.RUBEDO),
				recipe.stages().stream().map(stage -> stage.stage()).toList());
		//Without a rune in the json every stage uses the colour of its kind
		Assertions.assertEquals(RuneColor.CITRINITAS, recipe.stages().get(2).rune());
		Assertions.assertEquals(245_760, recipe.totalEmc());
		Assertions.assertTrue(recipe.stages().get(2).needs().isPresent());
		Assertions.assertEquals(PEItems.PHILOSOPHERS_STONE.get(), recipe.assemble(new AthanorInput(List.of(), ItemStack.EMPTY)).getItem());
	}

	@Test
	@DisplayName("The ritual matches its ingredients in any slot, and nothing else")
	void testMatching(MinecraftServer server) {
		AthanorRecipe recipe = philosophersStone(server);
		ItemStack star = new ItemStack(Items.NETHER_STAR);
		Assertions.assertTrue(recipe.matches(new AthanorInput(List.of(falseStone(), falseStone(), falseStone(), ItemStack.EMPTY, ItemStack.EMPTY), star), server.overworld()));
		Assertions.assertTrue(recipe.matches(new AthanorInput(List.of(ItemStack.EMPTY, falseStone(), ItemStack.EMPTY, falseStone(), falseStone()), star), server.overworld()));
		//Too few, too many, the wrong item or the wrong catalyst
		Assertions.assertFalse(recipe.matches(new AthanorInput(List.of(falseStone(), falseStone(), ItemStack.EMPTY), star), server.overworld()));
		Assertions.assertFalse(recipe.matches(new AthanorInput(List.of(falseStone(), falseStone(), falseStone(), falseStone()), star), server.overworld()));
		Assertions.assertFalse(recipe.matches(new AthanorInput(List.of(falseStone(), falseStone(), new ItemStack(Items.DIAMOND)), star), server.overworld()));
		Assertions.assertFalse(recipe.matches(new AthanorInput(List.of(falseStone(), falseStone(), falseStone()), new ItemStack(Items.DIAMOND)), server.overworld()));
	}

	@Test
	@DisplayName("Every ritual in the datapack loads, fits the Athanor and makes something only a ritual makes")
	void testAllRitualsLoad(MinecraftServer server) {
		List<AthanorRecipe> recipes = server.getRecipeManager().recipeMap().byType(PERecipeTypes.ATHANOR.get()).stream()
				.map(RecipeHolder::value)
				.toList();
		//The Philosopher's Stone, eleven coloured matters, six Klein Stars, Infinite Fuel, Infinite Steak and the two Final Stars
		Assertions.assertEquals(22, recipes.size());
		Assertions.assertEquals(5, recipes.stream().filter(recipe -> recipe.tier() == 2).count());
		for (AthanorRecipe recipe : recipes) {
			Assertions.assertTrue(recipe.inputs().size() <= (recipe.tier() == 1 ? AthanorRecipe.TIER_ONE_INPUTS : AthanorRecipe.MAX_INPUTS));
			Assertions.assertTrue(recipe.result().create().is(PETags.Items.RITUAL_ONLY), () -> recipe.result() + " is not in projecte:ritual_only");
		}
	}

	@Test
	@DisplayName("Upgrading a Klein Star keeps the EMC that is stored in it")
	void testKleinStarKeepsEmc(MinecraftServer server) {
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, PECore.rl("athanor/klein/zwei"));
		AthanorRecipe recipe = (AthanorRecipe) server.getRecipeManager().byKey(key).orElseThrow().value();
		ItemStack ein = new ItemStack(PEItems.KLEIN_STAR_EIN.get());
		ein.set(PEDataComponentTypes.STORED_EMC, 1234L);
		AthanorInput input = new AthanorInput(List.of(new ItemStack(Items.GHAST_TEAR), new ItemStack(PEItems.AETERNALIS_FUEL.get()), new ItemStack(Items.BLAZE_ROD)), ein);
		Assertions.assertTrue(recipe.matches(input, server.overworld()));
		ItemStack zwei = recipe.assemble(input);
		Assertions.assertTrue(zwei.is(PEItems.KLEIN_STAR_ZWEI.get()));
		Assertions.assertEquals(1234L, zwei.get(PEDataComponentTypes.STORED_EMC));
	}

	@Test
	@DisplayName("Rune colours can use the plain colour names, and more than three inputs need tier 2")
	void testParsing(MinecraftServer server) {
		AthanorRecipe recipe = CodecTestHelper.parseJson(server.registryAccess(), AthanorRecipe.CODEC.codec(), "athanor test", """
				{
					"inputs": ["minecraft:dirt", "#minecraft:logs"],
					"catalyst": "minecraft:diamond",
					"keep_catalyst": true,
					"stages": [
						{ "stage": "cauda_pavonis", "rune": "cyan", "ticks": 20 },
						{ "stage": "rubedo", "rune": "red", "ticks": 40, "emc": 10, "needs": "night" }
					],
					"result": { "id": "minecraft:emerald", "count": 2 }
				}""");
		Assertions.assertTrue(recipe.keepCatalyst());
		Assertions.assertEquals(RuneColor.CYAN, recipe.stages().get(0).rune());
		Assertions.assertEquals(RuneColor.RUBEDO, recipe.stages().get(1).rune());
		Assertions.assertEquals(2, recipe.result().count());
		Assertions.assertTrue(recipe.inputsMatch(List.of(new ItemStack(Items.OAK_LOG), new ItemStack(Items.DIRT))));
		Assertions.assertThrows(JsonParseException.class, () -> CodecTestHelper.parseJson(server.registryAccess(), AthanorRecipe.CODEC.codec(), "athanor test", """
				{
					"inputs": ["minecraft:dirt", "minecraft:dirt", "minecraft:dirt", "minecraft:dirt"],
					"catalyst": "minecraft:diamond",
					"stages": [{ "stage": "nigredo", "ticks": 20 }],
					"result": "minecraft:emerald"
				}"""));
		//Idle and complete are not stages a ritual can have
		Assertions.assertThrows(JsonParseException.class, () -> CodecTestHelper.parseJson(server.registryAccess(), AthanorRecipe.CODEC.codec(), "athanor test", """
				{
					"inputs": ["minecraft:dirt"],
					"catalyst": "minecraft:diamond",
					"stages": [{ "stage": "idle", "ticks": 20 }],
					"result": "minecraft:emerald"
				}"""));
	}
}
