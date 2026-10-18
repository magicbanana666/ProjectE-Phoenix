package moze_intel.projecte.expansion.item;

import moze_intel.projecte.expansion.gui.container.ContainerArcaneTransmutationTablet;
import moze_intel.projecte.expansion.util.ContainerData;
import moze_intel.projecte.api.item.ITransmutationTablet;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItemArcaneTransmutationTablet extends Item implements ITransmutationTablet {
	public ItemArcaneTransmutationTablet(Properties properties) {
		super(properties.rarity(Rarity.RARE).stacksTo(1).fireResistant());
	}

	//26.3 appendHoverText takes a TooltipDisplay and a Consumer<Component> instead of a List<Component>
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, display, tooltip, tooltipFlag);
		tooltip.accept(Lang.Items.ARCANE_TRANSMUTATION_TABLET_TOOLTIP.translateColored(ChatFormatting.GRAY));
	}

	//26.3 Item#use returns an InteractionResult instead of an InteractionResultHolder<ItemStack>
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			openContainer(player, hand, player.getInventory().getSelectedSlot());
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void openContainer(Player player, InteractionHand hand, int selected) {
		player.openMenu(new Provider(hand), (buf) -> ContainerData.inHand(buf, hand, selected));
	}

	@Override
	public void openContainer(Player player) {
		player.openMenu(new Provider(null), ContainerData::noHand);
	}

	private record Provider(@Nullable InteractionHand hand) implements MenuProvider {
		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (provider == null) return null;
			return new ContainerArcaneTransmutationTablet(windowId, inventory, provider, hand, inventory.getSelectedSlot());
		}

		@Override
		public Component getDisplayName() {
			return Lang.GUI.ARCANE_TRANSMUTATION_TABLET.translate();
		}

	}
}
