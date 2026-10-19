package moze_intel.projecte.config;

import moze_intel.projecte.PECore;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Re-evaluate all these translations
public enum PEConfigTranslations implements IConfigTranslation {
	//Client Config
	CLIENT_PHILO_OVERLAY("client.philo_overlay", "Pulsating Overlay", "The Philosopher's Stone overlay softly pulsates."),

	CLIENT_RITUAL_PARTICLES("client.ritual_particles", "Ritual Particles",
			"Multiplier for the amount of particles of the Alchemical Crucible. 0 turns them off."),
	CLIENT_TOOLTIPS("client.tooltips", "Tooltip Settings", "Settings for configuring Tooltips provided by ProjectE.", true),

	CLIENT_TOOLTIPS_EMC("client.tooltips.emc", "EMC Tooltips", "Show the EMC value as a tooltip on items and blocks."),
	CLIENT_TOOLTIPS_EMC_SHIFT("client.tooltips.emc.shift", "Shift EMC Tooltips",
			"Requires holding shift to display the EMC value as a tooltip on items and blocks. Note: this does nothing if EMC Tooltips are disabled."),
	CLIENT_TOOLTIPS_LEARNED_SHIFT("client.tooltips.learned.shift", "Shift Learned Tooltips",
			"Requires holding shift to display the learned/unlearned text as a tooltip on items and blocks. Note: this does nothing if EMC Tooltips are disabled."),
	CLIENT_TOOLTIPS_PEDESTAL("client.tooltips.pedestal", "DM Pedestal Tooltips", "Show Dark Matter Pedestal functions in item tooltips."),
	CLIENT_TOOLTIPS_STATS("client.tooltips.stats", "Stat Tooltips", "Show stats as tooltips for various ProjectE blocks."),
	CLIENT_TOOLTIPS_TAGS("client.tooltips.tags", "Tag Tooltips", "Show item tags in tooltips (useful for custom EMC registration)."),

	//Expansion (formerly the ProjectExpansion addon) - Client Config
	CLIENT_EXPANSION("client.expansion", "Expansion Settings",
			"Settings for the features that came from the ProjectExpansion addon.", true),

	CLIENT_EXPANSION_FORMAT_EMC("client.expansion.format_emc", "Shorten EMC Values",
			"If EMC should be formatted as M/B/T/etc."),
	CLIENT_EXPANSION_FULL_NUMBER_NAMES("client.expansion.full_number_names", "Full Number Names",
			"If full number names (Million/Billion/Trillion) should be used instead of abbreviations."),
	CLIENT_EXPANSION_EMC_DISPLAY("client.expansion.emc_display", "EMC Display",
			"Displays your current emc and gained emc per second in the top left corner."),
	CLIENT_EXPANSION_EMC_DISPLAY_POSITION("client.expansion.emc_display_position", "EMC Display Position",
			"The position of the emc display."),
	CLIENT_EXPANSION_LEARNED_TOOLTIP("client.expansion.learned_tooltip", "Learned Tooltip",
			"If a tooltip should be shown on items which can be learned, denoting if the item has been learned or not. Note: ProjectE's client.shift_emc applies to this."),
	CLIENT_EXPANSION_COLLECTION_SOUND("client.expansion.collection_sound", "Alchemical Collection Sound",
			"If a sound should be played when something is collected with Alchemical Collection."),
	CLIENT_EXPANSION_SEARCH_TYPE("client.expansion.search_type", "Search Type",
			"How the transmutation search should behave."),

	//Expansion (formerly the ProjectExpansion addon) - Server Config
	SERVER_EXPANSION("server.expansion", "Expansion Settings",
			"Settings for the features that came from the ProjectExpansion addon.", true),

