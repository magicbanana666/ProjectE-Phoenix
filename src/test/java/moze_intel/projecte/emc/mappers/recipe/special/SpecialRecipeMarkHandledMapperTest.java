package moze_intel.projecte.emc.mappers.recipe.special;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test special recipe diagnostic classification")
class SpecialRecipeMarkHandledMapperTest {

	@Test
	@DisplayName("Balance-preserving special recipes are marked as handled")
	void testBalancePreservingSpecialRecipesAreHandled() {
		SpecialRecipeMarkHandledMapper mapper = new SpecialRecipeMarkHandledMapper();
		RecipeHolder<?> bannerRecipe = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE,
				Identifier.fromNamespaceAndPath("projecte", "banner_duplicate_test")),
				new BannerDuplicateRecipe(Ingredient.of(Items.BANNER.white()), new ItemStackTemplate(Items.BANNER.white())));
		RecipeHolder<?> bookRecipe = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE,
				Identifier.fromNamespaceAndPath("projecte", "book_clone_test")),
				new BookCloningRecipe(Ingredient.of(Items.WRITABLE_BOOK), Ingredient.of(Items.PAPER),
						BookCloningRecipe.DEFAULT_BOOK_GENERATION_RANGES, new ItemStackTemplate(Items.WRITTEN_BOOK)));

		Assertions.assertTrue(mapper.handleRecipe(null, bannerRecipe, null, null));
		Assertions.assertTrue(mapper.handleRecipe(null, bookRecipe, null, null));
		Assertions.assertNull(mapper.getExpectedUnhandledReason(bookRecipe, null));
	}
}
