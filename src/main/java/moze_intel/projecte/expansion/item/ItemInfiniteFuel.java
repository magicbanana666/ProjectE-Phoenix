package moze_intel.projecte.expansion.item;

import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.registries.ExpansionDataComponentTypes;
import moze_intel.projecte.expansion.util.ColorStyle;
import moze_intel.projecte.expansion.util.EMCFormat;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.expansion.util.Util;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.UUID;

public class ItemInfiniteFuel extends Item {

	public ItemInfiniteFuel(Properties properties) {
		//Since 26.3 the properties handed in by ItemDeferredRegister already carry the registry id, and since 26.1
		//the crafting remainder is a fixed ItemStackTemplate, so the item hands back a plain copy of itself
		super(properties.stacksTo(1).rarity(Rarity.RARE));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Lang.Items.INFINITE_FUEL_TOOLTIP.translateColored(ChatFormatting.GRAY));
		tooltip.accept(Lang.COST.translateColored(ChatFormatting.RED, EMCFormat.getComponent(Config.server.infiniteFuelCost.get()).setStyle(ColorStyle.GRAY)));
	}

	/**
	 * Burns for the configured amount of time, as long as the owner can pay the EMC price, and pays that price here.
	 * <p>
	 * 26.1 made {@link Item#getCraftingRemainder} a fixed value taken from the item properties, so the addon can no
	 * longer charge the owner when the furnace hands the item back. Asking for the burn time is the only place left
	 * that runs exactly once per lighting of a furnace, which is when the fuel is actually used up.
	 */
	@Override
	public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
		//Mods such as Thermal query burn times before the server config is loaded
		int cost = Config.server.infiniteFuelCost.getOrDefault();
		int burnTime = Config.server.infiniteFuelBurnTime.getOrDefault();
		if (cost == 0 || burnTime == 0) return 0;
		@Nullable ExpansionDataComponentTypes.OwnerData owner = stack.get(ExpansionDataComponentTypes.OWNER);
		@Nullable IKnowledgeProvider provider = owner == null ? null : Util.getKnowledgeProvider(owner.uuid());
		if (owner == null || provider == null || provider.getEmc().compareTo(BigInteger.valueOf(cost)) < 0) return 0;
		provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(cost)));
		ServerPlayer player = Util.getPlayer(owner.uuid());
		if (player != null) provider.syncEmc(player);
		return burnTime;
	}


}
