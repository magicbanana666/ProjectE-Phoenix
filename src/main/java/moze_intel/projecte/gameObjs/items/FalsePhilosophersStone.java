package moze_intel.projecte.gameObjs.items;

import java.util.function.Consumer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Red Water crystallised around a diamond in the Alchemical Cauldron. It has none of the powers of the real stone: it only
 * converts fuels up to Aeternalis Fuel and makes a Transmutation Table, and every use wears it down until it crumbles.
 * Three of them and a catalyst become the real stone in the Athanor.
 */
public class FalsePhilosophersStone extends Item {

	/**
	 * How many crafts a stone survives.
	 */
	public static final int USES = 256;
	private static final DustParticleOptions UNSTABLE_GLOW = new DustParticleOptions(0x5A0A12, 0.6F);

	public FalsePhilosophersStone(Properties props) {
		super(props);
	}

	//Like the real stone it stays in the crafting grid, unless the craft wore it out. Since 26.1 the remainder is an ItemStackTemplate.
	// The remainder has to be the stone exactly as it was, or EMC mapping sees a different item and every recipe with the stone loses its value
	@Nullable
	@Override
	public ItemStackTemplate getCraftingRemainder(@NotNull ItemInstance instance) {
		DataComponentPatch patch = instance instanceof ItemStack stack ? stack.getComponentsPatch() : DataComponentPatch.EMPTY;
		if (instance instanceof ItemStack stack && stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
			//It crumbles
			return null;
		}
		return new ItemStackTemplate(instance.typeHolder(), instance.count(), patch);
	}

	/**
	 * Wears down every False Philosopher's Stone in a crafting grid when something is crafted with it. This happens before the
	 * grid is emptied, so the stone that stays behind is the worn one.
	 */
	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		if (event.getEntity().level().isClientSide()) {
			return;
		}
		Container grid = event.getInventory();
		for (int slot = 0; slot < grid.getContainerSize(); slot++) {
			ItemStack stack = grid.getItem(slot);
			if (stack.getItem() instanceof FalsePhilosophersStone && stack.isDamageableItem()) {
				stack.setDamageValue(stack.getDamageValue() + 1);
			}
		}
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
