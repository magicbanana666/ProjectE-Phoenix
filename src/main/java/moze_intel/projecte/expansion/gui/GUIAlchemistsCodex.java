package moze_intel.projecte.expansion.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.expansion.codex.Codex;
import moze_intel.projecte.expansion.codex.Codex.Chapter;
import moze_intel.projecte.expansion.codex.Codex.Entry;
import moze_intel.projecte.expansion.codex.CodexRecipes;
import moze_intel.projecte.expansion.registries.ExpansionItems;
import moze_intel.projecte.gameObjs.CrucibleStructure;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorStage;
import moze_intel.projecte.gameObjs.customRecipes.athanor.RuneColor;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronLiquid;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The screen of the Alchemist's Codex, the in game guide of the mod.
 * <p>
 * An open book with two pages side by side. It starts on the home spread: the introduction, and a grid with the icon of
 * every chapter. A chapter lists its entries, an entry shows its text followed by the recipes of the items it is about:
 * crafting grids, the rituals of the Athanor, and for the Athanor itself its layers. Clicking an item in a recipe opens
 * the entry about it. Home goes back to the start, Back to wherever the player came from.
 */
public class GUIAlchemistsCodex extends Screen {

	private static final int BOOK_WIDTH = 292;
	private static final int BOOK_HEIGHT = 180;
	private static final int PAGE_MARGIN = 14;
	private static final int PAGE_WIDTH = BOOK_WIDTH / 2 - 2 * PAGE_MARGIN;
	private static final int CONTENT_TOP = 12;
	/**
	 * Room for content on a page, the bottom is kept free for the page buttons and numbers.
	 */
	private static final int CONTENT_HEIGHT = BOOK_HEIGHT - CONTENT_TOP - 24;
	private static final int LINE_HEIGHT = 9;
	private static final int HEADER_HEIGHT = 22;
	private static final int ROW_HEIGHT = 18;
	private static final int SLOT = 18;

	private static final int COVER = 0xFF5A2E16;
	private static final int COVER_DARK = 0xFF2E160A;
	private static final int PARCHMENT = 0xFFF2E6C8;
	private static final int PARCHMENT_SHADE = 0xFFE0CFA6;
	private static final int TEXT = 0xFF2B1D0E;
	private static final int MUTED = 0xFF7A6A50;
	private static final int TITLE = 0xFF6B0F1A;
	private static final int RULE = 0x80B08A3C;
	private static final int HIGHLIGHT = 0x40B08A3C;
	private static final int SLOT_BORDER = 0xFF8B7350;
	private static final int SLOT_FILL = 0xFFE8D9B3;

	/**
	 * Where the codex was left, so that reopening it continues there.
	 */
	@Nullable
	private static Location lastLocation;

	private final Deque<Location> history = new ArrayDeque<>();
	private final List<Hotspot> hotspots = new ArrayList<>();
	/**
	 * The entry each item belongs to, to jump there when the item is clicked in a recipe.
	 */
	private final Map<Item, View> itemEntries = new HashMap<>();
	private View view = View.HOME;
	private int spread;
	private List<Page> pages = List.of();
	private PageButton forwardButton;
	private PageButton backButton;
	private Button historyButton;

	public GUIAlchemistsCodex() {
		super(Component.translatable(Codex.TITLE));
		for (int c = 0; c < Codex.CHAPTERS.size(); c++) {
			List<Entry> entries = Codex.CHAPTERS.get(c).entries();
			for (int e = 0; e < entries.size(); e++) {
				View target = new View(c, e);
				itemEntries.putIfAbsent(item(entries.get(e).iconId()), target);
				for (Identifier recipeItem : entries.get(e).recipeItems()) {
					itemEntries.putIfAbsent(item(recipeItem), target);
				}
			}
		}
		itemEntries.remove(Items.AIR);
	}

	private static Item item(Identifier id) {
		return BuiltInRegistries.ITEM.getValue(id);
	}

