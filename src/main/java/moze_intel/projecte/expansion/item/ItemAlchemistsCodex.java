package moze_intel.projecte.expansion.item;

import java.util.function.Consumer;
import moze_intel.projecte.expansion.util.ClientSideHandler;
import moze_intel.projecte.expansion.util.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * The in game guide of the mod. Using it opens a book that explains every block and item.
 */
public class ItemAlchemistsCodex extends Item {

	public ItemAlchemistsCodex(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			ClientSideHandler.openCodex();
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, display, tooltip, tooltipFlag);
		tooltip.accept(Lang.Items.ALCHEMISTS_CODEX_TOOLTIP.translateColored(ChatFormatting.GRAY));
	}
}
