package moze_intel.projecte.expansion.client;

import java.util.List;
import java.util.Map;
import moze_intel.projecte.PECore;
import net.minecraft.resources.Identifier;

/**
 * Static string tables that the merged ProjectExpansion GUI, command and tooltip code needs.
 *
 * <p>Everything here used to be spread over a handful of source classes:
 * {@code util/Matter} and {@code util/Fuel} (tier grouping and level numbers),
 * {@code util/TagNames} (the string keys of the emc storage / emc link components),
 * {@code util/TableGenerator} (the glyph width buckets used to draw aligned chat tables),
 * and the hard coded texture paths of the collector GUI.
 * They are collected in one place so the ported code has a single, dependency free place to read
 * them from instead of duplicating literals.</p>
 *
 * <p>How to read this class:</p>
 * <ul>
 *     <li>{@link #MATTER_LEVELS} / {@link #MATTER_TIERS} and {@link #FUEL_TIERS} give the rarity
 *     grouping and the upgrade level of a value, keyed by the lower case enum name
 *     ({@code basic}, {@code dark}, ... {@code final}).</li>
 *     <li>{@link #COMPONENT_KEYS} holds the serialized component names used by the collector,
 *     relay, emc link and the energy items.</li>
 *     <li>{@link #GLYPH_WIDTH} maps a character to the number of "half cells" it occupies, which is
 *     what {@code TableGenerator} needs to align its columns.</li>
 *     <li>{@link #COLLECTOR_GUI_TEXTURES} holds the collector screen background, the emc import and
 *     export arrows and the MK3 condenser input / output slots.</li>
 * </ul>
 *
 * <p>User facing text is <b>not</b> here - it lives in {@code assets/projecte/lang/en_us.json} and is
 * emitted by {@code moze_intel.projecte.expansion.lang.ExpansionLangProvider}. The constants below are
 * only the parts that have to be known without a language lookup: units, numbers, ids and paths.</p>
 */
public final class ExpansionTextConstants {

	private ExpansionTextConstants() {
	}

	// ------------------------------------------------------------------
	// Units. The matching language keys are pe.emc_rate / pe.fluid_rate and friends.
	// ------------------------------------------------------------------

	/** Suffix for a per tick rate, appended after the value. */
	public static final String EMC_PER_TICK = " EMC/t";
	/** Suffix for a per second rate, as shown by the collector and the relay tooltips. */
	public static final String EMC_PER_SECOND = " EMC/s";
	/** Suffix for a fluid transfer rate, in millibuckets per second. */
	public static final String FLUID_PER_SECOND = " mB/s";
	/** Suffix used by the relay for the flat bonus it hands out every second. */
	public static final String RELAY_BONUS_PER_SECOND = "/s";
	/** Separator between a value and its unit, used when a tooltip is assembled in code. */
	public static final String UNIT_SEPARATOR = " ";
	/** Ticks in a second, the divisor used by every "per second" to "per tick" conversion. */
	public static final int TICKS_PER_SECOND = 20;

	// ------------------------------------------------------------------
	// Rarity tiers. Values are the lower case enum names, the same ids the codecs serialise.
	// ------------------------------------------------------------------

	/** {@code util/Matter} values that share the "common" rarity, in enum order. */
	public static final List<String> MATTER_COMMON = List.of("basic", "dark", "red");
	/** {@code util/Matter} values that share the "uncommon" rarity, in enum order. */
	public static final List<String> MATTER_UNCOMMON = List.of("magenta", "purple", "violet", "blue");
	/** {@code util/Matter} values that share the "rare" rarity, in enum order. */
	public static final List<String> MATTER_RARE = List.of("cyan", "green", "lime", "yellow");
	/** {@code util/Matter} values that share the "epic" rarity, in enum order. */
	public static final List<String> MATTER_EPIC = List.of("orange", "white", "fading", "final");
	/** Every {@code util/Matter} value, in enum order. */
	public static final List<String> MATTER_VALUES = List.of("basic", "dark", "red", "magenta", "pink", "purple", "violet",
			"blue", "cyan", "green", "lime", "yellow", "orange", "white", "fading", "final");

