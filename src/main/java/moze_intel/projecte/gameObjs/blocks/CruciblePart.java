package moze_intel.projecte.gameObjs.blocks;

import java.util.function.Consumer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * A block of the Alchemical Crucible. The core of the array sets {@link #GLOW} on every part, which shows the glowing
 * runes and crystals in the colour of the current stage of the ritual.
 */
public class CruciblePart extends Block implements IBlockTooltip {

	/**
	 * 0: not part of a complete array, 1: formed, 2 to 5: the four stages of the ritual, 6: the ritual is complete.
	 */
	public static final IntegerProperty GLOW = IntegerProperty.create("glow", 0, 6);
	private static final int[] LIGHT = {0, 6, 4, 10, 13, 15, 12};

	private final VoxelShape shape;

	public CruciblePart(Properties props, VoxelShape shape) {
		super(props);
		this.shape = shape;
		registerDefaultState(getStateDefinition().any().setValue(GLOW, 0));
	}

	public static int getLight(BlockState state) {
		return LIGHT[state.getValue(GLOW)];
	}

	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(GLOW);
	}

	@NotNull
	@Override
	@Deprecated
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
		return shape;
	}

	@Override
	@Deprecated
	public boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flags) {
		tooltip.accept(PELang.TOOLTIP_CRUCIBLE_PART.translateColored(ChatFormatting.GRAY));
	}
}
