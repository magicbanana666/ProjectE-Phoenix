package moze_intel.projecte.gameObjs.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * An arch of the Alchemical Crucible. Every arch runs from the top of a corner pillar up to the apex in the centre; the
 * crucible tells each arch block which corner it belongs to and whether it is the lower or the upper half of its arch, so
 * the player does not have to place them in a particular direction.
 */
public class CrucibleArch extends CruciblePart {

	/**
	 * 0: south east, 1: south west, 2: north west, 3: north east. The model is turned by 90 degrees for every step.
	 */
	public static final IntegerProperty CORNER = IntegerProperty.create("corner", 0, 3);
	public static final BooleanProperty UPPER = BooleanProperty.create("upper");

	public CrucibleArch(Properties props, VoxelShape shape) {
		super(props, shape);
		registerDefaultState(defaultBlockState().setValue(CORNER, 0).setValue(UPPER, false));
	}

	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(CORNER, UPPER);
	}

	/**
	 * @return the corner of the crucible an arch at the given offset from the crucible belongs to
	 */
	public static int cornerOf(int dx, int dz) {
		if (dx > 0) {
			return dz > 0 ? 0 : 3;
		}
		return dz > 0 ? 1 : 2;
	}
}