	/** {@code util/Fuel} values that share the "common" rarity, in enum order. */
	public static final List<String> FUEL_COMMON = List.of("alchemical", "mobius", "aeternalis");
	/** {@code util/Fuel} values that share the "uncommon" rarity, in enum order. */
	public static final List<String> FUEL_UNCOMMON = List.of("magenta", "purple", "violet", "blue");
	/** {@code util/Fuel} values that share the "rare" rarity, in enum order. */
	public static final List<String> FUEL_RARE = List.of("cyan", "green", "lime", "yellow");
	/** {@code util/Fuel} values that share the "epic" rarity, in enum order. */
	public static final List<String> FUEL_EPIC = List.of("orange", "white");
	/** Every {@code util/Fuel} value, in enum order. */
	public static final List<String> FUEL_VALUES = List.of("alchemical", "mobius", "aeternalis", "magenta", "pink",
			"purple", "violet", "blue", "cyan", "green", "lime", "yellow", "orange", "white");

	/**
	 * The matter upgrade level of every value, as used by the matter upgrader. Two values with the
	 * same level can be upgraded into each other, which is why {@code basic} and {@code dark} differ
	 * while {@code magenta} and {@code red} share level 4.
	 */
	public static final Map<String, Integer> MATTER_LEVELS = Map.ofEntries(
			Map.entry("basic", 0),
			Map.entry("dark", 2),
			Map.entry("red", 4),
			Map.entry("magenta", 4),
			Map.entry("pink", 5),
			Map.entry("purple", 5),
			Map.entry("violet", 6),
			Map.entry("blue", 6),
			Map.entry("cyan", 7),
			Map.entry("green", 7),
			Map.entry("lime", 8),
			Map.entry("yellow", 8),
			Map.entry("orange", 9),
			Map.entry("white", 9),
			Map.entry("fading", 10),
			Map.entry("final", 10));

	/**
	 * The rarity of a matter or fuel value, spelled the way the vanilla {@code Rarity} enum spells it.
	 *
	 * @param value lower case enum name, see {@link #MATTER_VALUES} and {@link #FUEL_VALUES}
	 * @return one of {@code common}, {@code uncommon}, {@code rare} or {@code epic}
	 */
	public static String rarityOf(String value) {
		if (MATTER_COMMON.contains(value) || FUEL_COMMON.contains(value)) {
			return "common";
		}
		if (MATTER_UNCOMMON.contains(value) || FUEL_UNCOMMON.contains(value)) {
			return "uncommon";
		}
		if (MATTER_RARE.contains(value) || FUEL_RARE.contains(value)) {
			return "rare";
		}
		return "epic";
	}

	// ------------------------------------------------------------------
	// Serialized component names, formerly util/TagNames.
	// ------------------------------------------------------------------

	/** Emc currently sitting in the collector / relay / emc link storage. */
	public static final String KEY_STORED_EMC = "StoredEMC";
	/** Flat emc bonus a relay hands to every collector below it. */
	public static final String KEY_BONUS_EMC = "BonusEMC";
	/** Uuid of the player that owns an emc container or an alchemical book. */
	public static final String KEY_OWNER = "Owner";
	/** Cached name of {@link #KEY_OWNER}, so the book tooltip does not need a server round trip. */
	public static final String KEY_OWNER_NAME = "OwnerName";
	/** The single item an emc link imports or exports. */
	public static final String KEY_ITEM = "Item";
	/** Emc that is produced but not yet pushed into the storage. */
	public static final String KEY_REMAINING_EMC = "RemainingEMC";
	/** Per tick item import budget still available this tick. */
	public static final String KEY_REMAINING_IMPORT = "RemainingImport";
	/** Per tick item export budget still available this tick. */
	public static final String KEY_REMAINING_EXPORT = "RemainingExport";
	/** Per tick fluid export budget still available this tick, in millibuckets. */
	public static final String KEY_REMAINING_FLUID = "RemainingFluid";
	/** Network position of a collector / relay, used by the map overlay. */
	public static final String KEY_X = "X";
	/** Network position of a collector / relay, used by the map overlay. */
	public static final String KEY_Y = "Y";
	/** Network position of a collector / relay, used by the map overlay. */
	public static final String KEY_Z = "Z";
	/** Display name of a transmutation interface or an advanced alchemical chest. */
	public static final String KEY_NAME = "Name";
	/** Dimension a transmutation interface or a book destination points at. */
	public static final String KEY_DIMENSION = "Dimension";
	/** Selected page of a multi page container. */
	public static final String KEY_INDEX = "Index";
	/** Emc that has been generated but has not gone through the fluid / item conversion yet. */
	public static final String KEY_UNPROCESSED_EMC = "UnprocessedEMC";
	/** Locked input item of a condenser side. */
	public static final String KEY_INPUT = "Input";
	/** Locked output item of a condenser side. */
	public static final String KEY_OUTPUT = "Output";
	/** Auxiliary upgrade slots of a collector or a relay. */
	public static final String KEY_AUX_SLOTS = "AuxSlots";
	/** Generic numeric value, used by the fuel and collector upgrade components. */
	public static final String KEY_VALUE = "Value";
	/** Target lock of a condenser. */
	public static final String KEY_LOCK = "Lock";
	/** Stored teleportation destinations of an alchemical book. */
	public static final String KEY_LOCATIONS = "Locations";

