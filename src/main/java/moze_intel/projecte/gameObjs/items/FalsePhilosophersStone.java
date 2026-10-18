package moze_intel.projecte.gameObjs.items;

import java.util.function.Consumer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * What the old Philosopher's Stone recipe makes now. It has none of the powers of the real stone: it only converts fuels up
 * to Aeternalis Fuel and makes a Transmutation Table. Three of them and a catalyst become the real stone in the
 * Alchemical Crucible.
 */
public class FalsePhilosophersStone extends Item {

	private static final DustParticleOptions UNSTABLE_GLOW = new DustParticleOptions(0x5A0A12, 0.6F);

	public FalsePhilosophersStone(Properties props) {
		super(props);
	}

	//Like the real stone it stays in the crafting grid. Since 26.1 the remainder is an ItemStackTemplate
	@Override
	public ItemStackTemplate getCraftingRemainder(@NotNull ItemInstance instance) {
		DataComponentPatch patch = instance instanceof ItemStack stack ? stack.getComponentsPatch() : DataComponentPatch.EMPTY;
		return new ItemStackTemplate(instance.typeHolder(), instance.count(), patch);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		//A weak, unstable glow while it is held
		if (slot != null && slot.getType() == EquipmentSlot.Type.HAND && level.getRandom().nextInt(30) == 0) {
			level.sendParticles(UNSTABLE_GLOW, owner.getX(), owner.getY() + owner.getBbHeight() * 0.6, owner.getZ(), 2, 0.3, 0.2, 0.3, 0);
		}
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(PELang.TOOLTIP_FALSE_PHILOSTONE_1.translateColored(ChatFormatting.GRAY));
		tooltip.accept(PELang.TOOLTIP_FALSE_PHILOSTONE_2.translateColored(ChatFormatting.DARK_RED));
	}
}
