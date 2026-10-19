package moze_intel.projecte.gameObjs;

import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.jetbrains.annotations.Nullable;

/**
 * The Dragon's Pulse: the flow of the earth, stronger in some places than others. Every chunk has a strength from 0 to 15.
 * It follows from the world seed alone, so it costs nothing to store and is the same for everyone, and it runs in long
 * veins, stronger along rivers and under mountains.
 * <p>
 * An Athanor on a strong pulse works for less EMC, and pulse arrays draw their power from it.
 */
public final class DragonsPulse {

	public static final int MAX = 15;
	/**
	 * From this strength on, the pulse is strong enough to ease the work of the Athanor.
	 */
	public static final int STRONG = 10;
	/**
	 * How many chunks one swell of the pulse spans.
	 */
	private static final double SCALE = 7.0;

	@Nullable
	private static ImprovedNoise noise;
	private static long noiseSeed;

	private DragonsPulse() {
	}

	/**
	 * @return the strength of the pulse in the chunk the position is in, from 0 to {@link #MAX}
	 */
	public static int strength(ServerLevel level, BlockPos pos) {
		ChunkPos chunk = ChunkPos.containing(pos);
		double value = noise(level.getSeed()).noise(chunk.x() / SCALE, 0.5, chunk.z() / SCALE);
		//The noise lies roughly between -1 and 1: most chunks are weak, and the veins between them strong
		int strength = Mth.floor((value + 0.6) / 1.4 * (MAX + 1));
		Holder<Biome> biome = level.getBiome(pos);
		if (biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_MOUNTAIN)) {
			strength += 3;
		}
		return Mth.clamp(strength, 0, MAX);
	}

	private static ImprovedNoise noise(long seed) {
		if (noise == null || noiseSeed != seed) {
			noise = new ImprovedNoise(RandomSource.create(seed ^ 0x5EED_D2A6_0F00L));
			noiseSeed = seed;
		}
		return noise;
	}

	public static boolean isStrong(ServerLevel level, BlockPos pos) {
		return strength(level, pos) >= STRONG;
	}

	public static Component describe(int strength) {
		ChatFormatting color = strength >= STRONG ? ChatFormatting.LIGHT_PURPLE : strength >= 5 ? ChatFormatting.AQUA : ChatFormatting.GRAY;
		return PELang.DRAGONS_PULSE.translateColored(color, strength, MAX);
	}
}
