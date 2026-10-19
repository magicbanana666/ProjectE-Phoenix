package moze_intel.projecte.gameObjs.customRecipes.athanor;

import com.mojang.serialization.Codec;
import java.util.Locale;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * What the Athanor is doing. The active stages are the kinds of stage an {@link AthanorRecipe} can be made of: each has its
 * own sounds, particles and movement of the floating items, and decides how hard a ritual rebounds when it fails in it.
 */
public enum OpusStage implements StringRepresentable {
	IDLE(ChatFormatting.GRAY, null, null, 0, 1.0F, RuneColor.FORMED),
	NIGREDO(ChatFormatting.DARK_PURPLE, SoundEvents.WITHER_SPAWN, SoundEvents.DEEPSLATE_BREAK, 30, 0.5F, RuneColor.NIGREDO),
	CAUDA_PAVONIS(ChatFormatting.LIGHT_PURPLE, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundEvents.AMETHYST_CLUSTER_STEP, 12, 1.4F, RuneColor.MAGENTA),
	ALBEDO(ChatFormatting.WHITE, SoundEvents.BEACON_ACTIVATE, SoundEvents.AMETHYST_BLOCK_CHIME, 15, 1.2F, RuneColor.ALBEDO),
	CITRINITAS(ChatFormatting.GOLD, SoundEvents.BEACON_POWER_SELECT, SoundEvents.BEACON_AMBIENT, 40, 1.0F, RuneColor.CITRINITAS),
	RUBEDO(ChatFormatting.RED, SoundEvents.END_PORTAL_SPAWN, SoundEvents.WARDEN_HEARTBEAT, 20, 0.8F, RuneColor.RUBEDO),
	COMPLETE(ChatFormatting.GOLD, null, null, 0, 1.0F, RuneColor.COMPLETE);

	/**
	 * Only the active stages, the ones a recipe can use.
	 */
	public static final Codec<OpusStage> CODEC = StringRepresentable.fromEnum(OpusStage::values).validate(stage -> stage.isActive()
			? com.mojang.serialization.DataResult.success(stage)
			: com.mojang.serialization.DataResult.error(() -> stage.getSerializedName() + " is not a stage a ritual can have"));

	private final ChatFormatting color;
	@Nullable
	private final SoundEvent startSound;
	@Nullable
	private final SoundEvent ambience;
	private final int ambienceInterval;
	private final float pitch;
	private final RuneColor defaultRune;

	OpusStage(ChatFormatting color, @Nullable SoundEvent startSound, @Nullable SoundEvent ambience, int ambienceInterval, float pitch, RuneColor defaultRune) {
		this.color = color;
		this.startSound = startSound;
		this.ambience = ambience;
		this.ambienceInterval = ambienceInterval;
		this.pitch = pitch;
		this.defaultRune = defaultRune;
	}

	public boolean isActive() {
		return this != IDLE && this != COMPLETE;
	}

	/**
	 * @return whether running out of EMC in this stage makes the ritual rebound instead of only waiting. The early stages are
	 * forgiving, once the work is yellowing or reddening there is no way back.
	 */
	public boolean reboundsWithoutEmc() {
		return this == CITRINITAS || this == RUBEDO;
	}

	public ChatFormatting getColor() {
		return color;
	}

	@Nullable
	public SoundEvent getStartSound() {
		return startSound;
	}

	@Nullable
	public SoundEvent getAmbience() {
		return ambience;
	}

	public int getAmbienceInterval() {
		return ambienceInterval;
	}

	public float getPitch() {
		return pitch;
	}

	public RuneColor getDefaultRune() {
		return defaultRune;
	}

	@NotNull
	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public Component getName() {
		return switch (this) {
			case NIGREDO -> PELang.RITUAL_NIGREDO.translate();
			case CAUDA_PAVONIS -> PELang.RITUAL_CAUDA_PAVONIS.translate();
			case ALBEDO -> PELang.RITUAL_ALBEDO.translate();
			case CITRINITAS -> PELang.RITUAL_CITRINITAS.translate();
			case RUBEDO -> PELang.RITUAL_RUBEDO.translate();
			default -> PELang.RITUAL_READY.translate();
		};
	}

	/**
	 * @return the variant of the False Philosopher's Stone model that shows the stones in this stage
	 */
	@Nullable
	public String stoneModel() {
		return switch (this) {
			case NIGREDO, ALBEDO, CITRINITAS, RUBEDO -> getSerializedName();
			case CAUDA_PAVONIS -> NIGREDO.getSerializedName();
			default -> null;
		};
	}

	public static OpusStage byName(String name) {
		for (OpusStage stage : values()) {
			if (stage.getSerializedName().equals(name)) {
				return stage;
			}
		}
		return IDLE;
	}
}
