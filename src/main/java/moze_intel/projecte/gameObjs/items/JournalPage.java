package moze_intel.projecte.gameObjs.items;

import java.util.List;
import java.util.function.Consumer;
import moze_intel.projecte.expansion.util.ClientSideHandler;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * A page torn from the journal of the last keeper of an Athanor, found in ancient cities, strongholds, trial chambers and end
 * cities. Read, it tells a part of the story, see the {@code journal.projecte.page.*} language keys.
 */
public class JournalPage extends Item {

	public static final int PAGES = 8;

	public JournalPage(Properties props) {
		super(props);
	}

	public static int getPage(ItemStack stack) {
		return Math.clamp(stack.getOrDefault(PEDataComponentTypes.JOURNAL_PAGE, 1), 1, PAGES);
	}

	@NotNull
	@Override
	public InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (level.isClientSide()) {
			int page = getPage(player.getItemInHand(hand));
			ClientSideHandler.openBook(List.of(Component.translatable("journal.projecte.page." + page)));
		}
		return InteractionResult.SUCCESS;
	}

	@NotNull
	@Override
	public Component getName(@NotNull ItemStack stack) {
		return Component.translatable(getDescriptionId(), getPage(stack), PAGES);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(Component.translatable(getDescriptionId() + ".lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
}
