package moze_intel.projecte.gameObjs.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

/**
 * An item with a line of lore under its name, from the language key of the item followed by {@code .lore}.
 */
public class LoreItem extends Item {

	public LoreItem(Properties props) {
		super(props);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(Component.translatable(getDescriptionId() + ".lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
}