	SERVER_EXPANSION_TICK_DELAY("server.expansion.tick_delay", "Tick Delay",
			"The delay between mod operations (in ticks, default 20) - this will slightly effect the amount of emc generated via rounding - increase if you're noticing lag."),
	SERVER_EXPANSION_NOTIFY_COMMAND_CHANGES("server.expansion.notify_command_changes", "Notify Command Changes",
			"Notify users when something is changed about them via commands."),
	SERVER_EXPANSION_NOTIFY_KNOWLEDGE_BOOK("server.expansion.notify_knowledge_book", "Notify Knowledge Book Results",
			"Tell users the list of items they gained when using a knowledge book."),
	SERVER_EXPANSION_LIMIT_EMC_LINK_VENDOR("server.expansion.limit_emc_link_vendor", "Limit EMC Link Vendor",
			"If EMC Link Right-Click functionality should be Limited by Tier or Not."),
	SERVER_EXPANSION_FLUID_EFFICIENCY("server.expansion.fluid_efficiency", "Fluid Efficiency",
			"If fluid efficiency should be enabled."),
	SERVER_EXPANSION_INTERFACE_ITEM_COUNT("server.expansion.interface_item_count", "Transmutation Interface Item Count",
			"The amount of items that the transmutation interface will report to have. Depending on your usage, you may want this to be a high value."),
	SERVER_EXPANSION_COLLECTOR_MULTIPLIER("server.expansion.collector_multiplier", "Collector Multiplier",
			"Multiplies the output of Collectors."),
	SERVER_EXPANSION_EMC_LINK_ITEM_MULTIPLIER("server.expansion.emc_link_item_multiplier", "EMC Link Item Limit Multiplier",
			"Multiplies the item limit of EMC Links."),
	SERVER_EXPANSION_EMC_LINK_FLUID_MULTIPLIER("server.expansion.emc_link_fluid_multiplier", "EMC Link Fluid Limit Multiplier",
			"Multiplies the fluid limit of EMC Links."),
	SERVER_EXPANSION_EMC_LINK_EMC_MULTIPLIER("server.expansion.emc_link_emc_multiplier", "EMC Link EMC Limit Multiplier",
			"Multiplies the emc limit of EMC Links."),
	SERVER_EXPANSION_POWER_FLOWER_MULTIPLIER("server.expansion.power_flower_multiplier", "Power Flower Multiplier",
			"Multiplies the output of Power Flowers."),
	SERVER_EXPANSION_RELAY_BONUS_MULTIPLIER("server.expansion.relay_bonus_multiplier", "Relay Bonus Multiplier",
			"Multiplies the bonus of Relays."),
	SERVER_EXPANSION_RELAY_TRANSFER_MULTIPLIER("server.expansion.relay_transfer_multiplier", "Relay Transfer Multiplier",
			"Multiplies the transfer limit of Relays."),
	SERVER_EXPANSION_INFINITE_FUEL_COST("server.expansion.infinite_fuel_cost", "Infinite Fuel Cost",
			"The cost of using the infinite fuel item."),
	SERVER_EXPANSION_INFINITE_FUEL_BURN_TIME("server.expansion.infinite_fuel_burn_time", "Infinite Fuel Burn Time",
			"The ticks each usage of the infinite fuel item will give."),
	SERVER_EXPANSION_INFINITE_STEAK_COST("server.expansion.infinite_steak_cost", "Infinite Steak Cost",
			"The cost of using the infinite steak item."),
	SERVER_EXPANSION_PERSIST_ENCHANTED_BOOKS("server.expansion.persist_enchanted_books", "Persist Enchanted Books Only",
			"If ProjectE's processors.EnchantmentProcessor.persistent option should only include enchanted books."),
	SERVER_EXPANSION_EDIT_OTHERS_BOOKS("server.expansion.edit_others_books", "Edit Others Alchemical Books",
			"If players should be allowed to edit books bound to other players. A player is considered to be \"OP\" when they have an op level of 2 or greater."),
	SERVER_EXPANSION_ZERO_EMC_FLUIDS("server.expansion.zero_emc_fluids_are_free", "Zero EMC Fluids Are Free",
			"If fluids which end their calculations at zero emc should be returned as free."),
	SERVER_EXPANSION_COLLECTOR_OPTIMIZATIONS("server.expansion.collector_optimizations", "Collector Optimizations",
			"If optimizations (ticking only once per second) should be enabled for collectors. This will make them process at most one item each second."),
	SERVER_EXPANSION_COMPACT_SUN_BONUS("server.expansion.compact_sun_bonus", "Compact Sun Bonus",
			"The bonus (multiplicative) the compact sun block should give. Set to 0 to disable."),
	SERVER_EXPANSION_SUN_PRICE_COMPENSATION("server.expansion.sun_price_compensation", "Sun Multiplier Price Compensation",
			"Enable determining the sun bonus multiplier via the difference in emc price between the final power flower and the compact sun block, rounded up to the next 10. If either block has no emc value or the multiplier is lower than compact_sun_bonus, that value will be used instead."),
	SERVER_EXPANSION_RELOAD_EMC_COMMAND("server.expansion.reload_emc_command", "Enable Reload EMC Command",
			"Enable the /px reloademc command."),

