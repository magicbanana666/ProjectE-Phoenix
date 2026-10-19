package moze_intel.projecte.gameObjs;

import java.util.List;
import java.util.Optional;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronHeat;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronInput;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronLiquid;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
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
class CauldronRecipeTest {

	private static CauldronRecipe recipe(MinecraftServer server, String name) {
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, PECore.rl("cauldron/" + name));
		return (CauldronRecipe) server.getRecipeManager().byKey(key).orElseThrow().value();
	}

	@Test
	@DisplayName("The cauldron recipes load from the datapack")
	void testRecipesLoad(MinecraftServer server) {
		List<CauldronRecipe> recipes = server.getRecipeManager().recipeMap().byType(PERecipeTypes.CAULDRON.get()).stream().map(RecipeHolder::value).toList();
		Assertions.assertEquals(10, recipes.size());
		CauldronRecipe redWater = recipe(server, "red_water");
		Assertions.assertEquals(Optional.of(CauldronLiquid.RED_WATER), redWater.resultLiquid());
		Assertions.assertEquals(Optional.of(CauldronHeat.SOUL), redWater.heat());
		Assertions.assertTrue(redWater.result().isEmpty());
	}

	@Test
	@DisplayName("A brew needs the right liquid, heat, circles, souls and exactly its ingredients")
	void testMatching(MinecraftServer server) {
		CauldronRecipe stone = recipe(server, "false_philosophers_stone");
		List<ItemStack> diamond = List.of(new ItemStack(Items.DIAMOND));
		Assertions.assertTrue(stone.matches(new CauldronInput(diamond, CauldronLiquid.RED_WATER, CauldronHeat.HIGH, 3, 16), server.overworld()));
		Assertions.assertFalse(stone.matches(new CauldronInput(diamond, CauldronLiquid.WATER, CauldronHeat.HIGH, 3, 16), server.overworld()));
		Assertions.assertFalse(stone.matches(new CauldronInput(diamond, CauldronLiquid.RED_WATER, CauldronHeat.LOW, 3, 16), server.overworld()));
		Assertions.assertFalse(stone.matches(new CauldronInput(diamond, CauldronLiquid.RED_WATER, CauldronHeat.HIGH, 2, 16), server.overworld()));
		Assertions.assertFalse(stone.matches(new CauldronInput(diamond, CauldronLiquid.RED_WATER, CauldronHeat.HIGH, 3, 15), server.overworld()));
		Assertions.assertFalse(stone.matches(new CauldronInput(List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.DIRT)), CauldronLiquid.RED_WATER,
				CauldronHeat.HIGH, 3, 16), server.overworld()));
		CauldronRecipe salt = recipe(server, "alchemical_salt");
		Assertions.assertFalse(salt.matches(new CauldronInput(List.of(new ItemStack(PEItems.LOW_COVALENCE_DUST.get(), 7)), CauldronLiquid.WATER,
				CauldronHeat.LOW, 0, 0), server.overworld()));
		Assertions.assertTrue(salt.matches(new CauldronInput(List.of(new ItemStack(PEItems.LOW_COVALENCE_DUST.get(), 20)), CauldronLiquid.WATER,
				CauldronHeat.LOW, 0, 0), server.overworld()));
	}

	@Test
	@DisplayName("What the cauldron brews has EMC, and passes it on through Red Water to the Philosopher's Stone")
	void testEmc(MinecraftServer server) {
		MinecraftServer.ReloadableResources resources = server.getServerResources();
		//The same order as when the server maps EMC
		CustomEMCParser.init(server.registryAccess());
		EMCMappingHandler.map(resources.managers(), server.registryAccess(), resources.resourceManager());
		try {
			long diamond = IEMCProxy.INSTANCE.getValue(Items.DIAMOND);
			long falseStone = value(PEItems.FALSE_PHILOSOPHERS_STONE.get());
			//A diamond, plus what the Red Water was brewed from
			Assertions.assertTrue(falseStone > diamond, () -> "False Philosopher's Stone is worth " + falseStone);
			Assertions.assertTrue(value(PEItems.PHILOSOPHERS_STONE.get()) > 3 * falseStone);
			Assertions.assertTrue(value(PEItems.ALCHEMICAL_SALT.get()) > 0);
			Assertions.assertTrue(value(PEItems.QUICKSILVER.get()) > 0);
		} finally {
			EMCMappingHandler.clearEmcMap();
		}
	}

	private static long value(Item item) {
		return IEMCProxy.INSTANCE.getValue(ItemInfo.fromItem(item));
	}
}
