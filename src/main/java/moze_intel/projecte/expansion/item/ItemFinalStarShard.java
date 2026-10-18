package moze_intel.projecte.expansion.item;

import moze_intel.projecte.expansion.util.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ItemFinalStarShard extends Item {
	public ItemFinalStarShard(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Lang.Items.FINAL_STAR_SHARD_TOOLTIP.translateColored(ChatFormatting.GRAY));
	}
}