	//Common Config
	COMMON_DEBUG_LOGGING("common.debug_logging", "Debug Logging", "Enable more verbose debug logging."),

	COMMON_CRAFTING("common.crafting", "Crafting Settings", "Settings for configuring crafting requirements of specific ProjectE recipes.", true),
	COMMON_CRAFTING_TOME("common.crafting.tome", "Craftable Tome", "Enable crafting the Tome of Knowledge."),
	COMMON_CRAFTING_FULL_KLEIN("common.crafting.full_klein", "Require Full Klein Stars",
			"Require full omega klein stars in the tome of knowledge and gem armor recipes. This is the same behavior that EE2 had."),

	//Server Config
	SERVER_COOLDOWN("server.cooldown", "Cooldown Settings",
			"Settings for configuring the Cooldown (in ticks) for various features in ProjectE. "
			+ "A cooldown of -1 will disable the functionality. A cooldown of 0 will allow the actions to happen every tick. "
			+ "Use caution as a very low value on features that run automatically could cause TPS issues.", true),

	SERVER_COOLDOWN_PEDESTAL("server.cooldown.pedestal", "Pedestal Cooldown Settings",
			"Cooldown settings for various items within Dark Matter Pedestals.", "Edit Pedestal Cooldowns"),
	SERVER_COOLDOWN_PEDESTAL_ARCHANGEL("server.cooldown.pedestal.archangel", "Archangel",
			"Delay between Archangel Smite shooting arrows while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_BODY_STONE("server.cooldown.pedestal.body_stone", "Body Stone",
			"Delay between Body Stone healing 0.5 shanks while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_EVERTIDE("server.cooldown.pedestal.evertide", "Evertide Amulet",
			"Delay between Evertide Amulet trying to start rain while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_HARVEST("server.cooldown.pedestal.harvest", "Harvest",
			"Delay between Harvest Goddess trying to grow and harvest while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_IGNITION("server.cooldown.pedestal.ignition", "Ignition",
			"Delay between Ignition Ring trying to light entities on fire while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_LIFE_STONE("server.cooldown.pedestal.life_stone", "Life Stone",
			"Delay between Life Stone healing both food and hunger by 0.5 shank/heart while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_REPAIR("server.cooldown.pedestal.repair", "Repair",
			"Delay between Talisman of Repair trying to repair player items while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_SWRG("server.cooldown.pedestal.swrg", "SWRG",
			"Delay between SWRG trying to smite mobs while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_SOUL_STONE("server.cooldown.pedestal.soul_stone", "Soul Stone",
			"Delay between Soul Stone healing 0.5 hearts while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_VOLCANITE("server.cooldown.pedestal.volcanite", "Volcanite Amulet",
			"Delay between Volcanite Amulet trying to stop rain while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_ZERO("server.cooldown.pedestal.zero", "SWRG",
			"Delay between Zero Ring trying to extinguish entities and freezing ground while in the pedestal."),

	SERVER_COOLDOWN_PLAYER("server.cooldown.player", "Player Cooldown Settings", "Cooldown settings for various items when being used by a player.",
			"Edit Player Cooldowns"),
	SERVER_COOLDOWN_PLAYER_PROJECTILE("server.cooldown.player.projectile", "Projectile", "A cooldown for firing projectiles."),
	SERVER_COOLDOWN_PLAYER_GEM_CHESTPLATE("server.cooldown.player.gem_chestplate", "Gem Chestplate", "A cooldown for Gem Chestplate explosion."),
	SERVER_COOLDOWN_PLAYER_REPAIR("server.cooldown.player.repair", "Repair",
			"Delay between Talisman of Repair trying to repair player items while in a player's inventory."),
	SERVER_COOLDOWN_PLAYER_HEAL("server.cooldown.player.heal", "Heal",
			"Delay between heal attempts while in a player's inventory. (Soul Stone, Life Stone, Gem Helmet)."),
	SERVER_COOLDOWN_PLAYER_FEED("server.cooldown.player.feed", "Feed",
			"Delay between feed attempts while in a player's inventory. (Body Stone, Life Stone, Gem Helmet)."),