	@Override
	protected void init() {
		int left = bookLeft();
		int top = bookTop();
		backButton = addRenderableWidget(new PageButton(left + PAGE_MARGIN, top + BOOK_HEIGHT - 18, false, button -> turn(-1), true));
		forwardButton = addRenderableWidget(new PageButton(left + BOOK_WIDTH - PAGE_MARGIN - 23, top + BOOK_HEIGHT - 18, true, button -> turn(1), true));
		int buttonsTop = top + BOOK_HEIGHT + 10;
		addRenderableWidget(Button.builder(Component.translatable(Codex.HOME), button -> open(View.HOME))
				.pos(width / 2 - 151, buttonsTop).width(98).build());
		historyButton = addRenderableWidget(Button.builder(Component.translatable(Codex.BACK), button -> goBack())
				.pos(width / 2 - 49, buttonsTop).width(98).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.pos(width / 2 + 53, buttonsTop).width(98).build());
		if (lastLocation != null) {
			view = lastLocation.view();
			spread = lastLocation.spread();
		}
		rebuild();
	}

	private void open(View target) {
		if (target.equals(view)) {
			spread = 0;
		} else {
			history.push(new Location(view, spread));
			view = target;
			spread = 0;
		}
		rebuild();
	}

	private void goBack() {
		if (!history.isEmpty()) {
			Location previous = history.pop();
			view = previous.view();
			spread = previous.spread();
			rebuild();
		}
	}

	private void turn(int direction) {
		spread = Mth.clamp(spread + direction, 0, spreadCount() - 1);
		remember();
		updateButtons();
	}

	private int spreadCount() {
		return Math.max(1, (pages.size() + 1) / 2);
	}

	private void rebuild() {
		pages = buildPages(view);
		spread = Mth.clamp(spread, 0, spreadCount() - 1);
		remember();
		updateButtons();
	}

	private void remember() {
		lastLocation = new Location(view, spread);
	}

	private void updateButtons() {
		backButton.visible = spread > 0;
		forwardButton.visible = spread < spreadCount() - 1;
		historyButton.active = !history.isEmpty();
	}

	//Building the pages

	private List<Page> buildPages(View target) {
		List<Page> built = new ArrayList<>();
		if (target.isHome()) {
			//The chapters always face the introduction, like the contents page of a real book
			addText(built, new ItemStack(ExpansionItems.ALCHEMISTS_CODEX.get()), Component.translatable(Codex.TITLE), Component.translatable(Codex.INTRO));
			built.add(1, new ChapterGrid());
		} else if (target.isChapter()) {
			Chapter chapter = Codex.CHAPTERS.get(target.chapter());
			addText(built, chapter.icon(), chapter.title(), chapter.intro());
			List<Link> links = new ArrayList<>();
			for (int e = 0; e < chapter.entries().size(); e++) {
				Entry entry = chapter.entries().get(e);
				links.add(new Link(entry.icon(), entry.title(), new View(target.chapter(), e)));
			}
			int perPage = CONTENT_HEIGHT / ROW_HEIGHT;
			for (int i = 0; i < links.size(); i += perPage) {
				built.add(new LinkList(links.subList(i, Math.min(links.size(), i + perPage))));
			}
		} else {
			Entry entry = Codex.CHAPTERS.get(target.chapter()).entries().get(target.entry());
			addText(built, entry.icon(), entry.title(), entry.text());
			if (entry.structure()) {
				built.add(new StructurePage());
			}
			Level level = Minecraft.getInstance().level;
			for (Identifier id : entry.recipeItems()) {
				if (id.getNamespace().equals("minecraft")) {
					//How to craft a vanilla item is not what this book is for
					continue;
				}
				Item recipeItem = item(id);
				for (CauldronRecipe brew : CodexRecipes.brewsFor(recipeItem)) {
					built.add(new CauldronPage(brew));
				}
				for (AthanorRecipe ritual : CodexRecipes.ritualsFor(recipeItem)) {
					built.add(new RitualPage(ritual));
				}
				if (level != null) {
					for (RecipeDisplay display : CodexRecipes.craftingFor(recipeItem, level)) {
						if (display instanceof ShapedCraftingRecipeDisplay || display instanceof ShapelessCraftingRecipeDisplay) {
							built.add(new CraftingPage(display));
						}
					}
				}
			}
		}
		return built;
	}

