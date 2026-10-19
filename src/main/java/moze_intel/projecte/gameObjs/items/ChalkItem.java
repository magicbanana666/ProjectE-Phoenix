package moze_intel.projecte.gameObjs.items;

import java.util.function.Consumer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

/**
 * Chalk of one of the three principles. Used on an Alchemical Cauldron it draws the circle of its principle around it: salt
 * the smallest, sulphur the middle, mercury the largest. Drawing is done by the cauldron, see
 * {@link moze_intel.projecte.gameObjs.block_entities.AlchemicalCauldronBlockEntity#drawCircle}.
 */
public class ChalkItem extends Item {

	/**
	 * How much of a chalk one circle uses up.
	 */
	public static final int USES_PER_CIRCLE = 16;

	private final int circle;

	public ChalkItem(Properties props, int circle) {
		super(props);
		this.circle = circle;
	}

	/**
	 * @return the circle this chalk draws, 1 for the smallest
	 */
	public int getCircle() {
		return circle;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(PELang.TOOLTIP_CHALK.translateColored(ChatFormatting.GRAY, circle));
	}
}