	SERVER_DIFFICULTY("server.difficulty", "Difficulty Settings", "Settings for configuring Difficulty options provided by ProjectE.", true),
	SERVER_DIFFICULTY_OFFENSIVE_ABILITIES("server.difficulty.offensive_abilities", "Offensive Abilities",
			"Set to false to disable Gem Armor offensive abilities (helmet zap and chestplate explosion)."),
	SERVER_DIFFICULTY_KATAR_DEATH_AURA("server.difficulty.katar_death_aura", "Katar Death Aura", "Amount of damage the Katar's Extra Function deals."),

	SERVER_DIFFICULTY_COVALENCE_LOSS("server.difficulty.covalence_loss", "Covalence Loss",
			"Adjusting this ratio changes how much EMC is received when burning a item. For example setting this to 0.5 will return half of the EMC cost."),
	SERVER_DIFFICULTY_COVALENCE_LOSS_ROUNDING("server.difficulty.covalence_loss.rounding", "Covalence Loss Rounding",
			"How rounding occurs when Covalence Loss results in a burn value less than 1 EMC. If true the value will be rounded up to 1. "
			+ "If false the value will be rounded down to 0."),

	SERVER_EFFECTS("server.effects", "Effect Settings", "Settings for configuring Effect options provided by ProjectE.", true),
	SERVER_EFFECTS_TIME_PEDESTAL_BONUS("server.effects.time_pedestal.bonus", "Time Pedestal Bonus",
			"Bonus ticks given by the Watch of Flowing Time while in the pedestal. 0 = effectively no bonus."),
	SERVER_EFFECTS_TIME_PEDESTAL_MOB_SLOWNESS("server.effects.time_pedestal.mob_slowness", "Time Pedestal Mob Slowness",
			"Factor the Watch of Flowing Time slows down mobs by while in the pedestal. Set to 1.0 for no slowdown."),
	SERVER_EFFECTS_INTERDICTION_MODE("server.effects.interdiction_mode", "Interdiction Mode",
			"If true the Interdiction Torch only affects hostile mobs and projectiles. If false it affects all non blacklisted living entities."),

	SERVER_ITEMS("server.items", "Item Settings", "Settings for configuring Item options provided by ProjectE.", true),
	SERVER_ITEMS_PICKAXE_AOE_VEIN_MINING("server.items.pickaxe_aoe_vein_mining", "Pickaxe AOE Vein Mining",
			"Instead of vein mining the ore you right click with your Dark/Red Matter Pick/Star it vein mines all ores in an AOE around you "
			+ "like it did in ProjectE before version 1.4.4."),
	SERVER_ITEMS_HARVEST_BAND_INDIRECT("server.items.harvest_band_indirect", "Harvest Band Indirect",
			"Allows the Harvest Goddess Band to passively grow things like tall grass, flowers, etc, on top of grass blocks and nylium. Also allows it to make moss spread."),
	SERVER_ITEMS_DISABLE_ALL_RADIUS_MINING("server.items.disable_all_radius_mining", "Disable All Radius Mining",
			"If set to true, disables all radius-based mining functionality (right click of tools)."),
	SERVER_ITEMS_TIME_WATCH("server.items.time_watch", "Watch of Flowing Time", "Enables the Watch of Flowing Time."),
	SERVER_ITEMS_OP_EVERTIDE("server.items.op_evertide", "Overpowered Evertide Amulet",
			"Allow the Evertide amulet to place water in dimensions that water evaporates. For example: The Nether."),

