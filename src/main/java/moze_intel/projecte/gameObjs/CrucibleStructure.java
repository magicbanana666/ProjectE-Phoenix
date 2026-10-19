package moze_intel.projecte.gameObjs;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.gameObjs.blocks.CrucibleArch;
import moze_intel.projecte.gameObjs.blocks.CruciblePart;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The shape of the Athanor, relative to the crucible block in its centre:
 * <ul>
 *     <li>below it a 5 x 5 base: nine Plinths in the middle, sixteen Steps around them</li>
 *     <li>around it eight Dais, and a Pillar on each corner of the base, three blocks high</li>
 *     <li>on top of every pillar an Arch, a second arch one block in and up, and the Apex above the centre</li>
 * </ul>
 * The shape is the same in every direction, so it does not have to be rotated.
 * <p>
 * Gilding the four inner arches and setting a Quintessence Apex on top makes it the Grand Athanor, which holds five
 * ingredients instead of three.
 */
public final class CrucibleStructure {

	public static final int ATHANOR = 1;
	public static final int GRAND_ATHANOR = 2;

	@Nullable
	private static List<Part> parts;

	private CrucibleStructure() {
	}

	/**
	 * @return every block of the crucible, relative to the crucible block itself
	 */
	public static List<Part> getParts() {
		if (parts == null) {
			List<Part> list = new ArrayList<>();
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					boolean inner = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
					boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
					list.add(new Part(new BlockPos(dx, -1, dz), inner ? PEBlocks.CRUCIBLE_PLINTH : PEBlocks.CRUCIBLE_STEP, null));
					if (dx == 0 && dz == 0) {
						list.add(new Part(BlockPos.ZERO, PEBlocks.ALCHEMICAL_CRUCIBLE, null));
					} else if (inner) {
						list.add(new Part(new BlockPos(dx, 0, dz), PEBlocks.CRUCIBLE_DAIS, null));
					}
					if (corner) {
						for (int y = 0; y < 3; y++) {
							list.add(new Part(new BlockPos(dx, y, dz), PEBlocks.CRUCIBLE_PILLAR, null));
						}
						list.add(new Part(new BlockPos(dx, 3, dz), PEBlocks.CRUCIBLE_ARCH, null));
					}
					if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
						list.add(new Part(new BlockPos(dx, 4, dz), PEBlocks.CRUCIBLE_ARCH, PEBlocks.GILDED_ARCH));
					}
				}
			}
			list.add(new Part(new BlockPos(0, 4, 0), PEBlocks.CRUCIBLE_APEX, PEBlocks.QUINTESSENCE_APEX));
			parts = List.copyOf(list);
		}
		return parts;
	}

	/**
	 * @return the first block of the crucible around the given centre that is missing, or null if it is complete
	 */
	@Nullable
	public static Part findMissing(Level level, BlockPos centre) {
		for (Part part : getParts()) {
			if (!part.matches(level.getBlockState(centre.offset(part.offset())))) {
				return part;
			}
		}
		return null;
	}

	public static int countMissing(Level level, BlockPos centre) {
		int missing = 0;
		for (Part part : getParts()) {
			if (!part.matches(level.getBlockState(centre.offset(part.offset())))) {
				missing++;
			}
		}
		return missing;
	}

	/**
	 * @return 0 if the Athanor is incomplete, {@link #ATHANOR} if it is complete, {@link #GRAND_ATHANOR} if every block that
	 * can be gilded is
	 */
	public static int getTier(Level level, BlockPos centre) {
		boolean grand = true;
		for (Part part : getParts()) {
			BlockState state = level.getBlockState(centre.offset(part.offset()));
			if (!part.matches(state)) {
				return 0;
			}
			if (part.grand() != null && !state.is(part.grand().getBlock())) {
				grand = false;
			}
		}
		return grand ? GRAND_ATHANOR : ATHANOR;
	}

	/**
	 * Lights up every block of the crucible that is in place, and turns the arches towards the centre.
	 */
	public static void update(Level level, BlockPos centre, int glow) {
		for (Part part : getParts()) {
			BlockPos pos = centre.offset(part.offset());
			BlockState state = level.getBlockState(pos);
			if (!part.matches(state)) {
				continue;
			}
			BlockState updated = state.setValue(CruciblePart.GLOW, glow);
			if (state.getBlock() instanceof CrucibleArch) {
				updated = updated.setValue(CrucibleArch.CORNER, CrucibleArch.cornerOf(part.offset().getX(), part.offset().getZ()))
						.setValue(CrucibleArch.UPPER, part.offset().getY() == 4);
			}
			if (updated != state) {
				level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
			}
		}
	}

	/**
	 * @param grand the gilded block that may take the place of this one, and makes the Grand Athanor
	 */
	public record Part(BlockPos offset, BlockRegistryObject<? extends CruciblePart, ?> block, @Nullable BlockRegistryObject<? extends CruciblePart, ?> grand) {

		public boolean matches(BlockState state) {
			return state.is(block.getBlock()) || grand != null && state.is(grand.getBlock());
		}
	}
}
