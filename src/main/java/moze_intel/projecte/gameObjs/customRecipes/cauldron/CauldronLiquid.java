package moze_intel.projecte.gameObjs.customRecipes.cauldron;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * What an Alchemical Cauldron is filled with.
 */
public enum CauldronLiquid implements StringRepresentable {
	EMPTY(0),
	WATER(0x3F76E4),
	RED_WATER(0xB0121F);

	public static final Codec<CauldronLiquid> CODEC = StringRepresentable.fromEnum(CauldronLiquid::values);

	private final int color;

	CauldronLiquid(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	@NotNull
	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static CauldronLiquid byName(String name) {
		for (CauldronLiquid liquid : values()) {
			if (liquid.getSerializedName().equals(name)) {
				return liquid;
			}
		}
		return EMPTY;
	}
}
