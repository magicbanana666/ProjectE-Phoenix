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
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorStage;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The rituals of the Athanor: the ingredients around the catalyst as they float above the dais, the result, and below them
 * every stage with the colour of its runes and the EMC it costs. Hovering a stage shows its length and what it needs.
 */
public class AthanorRecipeCategory implements IRecipeCategory<AthanorRecipe> {

	public static final RecipeType<AthanorRecipe> RECIPE_TYPE = new RecipeType<>(PECore.rl("athanor"), AthanorRecipe.class);
	private static final int WIDTH = 150;
	private static final int CENTRE_X = 34;
	private static final int CENTRE_Y = 30;
	private static final int RADIUS = 24;
	private static final int STAGES_TOP = 64;
	private static final int STAGE_HEIGHT = 10;
	private static final int TEXT = 0xFF404040;

	private final IDrawable icon;

	public AthanorRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PEBlocks.ALCHEMICAL_CRUCIBLE));
	}

	@NotNull
	@Override
	public RecipeType<AthanorRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}

	@NotNull
	@Override
	public Component getTitle() {
		return PELang.JEI_ATHANOR.translate();
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		//The tallest ritual has as many lines as stages, and one for the total
		return STAGES_TOP + 5 * STAGE_HEIGHT + 2;
	}

	@NotNull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull AthanorRecipe recipe, @NotNull IFocusGroup focuses) {
		int count = recipe.inputs().size();
		for (int i = 0; i < count; i++) {
			double angle = -Math.PI / 2 + i * Math.PI * 2 / count;
			int x = CENTRE_X + (int) Math.round(Math.cos(angle) * RADIUS) - 8;
			int y = CENTRE_Y + (int) Math.round(Math.sin(angle) * RADIUS) - 8;
			builder.addSlot(RecipeIngredientRole.INPUT, x, y)
					.setStandardSlotBackground()
					.addIngredients(recipe.inputs().get(i));
		}
		builder.addSlot(RecipeIngredientRole.INPUT, CENTRE_X - 8, CENTRE_Y - 8)
				.setStandardSlotBackground()
				.addIngredients(recipe.catalyst());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 118, CENTRE_Y - 8)
				.setOutputSlotBackground()
				.add(recipe.result());
	}

	@Override
	public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull AthanorRecipe recipe, @NotNull IFocusGroup focuses) {
		builder.addAnimatedRecipeArrow(Math.max(20, recipe.totalTicks() / 10)).setPosition(80, CENTRE_Y - 8);
	}

	@Override
	public void draw(@NotNull AthanorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		int y = STAGES_TOP;
		for (AthanorStage stage : recipe.stages()) {
			graphics.fill(0, y, 7, y + 7, 0xFF2E160A);
			graphics.fill(1, y + 1, 6, y + 6, 0xFF000000 | stage.rune().getRgb());
			Component name = stage.stage().getName().copy().append(stage.needs().isPresent() ? " *" : "");
			graphics.text(font, name, 10, y, TEXT, false);
			String emc = String.format("%,d EMC", stage.emc());
			graphics.text(font, emc, WIDTH - font.width(emc), y, TEXT, false);
			y += STAGE_HEIGHT;
		}
		Component total = PELang.JEI_ATHANOR_TOTAL.translate(String.format("%,d", recipe.totalEmc()), String.format("%.0f", recipe.totalTicks() / 20F));
		graphics.text(font, total, 0, y + 1, 0xFF6B0F1A, false);
	}

	@Override
	public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull AthanorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		int index = (int) Math.floor((mouseY - STAGES_TOP) / STAGE_HEIGHT);
		if (mouseY < STAGES_TOP || index >= recipe.stages().size() || mouseX < 0 || mouseX > WIDTH) {
			return;
		}
		AthanorStage stage = recipe.stages().get(index);
		tooltip.add(stage.stage().getName());
		tooltip.add(PELang.JEI_ATHANOR_STAGE.translateColored(ChatFormatting.GRAY, String.format("%.1f", stage.ticks() / 20F), String.format("%,d", stage.emc())));
		stage.needs().ifPresent(needs -> tooltip.add(Component.translatable("codex.projecte.ritual.needs",
				Component.translatable("codex.projecte.ritual.needs." + needs.getSerializedName())).withStyle(ChatFormatting.YELLOW)));
	}

	public static List<AthanorRecipe> getRecipes() {
		return moze_intel.projecte.expansion.codex.CodexRecipes.rituals();
	}
}
