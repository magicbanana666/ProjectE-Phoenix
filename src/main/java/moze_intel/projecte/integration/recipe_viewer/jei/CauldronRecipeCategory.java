package moze_intel.projecte.integration.recipe_viewer.jei;

import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronLiquid;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

/**
 * The recipes of the Alchemical Cauldron: the ingredients, what they brew into, and below them the liquid, heat, circles and
 * souls the brew needs.
 */
public class CauldronRecipeCategory implements IRecipeCategory<CauldronRecipe> {

	public static final RecipeType<CauldronRecipe> RECIPE_TYPE = new RecipeType<>(PECore.rl("cauldron"), CauldronRecipe.class);
	private static final int WIDTH = 150;
	private static final int TEXT_TOP = 40;
	private static final int TEXT = 0xFF404040;
	private static final int RESULT_X = 110;
	private static final int RESULT_Y = 10;

	private final IDrawable icon;

	public CauldronRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PEBlocks.ALCHEMICAL_CAULDRON));
	}

	@NotNull
	@Override
	public RecipeType<CauldronRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}

	@NotNull
	@Override
	public Component getTitle() {
		return PELang.JEI_CAULDRON.translate();
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return TEXT_TOP + 4 * 10;
	}

	@NotNull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CauldronRecipe recipe, @NotNull IFocusGroup focuses) {
		for (int i = 0; i < recipe.ingredients().size(); i++) {
			SizedIngredient ingredient = recipe.ingredients().get(i);
			builder.addSlot(RecipeIngredientRole.INPUT, 1 + (i % 3) * 18, 1 + (i / 3) * 18)
					.setStandardSlotBackground()
					.addItemStacks(ingredient.ingredient().items().map(item -> new ItemStack(item, ingredient.count())).toList());
		}
		recipe.result().ifPresent(result -> builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, RESULT_Y)
				.setOutputSlotBackground()
				.add(result));
	}

	@Override
	public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull CauldronRecipe recipe, @NotNull IFocusGroup focuses) {
		builder.addAnimatedRecipeArrow(recipe.ticks()).setPosition(70, RESULT_Y);
	}

	@Override
	public void draw(@NotNull CauldronRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		if (recipe.result().isEmpty()) {
			//Only the liquid changes: a swatch of its colour where the item would be
			CauldronLiquid liquid = recipe.resultLiquid().orElse(CauldronLiquid.WATER);
			graphics.fill(RESULT_X - 1, RESULT_Y - 1, RESULT_X + 17, RESULT_Y + 17, 0xFF373737);
			graphics.fill(RESULT_X, RESULT_Y, RESULT_X + 16, RESULT_Y + 16, 0xFF000000 | liquid.getColor());
		}
		int y = TEXT_TOP;
		Component heat = Component.translatable("cauldron.projecte.heat." + recipe.heat().map(h -> h.getSerializedName()).orElse("none"));
		Component conditions = Component.translatable("codex.projecte.cauldron.conditions", liquidName(recipe.liquid()), heat, recipe.circles(),
				String.format("%.0f", recipe.ticks() / 20F));
		for (FormattedCharSequence line : font.split(conditions, WIDTH)) {
			graphics.text(font, line, 0, y, TEXT, false);
			y += 10;
		}
		if (recipe.souls() > 0) {
			graphics.text(font, Component.translatable("codex.projecte.cauldron.souls", recipe.souls()), 0, y, 0xFF2D6E6E, false);
		}
	}

	@Override
	public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull CauldronRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (recipe.result().isEmpty() && mouseX >= RESULT_X && mouseX < RESULT_X + 16 && mouseY >= RESULT_Y && mouseY < RESULT_Y + 16) {
			tooltip.add(liquidName(recipe.resultLiquid().orElse(CauldronLiquid.WATER)));
		}
	}

	private static Component liquidName(CauldronLiquid liquid) {
		return Component.translatable("cauldron.projecte.liquid." + liquid.getSerializedName());
	}

	public static List<CauldronRecipe> getRecipes() {
		return moze_intel.projecte.expansion.codex.CodexRecipes.brews();
	}
}