	SERVER_MISC("server.misc", "Misc Settings", "Settings for configuring misc options provided by ProjectE.", true),
	SERVER_MISC_UNSAFE_KEY_BINDS("server.misc.unsafe_key_binds", "Unsafe Key Binds",
			"False requires your hand be empty for Gem Armor Offensive Abilities to be readied or triggered."),
	SERVER_MISC_LOOKING_AT_DISPLAY("server.misc.looking_at_display", "Looking At Display",
			"Shows the EMC value of blocks when looking at them in Jade, TOP, or WTHIT."),
	SERVER_MISC_SHOW_MISSING_STAGES("server.misc.show_missing_game_stages", "Show Missing Game Stages",
			"If enabled, warnings will tell you which Game Stages you are missing to use blacklisted items."),

	SERVER_RITUAL("server.ritual", "Athanor", "Settings of the rituals in the Athanor. The rituals themselves are recipes of the type projecte:athanor.", true),
	SERVER_RITUAL_EMC_MULTIPLIER("server.ritual.emc_multiplier", "EMC Multiplier", "Multiplies the personal EMC every stage of every ritual costs."),
	SERVER_RITUAL_DURATION_MULTIPLIER("server.ritual.duration_multiplier", "Duration Multiplier", "Multiplies how long every stage of every ritual takes."),
	SERVER_RITUAL_REBOUND("server.ritual.rebound", "Rebound",
			"If true, a ritual rebounds when the Athanor stays broken for 5 seconds, or its owner stays out of EMC for 10 seconds in Citrinitas or Rubedo. "
			+ "A rebound destroys some or all of the ingredients. If false, the ritual only waits."),
	SERVER_RITUAL_REBOUND_BREAKS_BLOCKS("server.ritual.rebound_breaks_blocks", "Rebound Breaks Blocks", "If true, the explosion of a rebound in Rubedo breaks blocks."),

	//EMC Mapping
	MAPPING_DUMP_TO_FILE("mapping.dump_to_file", "Dump Everything To File",
			"Want to take a look at the internals of EMC Calculation? Enable this to write all the conversions and setValue-Commands to config/ProjectE/mapping_dump.json"),
	MAPPING_PREGENERATED("mapping.pregenerated", "Pregenerate EMC",
			"When the next EMC mapping occurs write the results to config/ProjectE/pregenerated_emc.json and only ever run the mapping again when that file "
			+ "does not exist, this setting is set to false, or an error occurred parsing that file."),
	MAPPING_RECOVER_MISSING_ITEMS("mapping.recover_missing_items", "Recover Missing Item EMC (Experimental)",
			"Opt-in modpack policy: retain acyclic recipe values, defer positive item values below 1 EMC to a minimum of 1, and recover missing items "
			+ "from valued recipes after cleanup. Can retain profitable recipe cycles and changes balance; does not guarantee exploit-free EMC. "
			+ "Disable pregenerated EMC to recalculate after changing this setting."),
	MAPPING_LOG_EXPLOITS("mapping.log_exploits", "Log Profitable EMC Conversion Mismatches",
			"Logs known profitable EMC conversion mismatches where a recipe costs less than the total EMC value of its outputs and the output value "
			+ "is retained because it is fixed/custom or comes from a forced conversion. This cannot find conversions that are unknown to ProjectE."),

	MAPPING_MAPPERS("mapping.mapper", "EMC Mappers", "Used to configure settings for the various EMC Mappers.", true),

	MAPPING_BREWING_MAPPER("mapping.mapper.brewing", "Brewing Mapper", "Add Conversions for Brewing Recipes.", true),
	MAPPING_OXIDATION_MAPPER("mapping.mapper.oxidation", "Oxidization Mapper", "Add Conversions for all oxidizable blocks.", true),
	MAPPING_TAG_MAPPER("mapping.mapper.tag", "Tag Mapper",
			"Adds back and forth conversions of objects and their Tag variant. (EMC values assigned to tags will not behave properly if this mapper is disabled)",
			"Edit Tags"),
	MAPPING_WAXABLE_MAPPER("mapping.mapper.waxable", "Waxable Mapper", "Add Conversions for all waxable blocks", true),

	MAPPING_CUSTOM_CONVERSION_MAPPER("mapping.mapper.custom.conversion", "Custom Conversion Mapper",
			"Loads json files within datapacks (data/<domain>/pe_custom_conversions/*.json) to add values and conversions.", "Edit Custom Conversions"),
	MAPPING_CUSTOM_EMC_MAPPER("mapping.mapper.custom.emc", "Custom EMC Mapper", "Uses the `custom_emc.json` File to add EMC values.", "Edit Custom EMC"),

