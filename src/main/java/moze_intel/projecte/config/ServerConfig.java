package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedBooleanValue;
import moze_intel.projecte.config.value.CachedDoubleValue;
import moze_intel.projecte.config.value.CachedFloatValue;
import moze_intel.projecte.config.value.CachedIntValue;
import net.minecraft.SharedConstants;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * For config options that the server has absolute say over
 */
public final class ServerConfig extends BasePEConfig {

	private final ModConfigSpec configSpec;

	public final Difficulty difficulty;
	public final Items items;
	public final Effects effects;
	public final Misc misc;
	public final Cooldown cooldown;
	public final Expansion expansion;
	public final Ritual ritual;
	public final Comprehension comprehension;

	ServerConfig() {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		cooldown = new Cooldown(this, builder);
		expansion = new Expansion(this, builder);
		difficulty = new Difficulty(this, builder);
		effects = new Effects(this, builder);
		items = new Items(this, builder);
		misc = new Misc(this, builder);
		ritual = new Ritual(this, builder);
		comprehension = new Comprehension(this, builder);
		configSpec = builder.build();
	}

	@Override
	public String getFileName() {
		return "server";
	}

	@Override
	public String getTranslation() {
		return "Server Config";
	}

	@Override
	public ModConfigSpec getConfigSpec() {
		return configSpec;
	}

	@Override
	public ModConfig.Type getConfigType() {
		return ModConfig.Type.SERVER;
	}

	public static class Cooldown {

		public final Pedestal pedestal;
		public final Player player;

		private Cooldown(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_COOLDOWN.applyToBuilder(builder).push("cooldown");
			pedestal = new Pedestal(config, builder);
			player = new Player(config, builder);
			builder.pop();
		}

		public static class Pedestal {

			public final CachedIntValue archangel;
			public final CachedIntValue body;
			public final CachedIntValue evertide;
			public final CachedIntValue harvest;
			public final CachedIntValue ignition;
			public final CachedIntValue life;
			public final CachedIntValue repair;
			public final CachedIntValue swrg;
			public final CachedIntValue soul;
			public final CachedIntValue volcanite;
			public final CachedIntValue zero;

			private Pedestal(IPEConfig config, ModConfigSpec.Builder builder) {
				PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL.applyToBuilder(builder).push("pedestal");
				archangel = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_ARCHANGEL.applyToBuilder(builder)
						.defineInRange("archangel", 2 * SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				body = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_BODY_STONE.applyToBuilder(builder)
						.defineInRange("body", SharedConstants.TICKS_PER_SECOND / 2, -1, Integer.MAX_VALUE));
				evertide = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_EVERTIDE.applyToBuilder(builder)
						.defineInRange("evertide", SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				harvest = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_HARVEST.applyToBuilder(builder)
						.defineInRange("harvest", SharedConstants.TICKS_PER_SECOND / 2, -1, Integer.MAX_VALUE));
				ignition = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_IGNITION.applyToBuilder(builder)
						.defineInRange("ignition", 2 * SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				life = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_LIFE_STONE.applyToBuilder(builder)
						.defineInRange("life", SharedConstants.TICKS_PER_SECOND / 4, -1, Integer.MAX_VALUE));
				repair = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_REPAIR.applyToBuilder(builder)
						.defineInRange("repair", SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				swrg = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_SWRG.applyToBuilder(builder)
						.defineInRange("swrg", (int) (3.5 * SharedConstants.TICKS_PER_SECOND), -1, Integer.MAX_VALUE));
				soul = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_SOUL_STONE.applyToBuilder(builder)
						.defineInRange("soul", SharedConstants.TICKS_PER_SECOND / 2, -1, Integer.MAX_VALUE));
				volcanite = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_VOLCANITE.applyToBuilder(builder)
						.defineInRange("volcanite", SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				zero = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PEDESTAL_ZERO.applyToBuilder(builder)
						.defineInRange("zero", 2 * SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				builder.pop();
			}
		}

		public static class Player {

			public final CachedIntValue projectile;
			public final CachedIntValue gemChest;
			public final CachedIntValue repair;
			public final CachedIntValue heal;
			public final CachedIntValue feed;

