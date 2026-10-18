package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedBooleanValue;
import moze_intel.projecte.config.value.CachedDoubleValue;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * For config options that only the client cares about
 */
public class ClientConfig extends BasePEConfig {

	private final ModConfigSpec configSpec;

	public final CachedBooleanValue emcToolTips;
	public final CachedBooleanValue shiftEmcToolTips;
	public final CachedBooleanValue shiftLearnedToolTips;
	public final CachedBooleanValue pedestalToolTips;
	public final CachedBooleanValue statToolTips;
	public final CachedBooleanValue tagToolTips;

	public final CachedBooleanValue pulsatingOverlay;
	public final CachedDoubleValue ritualParticles;

	public final Expansion expansion;

	ClientConfig() {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		pulsatingOverlay = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_PHILO_OVERLAY.applyToBuilder(builder).define("pulsatingOverlay", false));
		ritualParticles = CachedDoubleValue.wrap(this, PEConfigTranslations.CLIENT_RITUAL_PARTICLES.applyToBuilder(builder)
				.defineInRange("ritualParticles", 1.0, 0.0, 4.0));

		PEConfigTranslations.CLIENT_TOOLTIPS.applyToBuilder(builder).push("tooltips");
		emcToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_EMC.applyToBuilder(builder).define("emc", true));
		shiftEmcToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_EMC_SHIFT.applyToBuilder(builder)
				.define("shift_emc", false));
		shiftLearnedToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_LEARNED_SHIFT.applyToBuilder(builder)
				.define("shift_learned", true));
		pedestalToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_PEDESTAL.applyToBuilder(builder)
				.define("pedestal", true));
		statToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_STATS.applyToBuilder(builder).define("statToolTips", true));
		tagToolTips = CachedBooleanValue.wrap(this, PEConfigTranslations.CLIENT_TOOLTIPS_TAGS.applyToBuilder(builder).define("tag", false));
		builder.pop();

		expansion = new Expansion(this, builder);

		configSpec = builder.build();
	}
	/**
	 * Settings of the features that came from the ProjectExpansion addon.
	 */
	public static class Expansion {

		public final CachedBooleanValue formatEMC;
		public final CachedBooleanValue fullNumberNames;
		public final CachedBooleanValue emcDisplay;
		public final ModConfigSpec.EnumValue<moze_intel.projecte.expansion.gui.EMCDisplay.EmcDisplayPosition> emcDisplayPosition;
		public final CachedBooleanValue enableLearnedTooltip;
		public final CachedBooleanValue alchemicalCollectionSound;
		public final ModConfigSpec.EnumValue<moze_intel.projecte.expansion.util.SearchType> searchType;

		private Expansion(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.CLIENT_EXPANSION.applyToBuilder(builder).push("expansion");
			formatEMC = CachedBooleanValue.wrap(config, PEConfigTranslations.CLIENT_EXPANSION_FORMAT_EMC.applyToBuilder(builder).define("formatEMC", true));
			fullNumberNames = CachedBooleanValue.wrap(config, PEConfigTranslations.CLIENT_EXPANSION_FULL_NUMBER_NAMES.applyToBuilder(builder).define("fullNumberNames", true));
			emcDisplay = CachedBooleanValue.wrap(config, PEConfigTranslations.CLIENT_EXPANSION_EMC_DISPLAY.applyToBuilder(builder).define("emcDisplay", true));
			emcDisplayPosition = PEConfigTranslations.CLIENT_EXPANSION_EMC_DISPLAY_POSITION.applyToBuilder(builder)
					.defineEnum("emcDisplayPosition", moze_intel.projecte.expansion.gui.EMCDisplay.EmcDisplayPosition.TOP_LEFT);
			enableLearnedTooltip = CachedBooleanValue.wrap(config, PEConfigTranslations.CLIENT_EXPANSION_LEARNED_TOOLTIP.applyToBuilder(builder).define("enableLearnedTooltip", true));
			alchemicalCollectionSound = CachedBooleanValue.wrap(config, PEConfigTranslations.CLIENT_EXPANSION_COLLECTION_SOUND.applyToBuilder(builder).define("alchemicalCollectionSound", true));
			searchType = PEConfigTranslations.CLIENT_EXPANSION_SEARCH_TYPE.applyToBuilder(builder)
					.defineEnum("searchType", moze_intel.projecte.expansion.util.SearchType.NORMAL);
			builder.pop();
		}
	}

	@Override
	public String getFileName() {
		return "client";
	}

	@Override
	public String getTranslation() {
		return "Client Config";
	}

	@Override
	public ModConfigSpec getConfigSpec() {
		return configSpec;
	}

	@Override
	public ModConfig.Type getConfigType() {
		return ModConfig.Type.CLIENT;
	}

}