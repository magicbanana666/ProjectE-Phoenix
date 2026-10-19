package moze_intel.projecte.gameObjs.customRecipes.athanor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;
import java.util.Map;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * The colours the runes and crystals of the Athanor can show. The ordinal is the value of
 * {@link moze_intel.projecte.gameObjs.blocks.CruciblePart#GLOW}, every colour has its own models and textures.
 * <p>
 * The colours of the Cauda Pavonis, the peacock's tail between Nigredo and Albedo, are there for the rituals of the coloured
 * matters. Yellow and white are Citrinitas and Albedo.
 */
public enum RuneColor implements StringRepresentable {
	OFF(0x000000, 0),
	FORMED(0xFF3040, 6),
	NIGREDO(0x8C8C98, 4),
	ALBEDO(0xFFFFFF, 10),
	CITRINITAS(0xFFC83A, 13),
	RUBEDO(0xFF2A3A, 15),
	COMPLETE(0xFFD650, 12),
	MAGENTA(0xFF4FD8, 12),
	PINK(0xFF9EC8, 12),
	PURPLE(0xB050F0, 12),
	VIOLET(0x8C6EFF, 12),
	BLUE(0x4678FF, 12),
	CYAN(0x30E0E8, 12),
	GREEN(0x46D246, 12),
	LIME(0x9CFF40, 12),
	ORANGE(0xFF8A20, 12);

	/**
	 * Other names the recipes may use, the plain colour of each stage of the Magnum Opus.
	 */
	private static final Map<String, RuneColor> ALIASES = Map.of(
			"black", NIGREDO,
			"white", ALBEDO,
			"yellow", CITRINITAS,
			"red", RUBEDO
	);
	public static final Codec<RuneColor> CODEC = Codec.STRING.comapFlatMap(RuneColor::byName, RuneColor::getSerializedName);

	private final int rgb;
	private final int light;

	RuneColor(int rgb, int light) {
		this.rgb = rgb;
		this.light = light;
	}

	public int getRgb() {
		return rgb;
	}

	public int getLight() {
		return light;
	}

	@NotNull
	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static RuneColor byGlow(int glow) {
		RuneColor[] values = values();
		return glow >= 0 && glow < values.length ? values[glow] : OFF;
	}

	private static DataResult<RuneColor> byName(String name) {
		String lower = name.toLowerCase(Locale.ROOT);
		RuneColor alias = ALIASES.get(lower);
		if (alias != null) {
			return DataResult.success(alias);
		}
		for (RuneColor color : values()) {
			if (color.getSerializedName().equals(lower)) {
				return DataResult.success(color);
			}
		}
		return DataResult.error(() -> "Unknown rune colour: " + name);
	}
}
