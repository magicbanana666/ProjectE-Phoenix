package moze_intel.projecte.expansion.gui;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.expansion.codex.Codex;
import moze_intel.projecte.expansion.codex.Codex.Chapter;
import moze_intel.projecte.expansion.codex.Codex.Entry;
import moze_intel.projecte.expansion.registries.ExpansionItems;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The screen of the Alchemist's Codex, the in game guide of the mod.
 * <p>
 * It looks like a vanilla book, but unlike {@link BookViewScreen} it lays out its own pages: the text of every entry is
 * wrapped and spread over as many pages as it needs, the first page of an entry shows the item it is about, and the
 * contents and chapter pages link to the pages they list.
 */
public class GUIAlchemistsCodex extends Screen {

	private static final int IMAGE_SIZE = 192;
	private static final int TEXT_X = 36;
	private static final int TEXT_Y = 30;
	private static final int TEXT_WIDTH = 114;
	private static final int LINE_HEIGHT = 9;
	private static final int LINES_PER_PAGE = 128 / LINE_HEIGHT;
	/**
	 * The first page of an entry starts with its icon and title, which take up two lines.
	 */
	private static final int HEADER_LINES = 2;
	private static final Style TEXT_STYLE = Style.EMPTY.withoutShadow().withColor(0xFF000000);
	private static final Style TITLE_STYLE = TEXT_STYLE.withColor(0xFF6B0F1A).withBold(true);
	private static final Style LINK_STYLE = TEXT_STYLE.withColor(0xFF1B3F8C);
	/**
	 * The page the codex was last left open on, so that reopening it continues where the player was.
	 */
	private static int lastPage;

	private final List<Page> pages = new ArrayList<>();
	/**
	 * Page index of every link target, the links store the index into this list as their page number.
	 */
	private final List<Integer> anchors = new ArrayList<>();
	private int contentsPage;
	private int currentPage;
	private PageButton forwardButton;
	private PageButton backButton;

	public GUIAlchemistsCodex() {
		super(Component.translatable(Codex.TITLE));
	}

	@Override
	protected void init() {
		buildPages();
		currentPage = Mth.clamp(lastPage, 0, pages.size() - 1);
		int left = backgroundLeft();
		int top = backgroundTop();
		forwardButton = addRenderableWidget(new PageButton(left + 116, top + 157, true, button -> setPage(currentPage + 1), true));
		backButton = addRenderableWidget(new PageButton(left + 43, top + 157, false, button -> setPage(currentPage - 1), true));
		int buttonsTop = top + IMAGE_SIZE + 2;
		addRenderableWidget(Button.builder(Component.translatable(Codex.CONTENTS), button -> setPage(contentsPage))
				.pos(width / 2 - 100, buttonsTop).width(98).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.pos(width / 2 + 2, buttonsTop).width(98).build());
		updateButtonVisibility();
	}

	private void buildPages() {
		pages.clear();
		anchors.clear();
		//Cover
		Page cover = new Page(new ItemStack(ExpansionItems.ALCHEMISTS_CODEX.get()), Component.translatable(Codex.TITLE));
		pages.add(cover);
		addText(Component.translatable(Codex.INTRO));
		//Contents, the chapter pages are not known yet, so the links point at anchors that are filled in below
		contentsPage = pages.size();
		Page contents = new Page(null, Component.translatable(Codex.CONTENTS));
		pages.add(contents);
		List<Integer> chapterAnchors = new ArrayList<>();
		for (Chapter chapter : Codex.CHAPTERS) {
			int anchor = newAnchor();
			chapterAnchors.add(anchor);
			addText(link(chapter.title(), anchor));
		}
		for (int c = 0; c < Codex.CHAPTERS.size(); c++) {
			Chapter chapter = Codex.CHAPTERS.get(c);
			anchors.set(chapterAnchors.get(c), pages.size());
			Page chapterPage = new Page(chapter.icon(), chapter.title());
			pages.add(chapterPage);
			addText(chapter.intro());
			addText(Component.empty());
			List<Integer> entryAnchors = new ArrayList<>();
			for (Entry entry : chapter.entries()) {
				int anchor = newAnchor();
				entryAnchors.add(anchor);
				addText(link(entry.title(), anchor));
			}
			for (int e = 0; e < chapter.entries().size(); e++) {
				Entry entry = chapter.entries().get(e);
				anchors.set(entryAnchors.get(e), pages.size());
				Page entryPage = new Page(entry.icon(), entry.title());
				pages.add(entryPage);
				addText(entry.text());
			}
		}
	}