	MAPPING_CRT_CONVERSION_MAPPER("mapping.mapper.crt.conversion", "CrT Conversion EMC Mapper",
			"Allows adding custom conversions through CraftTweaker. This behaves similarly to if someone used a custom conversion file instead.",
			"Edit CrT Conversion"),
	MAPPING_CRT_EMC_MAPPER("mapping.mapper.crt.emc", "CrT Custom EMC Mapper",
			"Allows setting EMC values through CraftTweaker. This behaves similarly to if someone used the custom emc file instead.", "Edit CrT Custom EMC"),

	MAPPING_BLACKLIST_ORE_MAPPER("mapping.mapper.blacklist.ore", "Ore Blacklist Mapper", "Set EMC=0 for everything in the c:ores tag.",
			"Edit Ore Blacklist"),
	MAPPING_BLACKLIST_RAW_ORE_MAPPER("mapping.mapper.blacklist.raw_ore", "Raw Ore Blacklist Mapper",
			"Set EMC=0 for everything in the c:raw_materials tag.", "Edit Raw Ores Blacklist"),

	MAPPING_CRAFTING_MAPPER("mapping.mapper.crafting", "Crafting Mapper",
			"Add Conversions for Crafting Recipes gathered from net.minecraft.world.item.crafting.RecipeManager", true),
	MAPPING_CRAFTING_MAPPER_VANILLA("mapping.mapper.crafting.vanilla", "Vanilla Recipe Types", "Maps the different vanilla recipe types.", true),
	MAPPING_CRAFTING_MAPPER_SMITHING("mapping.mapper.crafting.smithing", "Smithing", "Maps smithing recipes.", true),
	MAPPING_CRAFTING_MAPPER_ATHANOR("mapping.mapper.crafting.athanor", "Athanor",
			"Maps the rituals of the Athanor. The result is worth its ingredients, the catalyst if it is used up, and the personal EMC the ritual costs."),
	MAPPING_CRAFTING_MAPPER_FALLBACK("mapping.mapper.crafting.fallback", "Fallback",
			"Fallback for default handling of recipes that extend ICraftingRecipe, AbstractCookingRecipe, SingleItemRecipe, or SmithingRecipe. "
			+ "This will catch modded extensions of the vanilla recipe classes, and if the VanillaRecipeTypes mapper is disabled, "
			+ "this mapper will still catch the vanilla recipes.", true),

	MAPPING_CRAFTING_MAPPER_MARK_HANDLED("mapping.mapper.crafting.mark_handled", "Mark Special Recipes Handled",
			"This mapper does not add conversions. It marks self-referencing recipes or recipes supported by data component processors as handled, "
			+ "and classifies known dynamic recipes that cannot be represented by a deterministic EMC conversion.", "Edit Special Recipes"),
	MAPPING_CRAFTING_MAPPER_SHULKER_RECOLORING("mapping.mapper.crafting.shulker_recoloring", "Shulker Recoloring Mapper",
			"Propagates shulker box values to colored variants.", "Edit Shulker Recoloring"),
	MAPPING_CRAFTING_MAPPER_TIPPED_ARROW("mapping.mapper.crafting.tipped_arrow", "Tipped Arrow Mapper",
			"Add conversions for all lingering potions to arrow recipes.", "Edit Tipped Arrows"),
	MAPPING_CRAFTING_MAPPER_DECORATED_POT("mapping.mapper.crafting.decorated_pot", "Decorated Pot Mapper",
			"Adds conversions for all the different decorated pot combinations.", "Edit Decorated Pots"),
	MAPPING_CRAFTING_MAPPER_SUSPICIOUS_STEW("mapping.mapper.crafting.suspicious_stew", "Suspicious Stew Mapper",
			"Adds conversions for all the different types of suspicious stews.", "Edit Suspicious Stew"),

	MAPPING_MAPPER_ENABLED("mapping.mapper.enabled", "Enabled", "Determines whether this EMC Mapper is enabled."),
	MAPPING_RECIPE_TYPE_MAPPER_ENABLED("mapping.mapper.recipe_type.enabled", "Enabled", "Determines whether this Recipe Type Mapper is enabled."),

