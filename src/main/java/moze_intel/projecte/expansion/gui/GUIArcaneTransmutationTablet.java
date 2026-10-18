package moze_intel.projecte.expansion.gui;

import com.mojang.blaze3d.platform.InputConstants;
import java.math.BigInteger;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.expansion.gui.container.ContainerArcaneTransmutationTablet;
import moze_intel.projecte.expansion.gui.container.slots.PXOutputSlot;
import moze_intel.projecte.expansion.net.packets.to_server.PacketArcaneTransmutationTabletSmallButton;
import moze_intel.projecte.expansion.util.EMCFormat;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.expansion.util.SearchType;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.gui.PEContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.joml.Matrix3x2fStack;
import org.jetbrains.annotations.NotNull;

public class GUIArcaneTransmutationTablet extends PEContainerScreen<ContainerArcaneTransmutationTablet> {
	public static final Identifier texture = PECore.rl("textures/gui/arcane_transmutation_tablet.png");
	// public static final Identifier texture = PECore.rl("textures/gui/arcane_transmutation_tablet_layout.png");

	private static final int TEXTURE_SIZE = 256;

	private final TransmutationInventory inv;
	private EditBox textBoxFilter;

	public GUIArcaneTransmutationTablet(ContainerArcaneTransmutationTablet container, Inventory invPlayer, Component title) {
		//26.3: imageWidth/imageHeight are final on AbstractContainerScreen, so they have to be given to super
		super(container, invPlayer, title, 176, 217);
		this.inv = container.transmutationInventory;
	}

	private void updateFilter(String text) {
		inv.updateFilter(text);
		ProjectEConfig.client.expansion.searchType.get().sync(text);
	}

	private Button previous, next, rotate, balance, search, clear;
	@Override
	public void init() {
		super.init();

		this.textBoxFilter = addWidget(new EditBox(this.font, leftPos + 7, topPos + 6, 162, 12, Component.empty()));
		textBoxFilter.setResponder(this::updateFilter);

		if (ProjectEConfig.client.expansion.searchType.get().autoFocus) {
			setFocused(textBoxFilter);
		}

		this.previous = addWidget(Button.builder(Component.empty(), (button) -> inv.previousPage())
				.pos(leftPos + 7, topPos + 20)
				.size(18, 18)
				.build());
		this.next = addWidget(Button.builder(Component.empty(), (button) -> inv.nextPage())
				.pos(leftPos + 151, topPos + 20)
				.size(18, 18)
				.build());
		this.rotate = addWidget(Button.builder(Component.empty(), (button) -> {
					menu.action(minecraft.hasShiftDown() ? PacketArcaneTransmutationTabletSmallButton.Action.ROTATE_CC : PacketArcaneTransmutationTabletSmallButton.Action.ROTATE);
				})
				.pos(leftPos - 71, topPos + 16)
				.size(9, 9)
				.build());
		this.balance = addWidget(Button.builder(Component.empty(), (button) -> {
					menu.action(minecraft.hasShiftDown() ? PacketArcaneTransmutationTabletSmallButton.Action.SPREAD : PacketArcaneTransmutationTabletSmallButton.Action.BALANCE);
				})
				.pos(leftPos - 71, topPos + 26)
				.size(9, 9)
				.build());
		this.search = addWidget(Button.builder(Component.empty(), (button) -> {
					ModConfigSpec.ConfigValue<SearchType> config = ProjectEConfig.client.expansion.searchType;
					config.set(config.get().next());
					config.save();
				})
				.pos(leftPos - 71, topPos + 36)
				.size(9, 9)
				.build());
		this.clear = addWidget(Button.builder(Component.empty(), (button) -> {
					menu.action(minecraft.hasShiftDown() ? PacketArcaneTransmutationTabletSmallButton.Action.CLEAR_FORCE : PacketArcaneTransmutationTabletSmallButton.Action.CLEAR);
				})
				.pos(leftPos - 71, topPos + 61)
				.size(9, 9)
				.build());
		updateButtons();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY < 0 && inv.hasNextPage()) inv.nextPage();
		if (scrollY > 0 && inv.hasPreviousPage()) inv.previousPage();
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void resize(int width, int height) {
		String filter = this.textBoxFilter.getValue();
		//26.3: Screen#resize no longer takes the Minecraft instance, and Screen#init(int,int) is final
		super.resize(width, height);
		this.textBoxFilter.setValue(filter);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		updateButtons();
	}

