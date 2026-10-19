package moze_intel.projecte.gameObjs.customRecipes.athanor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * One stage of an {@link AthanorRecipe}: what kind of stage it is, the colour of the runes while it runs, how long it takes,
 * the personal EMC its owner pays over that time, and what the world has to be like for it to continue.
 */
public record AthanorStage(OpusStage stage, RuneColor rune, int ticks, long emc, Optional<Requirement> needs) {

	public static final Codec<AthanorStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			OpusStage.CODEC.fieldOf("stage").forGetter(AthanorStage::stage),
			RuneColor.CODEC.optionalFieldOf("rune").forGetter(stage -> Optional.of(stage.rune())),
			ExtraCodecs.POSITIVE_INT.fieldOf("ticks").forGetter(AthanorStage::ticks),
			Codec.LONG.validate(emc -> emc >= 0 ? com.mojang.serialization.DataResult.success(emc)
					: com.mojang.serialization.DataResult.error(() -> "EMC can not be negative")).optionalFieldOf("emc", 0L).forGetter(AthanorStage::emc),
			Requirement.CODEC.optionalFieldOf("needs").forGetter(AthanorStage::needs)
	).apply(instance, (stage, rune, ticks, emc, needs) -> new AthanorStage(stage, rune.orElse(stage.getDefaultRune()), ticks, emc, needs)));

	public static final StreamCodec<RegistryFriendlyByteBuf, AthanorStage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.idMapper(i -> OpusStage.values()[i], OpusStage::ordinal), AthanorStage::stage,
			ByteBufCodecs.idMapper(RuneColor::byGlow, RuneColor::ordinal), AthanorStage::rune,
			ByteBufCodecs.VAR_INT, AthanorStage::ticks,
			ByteBufCodecs.VAR_LONG, AthanorStage::emc,
			ByteBufCodecs.optional(ByteBufCodecs.idMapper(i -> Requirement.values()[i], Requirement::ordinal)), AthanorStage::needs,
			AthanorStage::new
	);

	public boolean isMet(ServerLevel level, BlockPos crucible) {
		return needs.isEmpty() || needs.get().isMet(level, crucible);
	}

	/**
	 * Something about the world a stage needs to continue. While it is missing the ritual waits.
	 */
	public enum Requirement implements StringRepresentable {
		SKY,
		DAY,
		NIGHT,
		THUNDER;

		public static final Codec<Requirement> CODEC = StringRepresentable.fromEnum(Requirement::values);

		public boolean isMet(ServerLevel level, BlockPos crucible) {
			return switch (this) {
				case SKY -> level.canSeeSky(crucible.above());
				case DAY -> level.isBrightOutside();
				case NIGHT -> level.isDarkOutside();
				case THUNDER -> level.isThundering() && level.canSeeSky(crucible.above());
			};
		}

		@NotNull
		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