	// ------------------------------------------------------------------
	// Glyph widths for the chat table renderer, formerly util/TableGenerator.
	// The value is the width in "half cells", a normal glyph being 1.
	// ------------------------------------------------------------------

	/** Characters that are one half cell wide. */
	public static final List<Character> GLYPH_WIDTH_1 = List.of('៲');
	/** Characters that are one and a half cells wide. */
	public static final List<Character> GLYPH_WIDTH_2 = List.of(',', '.', '!', 'i', '´', ':', ';', '|');
	/** Characters that are two cells wide. */
	public static final List<Character> GLYPH_WIDTH_3 = List.of('l', '`', '³', '\'');
	/** Characters that are two and a half cells wide. */
	public static final List<Character> GLYPH_WIDTH_4 = List.of('I', 't', ' ', '[', ']', '€');
	/** Characters that are three and a half cells wide. */
	public static final List<Character> GLYPH_WIDTH_5 = List.of('"', '{', '}', '(', ')', '*', 'f', 'k', '<', '>');
	/** Characters that are four and a half cells wide. */
	public static final List<Character> GLYPH_WIDTH_7 = List.of('°', '~', '@');

	/**
	 * @param glyph the character to measure
	 * @return how many half cells the glyph occupies, at least 1
	 */
	public static int glyphWidth(char glyph) {
		if (GLYPH_WIDTH_1.contains(glyph)) {
			return 1;
		} else if (GLYPH_WIDTH_2.contains(glyph)) {
			return 2;
		} else if (GLYPH_WIDTH_3.contains(glyph)) {
			return 3;
		} else if (GLYPH_WIDTH_4.contains(glyph)) {
			return 4;
		} else if (GLYPH_WIDTH_5.contains(glyph)) {
			return 5;
		} else if (GLYPH_WIDTH_7.contains(glyph)) {
			return 7;
		}
		return 1;
	}

	// ------------------------------------------------------------------
	// Gui resources
	// ------------------------------------------------------------------

	/** Background of the collector screen, the "mk1" variant. */
	public static final Identifier GUI_COLLECTOR_1 = PECore.rl("textures/gui/collector1.png");
	/** Background of the collector screen, the "mk2" variant. */
	public static final Identifier GUI_COLLECTOR_2 = PECore.rl("textures/gui/collector2.png");
	/** Background of the collector screen, the "mk3" variant. */
	public static final Identifier GUI_COLLECTOR_3 = PECore.rl("textures/gui/collector3.png");
	/** Arrow pointing into an emc link, the import side. */
	public static final Identifier GUI_EMC_IMPORT = PECore.rl("textures/gui/emc_import.png");
	/** Arrow pointing out of an emc link, the export side. */
	public static final Identifier GUI_EMC_EXPORT = PECore.rl("textures/gui/emc_export.png");
	/** Input slot overlay of the MK3 condenser. */
	public static final Identifier GUI_CONDENSER_MK3_INPUT = PECore.rl("textures/gui/condenser_mk3_input.png");
	/** Output slot overlay of the MK3 condenser. */
	public static final Identifier GUI_CONDENSER_MK3_OUTPUT = PECore.rl("textures/gui/condenser_mk3_output.png");
	/** Screen of the arcane transmutation tablet. */
	public static final Identifier GUI_ARCANE_TABLET = PECore.rl("textures/gui/arcane_transmutation_tablet.png");
	/** Slot layout overlay of the arcane transmutation tablet. */
	public static final Identifier GUI_ARCANE_TABLET_LAYOUT = PECore.rl("textures/gui/arcane_transmutation_tablet_layout.png");
}