	private int newAnchor() {
		anchors.add(0);
		return anchors.size() - 1;
	}

	private static Component link(Component text, int anchor) {
		//The anchor is stored as a one based page number, like the click events of vanilla books
		return Component.literal("• ").append(text).withStyle(LINK_STYLE
				.withClickEvent(new ClickEvent.ChangePage(anchor + 1)));
	}

	/**
	 * Wraps the given text and adds it to the last page, continuing on new pages when it does not fit.
	 */
	private void addText(Component text) {
		Page page = pages.getLast();
		MutableComponent styled = ComponentUtils.mergeStyles(text.copy(), TEXT_STYLE);
		List<FormattedCharSequence> lines = font.split(styled, TEXT_WIDTH);
		if (lines.isEmpty()) {
			lines = List.of(FormattedCharSequence.EMPTY);
		}
		for (FormattedCharSequence line : lines) {
			if (page.isFull()) {
				page = new Page(null, null);
				pages.add(page);
			}
			page.lines.add(line);
		}
	}

	private void setPage(int page) {
		int clamped = Mth.clamp(page, 0, pages.size() - 1);
		if (clamped != currentPage) {
			currentPage = clamped;
			lastPage = clamped;
			updateButtonVisibility();
		}
	}

	private void updateButtonVisibility() {
		forwardButton.visible = currentPage < pages.size() - 1;
		backButton.visible = currentPage > 0;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) {
			return true;
		}
		return switch (event.key()) {
			//Page up and page down, like vanilla books
			case 266 -> {
				backButton.onPress(event);
				yield true;
			}
			case 267 -> {
				forwardButton.onPress(event);
				yield true;
			}
			default -> false;
		};
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0) {
			setPage(currentPage + (scrollY < 0 ? 1 : -1));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			ActiveTextCollector.ClickableStyleFinder finder = new ActiveTextCollector.ClickableStyleFinder(font, (int) event.x(), (int) event.y());
			visitText(finder, true);
			Style clicked = finder.result();
			if (clicked != null && clicked.getClickEvent() instanceof ClickEvent.ChangePage(int target)) {
				int anchor = target - 1;
				if (anchor >= 0 && anchor < anchors.size()) {
					setPage(anchors.get(anchor));
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, backgroundLeft(), backgroundTop(), 0.0F, 0.0F, IMAGE_SIZE, IMAGE_SIZE, 256, 256);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		Page page = pages.get(currentPage);
		if (page.icon != null) {
			graphics.item(page.icon, backgroundLeft() + TEXT_X, backgroundTop() + TEXT_Y - 2);
		}
		visitText(graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR), false);
	}

	private void visitText(ActiveTextCollector collector, boolean clickableOnly) {
		Page page = pages.get(currentPage);
		int left = backgroundLeft() + TEXT_X;
		int top = backgroundTop() + TEXT_Y;
		if (!clickableOnly) {
			Component indicator = Component.translatable("book.pageIndicator", currentPage + 1, pages.size()).withStyle(TEXT_STYLE);
			collector.accept(TextAlignment.RIGHT, backgroundLeft() + 148, backgroundTop() + 16, indicator);
			if (page.title != null) {
				//Titles that are too long for the line are drawn in two lines next to the icon
				int titleX = page.icon == null ? left : left + 20;
				List<FormattedCharSequence> title = font.split(page.title.copy().withStyle(TITLE_STYLE), TEXT_WIDTH - (titleX - left));
				int titleY = title.size() > 1 ? top - 3 : top + 2;
				for (int i = 0; i < Math.min(2, title.size()); i++) {
					collector.accept(titleX, titleY + i * LINE_HEIGHT, title.get(i));
				}
			}
		}
		int firstLine = page.title == null ? 0 : HEADER_LINES;
		for (int i = 0; i < page.lines.size(); i++) {
			collector.accept(left, top + (firstLine + i) * LINE_HEIGHT, page.lines.get(i));
		}
	}

	private int backgroundLeft() {
		return (width - IMAGE_SIZE) / 2;
	}

	private int backgroundTop() {
		return 2;
	}

	private static final class Page {

		@Nullable
		private final ItemStack icon;
		@Nullable
		private final Component title;
		private final List<FormattedCharSequence> lines = new ArrayList<>();

		private Page(@Nullable ItemStack icon, @Nullable Component title) {
			this.icon = icon;
			this.title = title;
		}

		private boolean isFull() {
			return lines.size() >= LINES_PER_PAGE - (title == null ? 0 : HEADER_LINES);
		}
	}
}