	/**
	 * Wraps the text and spreads it over as many pages as it needs. The first page starts with the icon and title.
	 */
	private void addText(List<Page> built, ItemStack icon, Component title, Component text) {
		List<FormattedCharSequence> lines = font.split(ComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withColor(TEXT)), PAGE_WIDTH);
		int first = (CONTENT_HEIGHT - HEADER_HEIGHT) / LINE_HEIGHT;
		int rest = CONTENT_HEIGHT / LINE_HEIGHT;
		int index = 0;
		boolean header = true;
		do {
			int count = header ? first : rest;
			built.add(new TextPage(header ? icon : null, header ? title : null, lines.subList(index, Math.min(lines.size(), index + count))));
			index += count;
			header = false;
		} while (index < lines.size());
	}

	//Input

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			for (Hotspot hotspot : hotspots) {
				if (hotspot.action() != null && hotspot.contains(event.x(), event.y())) {
					hotspot.action().run();
					return true;
				}
			}
		} else if (event.button() == 3) {
			//The back button of a mouse
			goBack();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) {
			return true;
		}
		return switch (event.key()) {
			//Page up and left arrow turn back, page down and right arrow turn forward, backspace goes back
			case 266, 263 -> {
				turn(-1);
				yield true;
			}
			case 267, 262 -> {
				turn(1);
				yield true;
			}
			case 259 -> {
				goBack();
				yield true;
			}
			default -> false;
		};
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0) {
			turn(scrollY < 0 ? 1 : -1);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	//Drawing

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int left = bookLeft();
		int top = bookTop();
		int middle = left + BOOK_WIDTH / 2;
		graphics.fill(left - 6, top - 6, left + BOOK_WIDTH + 6, top + BOOK_HEIGHT + 6, COVER);
		graphics.outline(left - 6, top - 6, BOOK_WIDTH + 12, BOOK_HEIGHT + 12, COVER_DARK);
		graphics.fill(left, top, left + BOOK_WIDTH, top + BOOK_HEIGHT, PARCHMENT);
		//The pages darken towards the spine
		graphics.fillGradient(middle - 12, top, middle, top + BOOK_HEIGHT, PARCHMENT, PARCHMENT_SHADE);
		graphics.fill(middle, top, middle + 6, top + BOOK_HEIGHT, PARCHMENT_SHADE);
		graphics.verticalLine(middle, top - 6, top + BOOK_HEIGHT + 6, COVER_DARK);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		hotspots.clear();
		for (int side = 0; side < 2; side++) {
			int index = spread * 2 + side;
			int x = pageLeft(side);
			if (index < pages.size()) {
				pages.get(index).draw(graphics, x, bookTop() + CONTENT_TOP);
				if (pages.size() > 2) {
					String number = String.valueOf(index + 1);
					graphics.text(font, number, x + (PAGE_WIDTH - font.width(number)) / 2, bookTop() + BOOK_HEIGHT - 14, MUTED, false);
				}
			}
		}
		//Highlight what can be clicked, and show the tooltip of what the mouse is over
		for (Hotspot hotspot : hotspots) {
			if (hotspot.contains(mouseX, mouseY)) {
				if (hotspot.action() != null) {
					graphics.fill(hotspot.x(), hotspot.y(), hotspot.x() + hotspot.width(), hotspot.y() + hotspot.height(), HIGHLIGHT);
				}
				if (!hotspot.stack().isEmpty()) {
					graphics.setTooltipForNextFrame(font, hotspot.stack(), mouseX, mouseY);
				} else if (!hotspot.tooltip().isEmpty()) {
					graphics.setTooltipForNextFrame(font, hotspot.tooltip(), mouseX, mouseY);
				}
				break;
			}
		}
	}

	private int bookLeft() {
		return (width - BOOK_WIDTH) / 2;
	}

	private int bookTop() {
		return Math.max(8, (height - BOOK_HEIGHT - 32) / 2);
	}

	private int pageLeft(int side) {
		return bookLeft() + side * BOOK_WIDTH / 2 + PAGE_MARGIN + (side == 1 ? 2 : 0);
	}

	private void drawHeader(GuiGraphicsExtractor graphics, @Nullable ItemStack icon, Component title, int x, int y) {
		int titleX = x;
		if (icon != null) {
			graphics.item(icon, x, y);
			titleX += 20;
		}
		List<FormattedCharSequence> lines = font.split(title.copy().withStyle(Style.EMPTY.withBold(true)), PAGE_WIDTH - (titleX - x));
		int titleY = lines.size() > 1 ? y - 1 : y + 4;
		for (int i = 0; i < Math.min(2, lines.size()); i++) {
			graphics.text(font, lines.get(i), titleX, titleY + i * LINE_HEIGHT, TITLE, false);
		}
		graphics.horizontalLine(x, x + PAGE_WIDTH, y + HEADER_HEIGHT - 4, RULE);
	}

	private void drawCentredTitle(GuiGraphicsExtractor graphics, Component title, int x, int y) {
		Component styled = title.copy().withStyle(Style.EMPTY.withBold(true));
		graphics.text(font, styled, x + (PAGE_WIDTH - font.width(styled)) / 2, y, TITLE, false);
		graphics.horizontalLine(x, x + PAGE_WIDTH, y + 11, RULE);
	}

	private void drawCentred(GuiGraphicsExtractor graphics, Component text, int x, int y, int color) {
		graphics.text(font, text, x + (PAGE_WIDTH - font.width(text)) / 2, y, color, false);
	}

	/**
	 * Draws a slot with one of the stacks in it, the stacks take turns once a second. Clicking it opens the entry of the item.
	 */
	private void drawStack(GuiGraphicsExtractor graphics, List<ItemStack> stacks, int x, int y) {
		graphics.fill(x, y, x + SLOT, y + SLOT, SLOT_BORDER);
		graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, SLOT_FILL);
		if (stacks.isEmpty()) {
			return;
		}
		ItemStack stack = stacks.get((int) (System.currentTimeMillis() / 1000 % stacks.size()));
		if (stack.isEmpty()) {
			return;
		}
		graphics.item(stack, x + 1, y + 1);
		graphics.itemDecorations(font, stack, x + 1, y + 1);
		View target = itemEntries.get(stack.getItem());
		Runnable action = target == null || target.equals(view) ? null : () -> open(target);
		hotspots.add(new Hotspot(x, y, SLOT, SLOT, stack, List.of(), action));
	}

	private static List<ItemStack> stacks(Ingredient ingredient) {
		return ingredient.items().map(ItemStack::new).toList();
	}

	private static List<ItemStack> stacks(SlotDisplay display, ContextMap context) {
		try {
			return display.resolveForStacks(context);
		} catch (RuntimeException e) {
			//A display of another mod that can not be resolved without more context
			return List.of();
		}
	}

	private FormattedCharSequence trim(Component text, int maxWidth) {
		if (font.width(text) <= maxWidth) {
			return text.getVisualOrderText();
		}
		String ellipsis = "...";
		return Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(text, maxWidth - font.width(ellipsis)), Component.literal(ellipsis)));
	}

	/**
	 * @return the EMC in a short form, such as 65.5k or 12.6M
	 */
	private static String shortEmc(long emc) {
		String[] suffixes = {"", "k", "M", "G", "T", "P"};
		double value = emc;
		int suffix = 0;
		while (value >= 1000 && suffix < suffixes.length - 1) {
			value /= 1000;
			suffix++;
		}
		return suffix == 0 ? String.valueOf(emc) : String.format(value >= 100 ? "%.0f%s" : "%.1f%s", value, suffixes[suffix]);
	}

	//Pages

	private interface Page {

		void draw(GuiGraphicsExtractor graphics, int x, int y);
	}

	private class TextPage implements Page {

		@Nullable
		private final ItemStack icon;
		@Nullable
		private final Component title;
		private final List<FormattedCharSequence> lines;

		private TextPage(@Nullable ItemStack icon, @Nullable Component title, List<FormattedCharSequence> lines) {
			this.icon = icon;
			this.title = title;
			this.lines = lines;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			int textY = y;
			if (title != null) {
				drawHeader(graphics, icon, title, x, y);
				textY += HEADER_HEIGHT;
			}
			for (FormattedCharSequence line : lines) {
				graphics.text(font, line, x, textY, TEXT, false);
				textY += LINE_HEIGHT;
			}
		}
	}

	/**
	 * The icons of all chapters. The name and introduction of a chapter show when the mouse is over its icon.
	 */
	private class ChapterGrid implements Page {

		private static final int COLUMNS = 3;
		private static final int CELL = 36;
		private static final int FRAME = 28;

		@Override
		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			drawCentredTitle(graphics, Component.translatable(Codex.CHAPTERS_TITLE), x, y);
			int gridLeft = x + (PAGE_WIDTH - COLUMNS * CELL) / 2;
			int gridTop = y + 18;
			for (int c = 0; c < Codex.CHAPTERS.size(); c++) {
				Chapter chapter = Codex.CHAPTERS.get(c);
				int cellX = gridLeft + (c % COLUMNS) * CELL + (CELL - FRAME) / 2;
				int cellY = gridTop + (c / COLUMNS) * CELL;
				graphics.fill(cellX, cellY, cellX + FRAME, cellY + FRAME, COVER);
				graphics.fill(cellX + 2, cellY + 2, cellX + FRAME - 2, cellY + FRAME - 2, SLOT_FILL);
				graphics.pose().pushMatrix();
				graphics.pose().translate(cellX + 2, cellY + 2);
				graphics.pose().scale(1.5F, 1.5F);
				graphics.item(chapter.icon(), 0, 0);
				graphics.pose().popMatrix();
				List<FormattedCharSequence> tooltip = new ArrayList<>();
				tooltip.add(chapter.title().copy().withStyle(Style.EMPTY.withBold(true)).getVisualOrderText());
				tooltip.addAll(font.split(chapter.intro(), 160));
				View target = new View(c, -1);
				hotspots.add(new Hotspot(cellX, cellY, FRAME, FRAME, ItemStack.EMPTY, tooltip, () -> open(target)));
			}
		}
	}

	private record Link(ItemStack icon, Component name, View target) {
	}

	private class LinkList implements Page {

		private final List<Link> links;

		private LinkList(List<Link> links) {
			this.links = links;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			int rowY = y;
			for (Link link : links) {
				graphics.item(link.icon(), x, rowY);
				graphics.text(font, trim(link.name(), PAGE_WIDTH - 20), x + 20, rowY + 4, TEXT, false);
				hotspots.add(new Hotspot(x - 1, rowY - 1, PAGE_WIDTH + 2, ROW_HEIGHT, ItemStack.EMPTY, List.of(), () -> open(link.target())));
				rowY += ROW_HEIGHT;
			}
		}
	}

	private class CraftingPage implements Page {

		private final RecipeDisplay display;

		private CraftingPage(RecipeDisplay display) {
			this.display = display;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			drawCentredTitle(graphics, Component.translatable(Codex.PAGE_CRAFTING), x, y);
			Level level = Minecraft.getInstance().level;
			if (level == null) {
				return;
			}
			ContextMap context = SlotDisplayContext.fromLevel(level);
			int gridLeft = x + (PAGE_WIDTH - (3 * SLOT + 22 + SLOT)) / 2;
			int gridTop = y + 24;
			List<SlotDisplay> ingredients;
			int recipeWidth;
			if (display instanceof ShapedCraftingRecipeDisplay shaped) {
				ingredients = shaped.ingredients();
				recipeWidth = shaped.width();
			} else {
				ingredients = ((ShapelessCraftingRecipeDisplay) display).ingredients();
				recipeWidth = 3;
			}
			for (int slot = 0; slot < 9; slot++) {
				int column = slot % 3;
				int row = slot / 3;
				int index = column < recipeWidth ? row * recipeWidth + column : -1;
				List<ItemStack> slotStacks = index >= 0 && index < ingredients.size() ? stacks(ingredients.get(index), context) : List.of();
				drawStack(graphics, slotStacks, gridLeft + column * SLOT, gridTop + row * SLOT);
			}
			graphics.text(font, "→", gridLeft + 3 * SLOT + 7, gridTop + SLOT + 5, MUTED, false);
			drawStack(graphics, stacks(display.result(), context), gridLeft + 3 * SLOT + 22, gridTop + SLOT);
			if (display instanceof ShapelessCraftingRecipeDisplay) {
				drawCentred(graphics, Component.translatable(Codex.PAGE_SHAPELESS), x, gridTop + 3 * SLOT + 8, MUTED);
			}
		}
	}

	private class RitualPage implements Page {

		private final AthanorRecipe recipe;

		private RitualPage(AthanorRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			drawCentredTitle(graphics, Component.translatable(Codex.PAGE_RITUAL), x, y);
			//The ingredients around the catalyst, as they float above the dais, and the result next to them
			int centreX = x + 38;
			int centreY = y + 44;
			int count = recipe.inputs().size();
			for (int i = 0; i < count; i++) {
				double angle = -Math.PI / 2 + i * Math.PI * 2 / count;
				int slotX = centreX + (int) Math.round(Math.cos(angle) * 24) - SLOT / 2;
				int slotY = centreY + (int) Math.round(Math.sin(angle) * 24) - SLOT / 2;
				drawStack(graphics, stacks(recipe.inputs().get(i)), slotX, slotY);
			}
			//The catalyst has a red border, like the rune circle it sits in
			graphics.fill(centreX - SLOT / 2 - 1, centreY - SLOT / 2 - 1, centreX + SLOT / 2 + 1, centreY + SLOT / 2 + 1, TITLE);
			drawStack(graphics, stacks(recipe.catalyst()), centreX - SLOT / 2, centreY - SLOT / 2);
			graphics.text(font, "→", x + 72, centreY - 4, MUTED, false);
			drawStack(graphics, List.of(recipe.result().create()), x + 86, centreY - SLOT / 2);
			int lineY = y + 74;
			if (recipe.keepCatalyst()) {
				drawCentred(graphics, Component.translatable(Codex.RITUAL_KEEPS_CATALYST), x, lineY, MUTED);
				lineY += LINE_HEIGHT + 2;
			}
			//The stages, one line each, with the colour of their runes
			for (AthanorStage stage : recipe.stages()) {
				RuneColor rune = stage.rune();
				graphics.fill(x, lineY, x + 7, lineY + 7, COVER_DARK);
				graphics.fill(x + 1, lineY + 1, x + 6, lineY + 6, 0xFF000000 | rune.getRgb());
				Component name = stage.stage().getName().copy().append(stage.needs().isPresent() ? " *" : "");
				graphics.text(font, name, x + 10, lineY, TEXT, false);
				String emc = shortEmc(stage.emc());
				graphics.text(font, emc, x + PAGE_WIDTH - font.width(emc), lineY, MUTED, false);
				List<FormattedCharSequence> tooltip = new ArrayList<>();
				tooltip.add(stage.stage().getName().getVisualOrderText());
				tooltip.add(Component.translatable(Codex.RITUAL_STAGE, String.format("%.1f", stage.ticks() / 20F), String.format("%,d", stage.emc())).getVisualOrderText());
				stage.needs().ifPresent(needs -> tooltip.add(Component.translatable(Codex.RITUAL_NEEDS,
						Component.translatable(Codex.RITUAL_NEEDS + "." + needs.getSerializedName())).getVisualOrderText()));
				hotspots.add(new Hotspot(x, lineY - 1, PAGE_WIDTH, LINE_HEIGHT + 1, ItemStack.EMPTY, tooltip, null));
				lineY += LINE_HEIGHT + 2;
			}
			graphics.horizontalLine(x, x + PAGE_WIDTH, lineY, RULE);
			lineY += 3;
			Component total = Component.translatable(Codex.RITUAL_TOTAL, String.format("%,d", recipe.totalEmc()), String.format("%.0f", recipe.totalTicks() / 20F));
			for (FormattedCharSequence line : font.split(total, PAGE_WIDTH)) {
				graphics.text(font, line, x, lineY, TEXT, false);
				lineY += LINE_HEIGHT;
			}
		}
	}

	/**
	 * A recipe of the Alchemical Cauldron: what goes in, what it brews into, and what the cauldron has to be like for it.
	 */
	private class CauldronPage implements Page {

		private final CauldronRecipe recipe;

		private CauldronPage(CauldronRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			drawCentredTitle(graphics, Component.translatable(Codex.PAGE_CAULDRON), x, y);
			int top = y + 18;
			for (int i = 0; i < recipe.ingredients().size(); i++) {
				SizedIngredient ingredient = recipe.ingredients().get(i);
				List<ItemStack> options = ingredient.ingredient().items().map(item -> new ItemStack(item, ingredient.count())).toList();
				drawStack(graphics, options, x + (i % 3) * SLOT, top + (i / 3) * SLOT);
			}
			int middle = top + (recipe.ingredients().size() > 3 ? SLOT / 2 : 0);
			graphics.text(font, "→", x + 3 * SLOT + 7, middle + 5, MUTED, false);
			int resultX = x + 3 * SLOT + 22;
			if (recipe.result().isPresent()) {
				drawStack(graphics, List.of(recipe.result().get().create()), resultX, middle);
			} else {
				Component liquid = liquidName(recipe.resultLiquid().orElse(CauldronLiquid.WATER));
				graphics.fill(resultX, middle, resultX + SLOT, middle + SLOT, SLOT_BORDER);
				graphics.fill(resultX + 1, middle + 1, resultX + SLOT - 1, middle + SLOT - 1, 0xFF000000 | recipe.resultLiquid().orElse(CauldronLiquid.WATER).getColor());
				hotspots.add(new Hotspot(resultX, middle, SLOT, SLOT, ItemStack.EMPTY, List.of(liquid.getVisualOrderText()), null));
			}
			int lineY = top + 2 * SLOT + 8;
			List<Component> lines = new ArrayList<>();
			Component heat = Component.translatable("cauldron.projecte.heat." + recipe.heat().map(h -> h.getSerializedName()).orElse("none"));
			lines.add(Component.translatable(Codex.CAULDRON_CONDITIONS, liquidName(recipe.liquid()), heat, recipe.circles(), String.format("%.0f", recipe.ticks() / 20F)));
			if (recipe.souls() > 0) {
				lines.add(Component.translatable(Codex.CAULDRON_SOULS, recipe.souls()));
			}
			recipe.resultLiquid().filter(liquid -> recipe.result().isPresent() || liquid != recipe.liquid())
					.ifPresent(liquid -> lines.add(Component.translatable(Codex.CAULDRON_BECOMES, liquidName(liquid))));
			for (Component line : lines) {
				for (FormattedCharSequence wrapped : font.split(line, PAGE_WIDTH)) {
					graphics.text(font, wrapped, x, lineY, TEXT, false);
					lineY += LINE_HEIGHT;
				}
				lineY += 2;
			}
		}

		private static Component liquidName(CauldronLiquid liquid) {
			return Component.translatable("cauldron.projecte.liquid." + liquid.getSerializedName());
		}
	}

	/**
	 * The Athanor layer by layer, from the base up. The arrows next to the layer number turn through the layers.
	 */
	private class StructurePage implements Page {

		private static final int BOTTOM = -1;
		private static final int TOP = 4;
		private int layer = BOTTOM;

		@Override
		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			drawCentredTitle(graphics, Component.translatable(Codex.PAGE_STRUCTURE), x, y);
			int labelY = y + 16;
			drawCentred(graphics, Component.translatable(Codex.STRUCTURE_LAYER, layer - BOTTOM + 1, TOP - BOTTOM + 1), x, labelY, TEXT);
			graphics.text(font, "<", x + 4, labelY, TITLE, false);
			graphics.text(font, ">", x + PAGE_WIDTH - 9, labelY, TITLE, false);
			hotspots.add(new Hotspot(x, labelY - 2, 14, 12, ItemStack.EMPTY, List.of(), () -> layer = layer == BOTTOM ? TOP : layer - 1));
			hotspots.add(new Hotspot(x + PAGE_WIDTH - 14, labelY - 2, 14, 12, ItemStack.EMPTY, List.of(), () -> layer = layer == TOP ? BOTTOM : layer + 1));
			int gridLeft = x + (PAGE_WIDTH - 5 * SLOT) / 2;
			int gridTop = y + 30;
			ItemStack[][] grid = new ItemStack[5][5];
			for (CrucibleStructure.Part part : CrucibleStructure.getParts()) {
				if (part.offset().getY() == layer) {
					grid[part.offset().getX() + 2][part.offset().getZ() + 2] = new ItemStack(part.block().getBlock());
				}
			}
			for (int gx = 0; gx < 5; gx++) {
				for (int gz = 0; gz < 5; gz++) {
					int slotX = gridLeft + gx * SLOT;
					int slotY = gridTop + gz * SLOT;
					if (grid[gx][gz] == null) {
						//Air: only a dot, so the shape of the layer stands out
						graphics.fill(slotX + 8, slotY + 8, slotX + 10, slotY + 10, SLOT_BORDER);
					} else {
						drawStack(graphics, List.of(grid[gx][gz]), slotX, slotY);
					}
				}
			}
		}
	}

	//Navigation

	/**
	 * What the book shows: the home spread, a chapter or an entry. Unused parts are -1.
	 */
	private record View(int chapter, int entry) {

		private static final View HOME = new View(-1, -1);

		private boolean isHome() {
			return chapter < 0;
		}

		private boolean isChapter() {
			return chapter >= 0 && entry < 0;
		}
	}

	private record Location(View view, int spread) {
	}

	private record Hotspot(int x, int y, int width, int height, ItemStack stack, List<FormattedCharSequence> tooltip, @Nullable Runnable action) {

		private boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
		}
	}
}
