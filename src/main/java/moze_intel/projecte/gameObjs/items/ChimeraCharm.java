package moze_intel.projecte.gameObjs.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Two souls brewed together with something of the beasts they came from. Carried anywhere in the inventory, the charm lends
 * its bearer what those beasts could do.
 */
public class ChimeraCharm extends Item {

	private final Holder<MobEffect> effect;

	public ChimeraCharm(Properties props, Holder<MobEffect> effect) {
		super(props);
		this.effect = effect;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		//Refreshed well before it runs out, so night vision does not flicker
		if (owner instanceof LivingEntity living && level.getGameTime() % 80 == 0) {
			living.addEffect(new MobEffectInstance(effect, 300, 0, true, false, true));
		}
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(Component.translatable(getDescriptionId() + ".lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		tooltip.accept(effect.value().getDisplayName().copy().withStyle(ChatFormatting.BLUE));
	}
}