			private Player(IPEConfig config, ModConfigSpec.Builder builder) {
				PEConfigTranslations.SERVER_COOLDOWN_PLAYER.applyToBuilder(builder).push("player");
				projectile = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PLAYER_PROJECTILE.applyToBuilder(builder)
						.defineInRange("projectile", 0, -1, Integer.MAX_VALUE));
				gemChest = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PLAYER_GEM_CHESTPLATE.applyToBuilder(builder)
						.defineInRange("gemChest", 0, -1, Integer.MAX_VALUE));
				repair = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PLAYER_REPAIR.applyToBuilder(builder)
						.defineInRange("repair", SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				heal = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PLAYER_HEAL.applyToBuilder(builder)
						.defineInRange("heal", SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				feed = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COOLDOWN_PLAYER_FEED.applyToBuilder(builder)
						.defineInRange("feed", SharedConstants.TICKS_PER_SECOND, -1, Integer.MAX_VALUE));
				builder.pop();
			}
		}
	}

	public static class Difficulty {

		public final CachedBooleanValue offensiveAbilities;
		public final CachedFloatValue katarDeathAura;
		public final CachedDoubleValue covalenceLoss;
		public final CachedBooleanValue covalenceLossRounding;

		private Difficulty(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_DIFFICULTY.applyToBuilder(builder).push("difficulty");
			offensiveAbilities = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_DIFFICULTY_OFFENSIVE_ABILITIES.applyToBuilder(builder)
					.define("offensiveAbilities", false));
			katarDeathAura = CachedFloatValue.wrap(config, PEConfigTranslations.SERVER_DIFFICULTY_KATAR_DEATH_AURA.applyToBuilder(builder)
					.defineInRange("katarDeathAura", 1_000F, 0, Integer.MAX_VALUE));
			covalenceLoss = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_DIFFICULTY_COVALENCE_LOSS.applyToBuilder(builder)
					.defineInRange("covalenceLoss", 1.0, 0.1, 1.0));
			covalenceLossRounding = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_DIFFICULTY_COVALENCE_LOSS_ROUNDING.applyToBuilder(builder)
					.define("covalenceLossRounding", true));
			builder.pop();
		}
	}

	public static class Effects {

		public final CachedIntValue timePedBonus;
		public final CachedDoubleValue timePedMobSlowness;
		public final CachedBooleanValue interdictionMode;

		private Effects(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_EFFECTS.applyToBuilder(builder).push("effects");
			timePedBonus = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_EFFECTS_TIME_PEDESTAL_BONUS.applyToBuilder(builder)
					.defineInRange("timePedBonus", 18, 0, 256));
			timePedMobSlowness = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EFFECTS_TIME_PEDESTAL_MOB_SLOWNESS.applyToBuilder(builder)
					.defineInRange("timePedMobSlowness", 0.10, 0, 1));
			interdictionMode = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EFFECTS_INTERDICTION_MODE.applyToBuilder(builder)
					.define("interdictionMode", true));
			builder.pop();
		}
	}

	public static class Items {

		public final CachedBooleanValue pickaxeAoeVeinMining;
		public final CachedBooleanValue harvBandIndirect;
		public final CachedBooleanValue disableAllRadiusMining;
		public final CachedBooleanValue enableTimeWatch;
		public final CachedBooleanValue opEvertide;

		private Items(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_ITEMS.applyToBuilder(builder).push("items");
			pickaxeAoeVeinMining = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_ITEMS_PICKAXE_AOE_VEIN_MINING.applyToBuilder(builder)
					.define("pickaxeAoeVeinMining", false));
			harvBandIndirect = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_ITEMS_HARVEST_BAND_INDIRECT.applyToBuilder(builder)
					.define("harvBandIndirect", false));
			disableAllRadiusMining = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_ITEMS_DISABLE_ALL_RADIUS_MINING.applyToBuilder(builder)
					.define("disableAllRadiusMining", false));
			enableTimeWatch = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_ITEMS_TIME_WATCH.applyToBuilder(builder)
					.define("enableTimeWatch", true));
			opEvertide = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_ITEMS_OP_EVERTIDE.applyToBuilder(builder)
					.define("opEvertide", false));
			builder.pop();
		}
	}

	public static class Misc {

		public final CachedBooleanValue unsafeKeyBinds;
		public final CachedBooleanValue lookingAtDisplay;
		public final CachedBooleanValue showMissingGameStages;

		private Misc(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_MISC.applyToBuilder(builder).push("misc");
			unsafeKeyBinds = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_MISC_UNSAFE_KEY_BINDS.applyToBuilder(builder)
					.define("unsafeKeyBinds", false));
			lookingAtDisplay = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_MISC_LOOKING_AT_DISPLAY.applyToBuilder(builder)
					.define("lookingAtDisplay", true));
			showMissingGameStages = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_MISC_SHOW_MISSING_STAGES.applyToBuilder(builder)
					.define("showMissingGameStages", true));
			builder.pop();
		}
	}
	/**
	 * Settings of the rituals in the Athanor.
	 */
	public static class Comprehension {

		public final CachedBooleanValue enabled;
		public final CachedIntValue instantBelow;
		public final CachedIntValue burnsNeeded;

		private Comprehension(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_COMPREHENSION.applyToBuilder(builder).push("comprehension");
			enabled = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_COMPREHENSION_ENABLED.applyToBuilder(builder)
					.define("enabled", true));
			instantBelow = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COMPREHENSION_INSTANT_BELOW.applyToBuilder(builder)
					.defineInRange("instantBelow", 2_048, 0, Integer.MAX_VALUE));
			burnsNeeded = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_COMPREHENSION_BURNS.applyToBuilder(builder)
					.defineInRange("burnsNeeded", 4, 1, 64));
			builder.pop();
		}
	}

	public static class Ritual {

		public final CachedDoubleValue emcMultiplier;
		public final CachedDoubleValue durationMultiplier;
		public final CachedBooleanValue rebound;
		public final CachedBooleanValue reboundBreaksBlocks;

		private Ritual(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_RITUAL.applyToBuilder(builder).push("ritual");
			emcMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_RITUAL_EMC_MULTIPLIER.applyToBuilder(builder)
					.defineInRange("emcMultiplier", 1.0, 0, 1_000));
			durationMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_RITUAL_DURATION_MULTIPLIER.applyToBuilder(builder)
					.defineInRange("durationMultiplier", 1.0, 0.01, 100));
			rebound = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_RITUAL_REBOUND.applyToBuilder(builder)
					.define("rebound", true));
			reboundBreaksBlocks = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_RITUAL_REBOUND_BREAKS_BLOCKS.applyToBuilder(builder)
					.define("reboundBreaksBlocks", false));
			builder.pop();
		}
	}

	/**
	 * Settings of the features that came from the ProjectExpansion addon.
	 */
	public static class Expansion {

		public final CachedIntValue tickDelay;
		public final CachedBooleanValue notifyCommandChanges;
		public final CachedBooleanValue notifyKnowledgeBookGains;
		public final CachedBooleanValue limitEmcLinkVendor;
		public final CachedBooleanValue enableFluidEfficiency;
		public final CachedIntValue transmutationInterfaceItemCount;
		public final CachedDoubleValue collectorMultiplier;
		public final CachedDoubleValue emcLinkItemLimitMultiplier;
		public final CachedDoubleValue emcLinkFluidLimitMultiplier;
		public final CachedDoubleValue emcLinkEMCLimitMultiplier;
		public final CachedDoubleValue powerflowerMultiplier;
		public final CachedDoubleValue relayBonusMultiplier;
		public final CachedDoubleValue relayTransferMultiplier;
		public final CachedIntValue infiniteFuelCost;
		public final CachedIntValue infiniteFuelBurnTime;
		public final CachedIntValue infiniteSteakCost;
		public final CachedBooleanValue persistEnchantedBooksOnly;
		public final ModConfigSpec.EnumValue<moze_intel.projecte.expansion.config.Config.AlchemicalBookEditLevel> editOthersAlchemicalBooks;
		public final CachedBooleanValue zeroEmcFluidsAreFree;
		public final CachedBooleanValue enableCollectorOptimizations;
		public final CachedIntValue compactSunBonus;
		public final CachedBooleanValue sunMultiplierPriceCompensation;
		public final CachedBooleanValue enableReloadEMCCommand;

		private Expansion(IPEConfig config, ModConfigSpec.Builder builder) {
			PEConfigTranslations.SERVER_EXPANSION.applyToBuilder(builder).push("expansion");
			tickDelay = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_TICK_DELAY.applyToBuilder(builder).defineInRange("tickDelay", 20, 1, 200));
			notifyCommandChanges = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_NOTIFY_COMMAND_CHANGES.applyToBuilder(builder).define("notifyCommandChanges", true));
			notifyKnowledgeBookGains = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_NOTIFY_KNOWLEDGE_BOOK.applyToBuilder(builder).define("notifyKnowledgeBookGains", true));
			limitEmcLinkVendor = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_LIMIT_EMC_LINK_VENDOR.applyToBuilder(builder).define("limitEmcLinkVendor", true));
			enableFluidEfficiency = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_FLUID_EFFICIENCY.applyToBuilder(builder).define("enableFluidEfficiency", true));
			transmutationInterfaceItemCount = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_INTERFACE_ITEM_COUNT.applyToBuilder(builder).defineInRange("transmutationInterfaceItemCount", Integer.MAX_VALUE, 1, Integer.MAX_VALUE));
			collectorMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_COLLECTOR_MULTIPLIER.applyToBuilder(builder).defineInRange("collectorMultiplier", 1.0D, 0.1D, 50D));
			emcLinkItemLimitMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_EMC_LINK_ITEM_MULTIPLIER.applyToBuilder(builder).defineInRange("emcLinkItemLimitMultiplier", 1.0D, 0.1D, 50D));
			emcLinkFluidLimitMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_EMC_LINK_FLUID_MULTIPLIER.applyToBuilder(builder).defineInRange("emcLinkFluidLimitMultiplier", 1.0D, 0.1D, 50D));
			emcLinkEMCLimitMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_EMC_LINK_EMC_MULTIPLIER.applyToBuilder(builder).defineInRange("emcLinkEMCLimitMultiplier", 1.0D, 0.1D, 50D));
			powerflowerMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_POWER_FLOWER_MULTIPLIER.applyToBuilder(builder).defineInRange("powerflowerMultiplier", 1.0D, 0.1D, 50D));
			relayBonusMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_RELAY_BONUS_MULTIPLIER.applyToBuilder(builder).defineInRange("relayBonusMultiplier", 1.0D, 0.1D, 50D));
			relayTransferMultiplier = CachedDoubleValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_RELAY_TRANSFER_MULTIPLIER.applyToBuilder(builder).defineInRange("relayTransferMultiplier", 1.0D, 0.1D, 50D));
			infiniteFuelCost = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_INFINITE_FUEL_COST.applyToBuilder(builder).defineInRange("infiniteFuelCost", 128, 1, Integer.MAX_VALUE));
			infiniteFuelBurnTime = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_INFINITE_FUEL_BURN_TIME.applyToBuilder(builder).defineInRange("infiniteFuelBurnTime", 1600, 1, Integer.MAX_VALUE));
			infiniteSteakCost = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_INFINITE_STEAK_COST.applyToBuilder(builder).defineInRange("infiniteSteakCost", 64, 1, Integer.MAX_VALUE));
			persistEnchantedBooksOnly = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_PERSIST_ENCHANTED_BOOKS.applyToBuilder(builder).define("persistEnchantedBooksOnly", false));
			editOthersAlchemicalBooks = PEConfigTranslations.SERVER_EXPANSION_EDIT_OTHERS_BOOKS.applyToBuilder(builder)
					.defineEnum("editOthersAlchemicalBooks", moze_intel.projecte.expansion.config.Config.AlchemicalBookEditLevel.DISABLED);
			zeroEmcFluidsAreFree = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_ZERO_EMC_FLUIDS.applyToBuilder(builder).define("zeroEmcFluidsAreFree", true));
			enableCollectorOptimizations = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_COLLECTOR_OPTIMIZATIONS.applyToBuilder(builder).define("enableCollectorOptimizations", false));
			compactSunBonus = CachedIntValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_COMPACT_SUN_BONUS.applyToBuilder(builder).defineInRange("compactSunBonus", 10, 0, Integer.MAX_VALUE));
			sunMultiplierPriceCompensation = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_SUN_PRICE_COMPENSATION.applyToBuilder(builder).define("sunMultiplierPriceCompensation", true));
			enableReloadEMCCommand = CachedBooleanValue.wrap(config, PEConfigTranslations.SERVER_EXPANSION_RELOAD_EMC_COMMAND.applyToBuilder(builder).define("enableReloadEMCCommand", true));
			builder.pop();
		}
	}
}