	private void updateButtons() {
		if (previous != null) {
			previous.active = inv.hasPreviousPage();
		}
		if (next != null) {
			next.active = inv.hasNextPage();
		}
	}

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos - 75, topPos + 10, 180, 32, 76, 89, TEXTURE_SIZE, TEXTURE_SIZE);
		this.textBoxFilter.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);

		renderIfHovered(graphics, mouseX, mouseY, previous, 196, 0);
		renderIfHovered(graphics, mouseX, mouseY, next, 215, 0);
		renderIfHovered(graphics, mouseX, mouseY, rotate, 234, 0);
		renderIfHovered(graphics, mouseX, mouseY, balance, 234, 0);
		renderIfHovered(graphics, mouseX, mouseY, search, 234, 0);
		renderIfHovered(graphics, mouseX, mouseY, clear, 234, 0);

		if (menu.isCrafting) { // fill in the crafting arrow
			graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos - 50, topPos + 76, 177, 19, 18, 12, TEXTURE_SIZE, TEXTURE_SIZE);
		}

		if (minecraft.hasShiftDown()) { // make the clear box red when hovering and holding shift
			renderIfHovered(graphics, mouseX, mouseY, clear, 234, 10);
		}
	}

	private void renderIfHovered(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Button button, int uOffset, int vOffset) {
		if (button.isMouseOver(mouseX, mouseY)) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, texture, button.getX(), button.getY(), uOffset, vOffset, button.getWidth(), button.getHeight(), TEXTURE_SIZE, TEXTURE_SIZE);
		}
	}

	@Override
	protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		BigInteger emc = inv.getAvailableEmc();
		String emcFmt = EMCFormat.format(emc);
		graphics.text(font, emcFmt, (imageWidth - font.width(emcFmt)) / 2, -9, 0xFFB5B5B5, true);

		if (inv.learnFlag > 0) {
			Component learned = Lang.GUI.ARCANE_TRANSMUTATION_TABLET_LEARNED.translate();
			graphics.text(font, learned, 170 - font.width(learned), 60, 0xFFB5B5B5, false);
			inv.learnFlag--;
		}

		if (inv.unlearnFlag > 0) {
			graphics.text(font, Lang.GUI.ARCANE_TRANSMUTATION_TABLET_UNLEARNED.translate(), 6, 60, 0xFFB5B5B5, false);
			inv.unlearnFlag--;
		}

		//26.3: the GUI transform is a 2D Matrix3x2fStack, so the "draw on top of the slots" part of the old
		// translate(x, y, 1000) is now done by moving to the next render stratum
		graphics.nextStratum();
		Matrix3x2fStack pose = graphics.pose();
		for (PXOutputSlot slot : menu.getOutputSlots()) {
			long value = IEMCProxy.INSTANCE.getValue(slot.getItem());
			if (value <= 0) continue;
			BigInteger count = emc.equals(BigInteger.ZERO) ? emc : emc.divide(BigInteger.valueOf(value));
			String countFmt = EMCFormat.formatForceShort(count);
			pose.pushMatrix();
			pose.translate(slot.x + 17, slot.y + 12);
			pose.scale(0.5F, 0.5F);
			graphics.text(font, countFmt, -font.width(countFmt), 0, 0xFFFFFFFF, true);
			pose.popMatrix();
		}
	}

	@Override
	public boolean keyPressed(@NotNull KeyEvent event) {
		if (textBoxFilter.isFocused()) {
			//Manually make it so that hitting escape when the filter is focused will exit the focus
			if (event.isEscape()) {
				textBoxFilter.setFocused(false);
				return true;
			}
			//Otherwise have it handle the key press
			//This is where key combos and deletion is handled, and where we bypass the inventory key closing the screen
			return textBoxFilter.keyPressed(event);
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		if (textBoxFilter.isMouseOver(event.x(), event.y())) {
			if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
				//Note: Clearing filter will be handled by the text box's responder
				this.textBoxFilter.setValue("");
			}
		} else if (textBoxFilter.isFocused()) {
			if (hoveredSlot == null || (!hoveredSlot.hasItem() && menu.getCarried().isEmpty())) {
				textBoxFilter.setFocused(false);
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void removed() {
		super.removed();
		inv.learnFlag = 0;
		inv.unlearnFlag = 0;
	}

	//26.3: there is no renderTooltip override anymore, AbstractContainerScreen#extractTooltip is invoked at the
	// end of extractRenderState and the tooltip text is what we hand to the extractor
	@Override
	protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			super.extractTooltip(graphics, mouseX, mouseY);
			return;
		}

		int width = 16, height = 16,
				lockX = leftPos + menu.lockX, lockY = topPos + menu.lockY,
				consumeX = leftPos + menu.consumeX, consumeY = topPos + menu.consumeY,
				unlearnX  = leftPos + menu.unlearnX, unlearnY = topPos + menu.unlearnY;

		if (mouseX > lockX && mouseX < (lockX + width) && mouseY > lockY && mouseY < (lockY + height)) {
			graphics.setTooltipForNextFrame(Lang.GUI.ARCANE_TRANSMUTATION_TABLET_LOCK.translate(), mouseX, mouseY);
		} else if (mouseX > consumeX && mouseX < (consumeX + width) && mouseY > consumeY && mouseY < (consumeY + height)) {
			graphics.setTooltipForNextFrame(Lang.GUI.ARCANE_TRANSMUTATION_TABLET_CONSUME.translate(), mouseX, mouseY);
		} else if (mouseX > unlearnX && mouseX < (unlearnX + width) && mouseY > unlearnY && mouseY < (unlearnY + height)) {
			graphics.setTooltipForNextFrame(Lang.GUI.ARCANE_TRANSMUTATION_TABLET_UNLEARN.translate(), mouseX, mouseY);
		} else if (rotate.isMouseOver(mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(Lang.GUI.ARCANE_TRANSMUTATION_TABLET_ROTATE.translate().append(Component.literal(": ")).append(
					minecraft.hasShiftDown() ? Lang.GUI.ARCANE_TRANSMUTATION_TABLET_ROTATE_COUNTER_CLOCKWISE.translateColored(ChatFormatting.GRAY) : Lang.GUI.ARCANE_TRANSMUTATION_TABLET_ROTATE_CLOCKWISE.translateColored(ChatFormatting.GRAY)
			), mouseX, mouseY);
		} else if (balance.isMouseOver(mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(minecraft.hasShiftDown() ? Lang.GUI.ARCANE_TRANSMUTATION_TABLET_SPREAD.translate() : Lang.GUI.ARCANE_TRANSMUTATION_TABLET_BALANCE.translate(), mouseX, mouseY);
		} else if (search.isMouseOver(mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(Lang.GUI.ARCANE_TRANSMUTATION_TABLET_SEARCH_TYPE.translate().append(Component.literal(": ")).append(
					ProjectEConfig.client.expansion.searchType.get().translateColored(ChatFormatting.GRAY)
			), mouseX, mouseY);
		} else if (clear.isMouseOver(mouseX, mouseY)) {
			MutableComponent tooltip = Lang.GUI.ARCANE_TRANSMUTATION_TABLET_CLEAR.translate();
			graphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
		} else {
			super.extractTooltip(graphics, mouseX, mouseY);
		}
	}
}
