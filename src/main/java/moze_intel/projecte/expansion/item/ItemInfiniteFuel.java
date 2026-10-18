package moze_intel.projecte.expansion.item;

import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.registries.ExpansionDataComponentTypes;
import moze_intel.projecte.expansion.util.ColorStyle;
import moze_intel.projecte.expansion.util.EMCFormat;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.expansion.util.Util;
import moze_intel.projecte.gameObjs.registries.PEContextIntProviders;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.TooltipDisplay;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.UUID;

public class ItemInfiniteFuel extends Item {

	public ItemInfiniteFuel(Properties properties) {
		//Since 26.3 the properties handed in by ItemDeferredRegister already carry the registry id, and since 26.1
		//the crafting remainder is a fixed ItemStackTemplate, so the item hands back a plain copy of itself
		//Since 26.3 the burn time and the EMC charge come from the cooking fuel component, see InfiniteFuelBurnTime
		super(properties.stacksTo(1).rarity(Rarity.RARE).cookingFuel(PEContextIntProviders.COOKING_TIME_INFINITE_FUEL));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Lang.Items.INFINITE_FUEL_TOOLTIP.translateColored(ChatFormatting.GRAY));
		tooltip.accept(Lang.COST.translateColored(ChatFormatting.RED, EMCFormat.getComponent(Config.server.infiniteFuelCost.get()).setStyle(ColorStyle.GRAY)));
	}

}
