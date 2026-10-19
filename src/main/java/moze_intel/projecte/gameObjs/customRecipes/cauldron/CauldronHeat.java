package moze_intel.projecte.gameObjs.customRecipes.cauldron;

import com.mojang.serialization.Codec;
import java.util.Locale;
import moze_intel.projecte.gameObjs.PETags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

/**
 * How hot an Alchemical Cauldron is, decided by the block right under it. Which blocks give which heat is up to the block
 * tags {@code projecte:cauldron_heat/*}, a campfire only counts while it is lit.
 */
public enum CauldronHeat implements StringRepresentable {
	NONE,
	COLD,
	LOW,
	HIGH,
	SOUL;

	public static final Codec<CauldronHeat> CODEC = StringRepresentable.fromEnum(CauldronHeat::values);

	public static CauldronHeat below(BlockGetter level, BlockPos cauldron) {
		BlockState state = level.getBlockState(cauldron.below());
		if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
			return NONE;
		} else if (state.is(PETags.Blocks.CAULDRON_HEAT_SOUL)) {
			return SOUL;
		} else if (state.is(PETags.Blocks.CAULDRON_HEAT_HIGH)) {
			return HIGH;
		} else if (state.is(PETags.Blocks.CAULDRON_HEAT_LOW)) {
			return LOW;
		} else if (state.is(PETags.Blocks.CAULDRON_HEAT_COLD)) {
			return COLD;
		}
		return NONE;
	}

	@NotNull
	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