	//Data Component Processors
	MAPPING_PROCESSORS("mapping.processors", "Data Component Processors", "Used to configure settings for the various Data Component Processors.",
			"Edit Processors"),

	DCP_ARMOR_TRIM("processing.data_component_processor.armor_trim", "Armor Trim Processor", "Calculates EMC value of trimmed armor.", true),
	DCP_BANNERS("processing.data_component_processor.banner", "Banner Processor", "Calculates EMC value of patterned banners.", true),
	DCP_BUNDLE("processing.data_component_processor.bundle", "Bundle Processor", "Calculates EMC value of items stored in bundles.", true),
	DCP_CONTAINER("processing.data_component_processor.container", "Container Processor", "Calculates EMC value of items stored in vanilla's container component. For example shulker boxes.", true),
	DCP_DAMAGE("processing.data_component_processor.damage", "Damage Processor", "Reduces the EMC value the more damaged an item is.", true),
	DCP_DECORATED_POT("processing.data_component_processor.decorated_pot", "Decorated Pot Processor",
			"Takes the different sherds into account for each decorated pot.", "Edit Pot Processor"),
	DCP_DECORATED_SHIELD("processing.data_component_processor.decorated_shield", "Decorated Shield Processor", "Calculates EMC value of decorated shield.",
			"Edit Shield Processor"),
	DCP_ENCHANTMENT("processing.data_component_processor.enchantment", "Enchantment Processor",
			"Increases the EMC value to take into account any enchantments on an item.", true),
	DCP_ENCHANTMENT_EMC_BONUS("processing.data_component_processor.enchantment.emc_bonus", "Enchantment EMC Bonus",
			"The amount (scaled by rarity) to increase EMC value by for enchantments."),

	DCP_FIREWORK("processing.data_component_processor.firework", "Firework Processor", "Calculates EMC value of fireworks.", true),
	DCP_FIREWORK_STAR("processing.data_component_processor.firework_star", "Firework Star Processor",
			"Calculates the EMC value of Firework Stars, based on what was required to craft them.", "Edit Star Processor"),
	DCP_MAP_EXTENSION("processing.data_component_processor.map_extension", "Map Extension Processor",
			"Increases the EMC value of maps that have had their range extended.", "Edit Map Ext. Processor"),
	DCP_MERCURIAL_EYE("processing.data_component_processor.mercurial_eye", "Mercurial Eye Processor",
			"Factors in the EMC value of Klein Stars stored in Mercurial eyes.", "Edit Eye Processor"),
	DCP_STORED_EMC("processing.data_component_processor.stored_emc", "Stored EMC Processor",
			"Increases the EMC value of the item to take into account any EMC the item has stored.", true),
	DCP_WRITABLE_BOOK("processing.data_component_processor.writable_book", "Writable Book Processor",
			"Allows persisting the contents of writable books. Does not change the EMC value.", "Edit Writable Books"),
	DCP_WRITTEN_BOOK("processing.data_component_processor.written_book", "Written Book Processor",
			"Allows persisting the contents of written books. Does not change the EMC value.", true),

	DCP_ENABLED("processing.enabled", "Enabled", "Determines whether this Data Component Processor is enabled and can adjust the EMC value of items."),
	DCP_PERSISTENT("processing.persistent", "Persistent",
			"Determines whether this Data Component Processor can affect the persistent data that gets saved to knowledge/copied in a condenser."),

	;

	private final String key;
	private final String title;
	private final String tooltip;
	@Nullable
	private final String button;

	PEConfigTranslations(String path, String title, String tooltip) {
		this(path, title, tooltip, false);
	}

	PEConfigTranslations(String path, String title, String tooltip, boolean isSection) {
		this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection));
	}

	PEConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
		this.key = Util.makeDescriptionId("configuration", PECore.rl(path));
		this.title = title;
		this.tooltip = tooltip;
		this.button = button;
	}

	@NotNull
	@Override
	public String getTranslationKey() {
		return key;
	}

	@Override
	public String title() {
		return title;
	}

	@Override
	public String tooltip() {
		return tooltip;
	}

	@Nullable
	@Override
	public String button() {
		return button;
	}
}
