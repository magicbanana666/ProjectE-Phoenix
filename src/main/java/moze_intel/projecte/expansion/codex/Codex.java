package moze_intel.projecte.expansion.codex;

import java.util.List;
import moze_intel.projecte.PECore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * The contents of the Alchemist's Codex: its chapters and the entries in them.
 * <p>
 * Entries are referenced by item id rather than by registry object, so the codex does not care which part of the mod an
 * item comes from. All the text lives in the language files, under {@code codex.projecte.*}.
 */
public final class Codex {

	public static final String TITLE = "codex.projecte.title";
	public static final String INTRO = "codex.projecte.intro";
	public static final String CONTENTS = "codex.projecte.contents";
	public static final String HOME = "codex.projecte.home";
	public static final String BACK = "codex.projecte.back";
	public static final String SEARCH = "codex.projecte.search";
	public static final String BOOKMARK_EMPTY = "codex.projecte.bookmark.empty";
	public static final String BOOKMARK_USED = "codex.projecte.bookmark.used";
	public static final String SEARCH_NOTHING = "codex.projecte.search.nothing";
	public static final String CHAPTERS_TITLE = "codex.projecte.chapters";
	public static final String PAGE_CRAFTING = "codex.projecte.page.crafting";
	public static final String PAGE_SHAPELESS = "codex.projecte.page.shapeless";
	public static final String PAGE_RITUAL = "codex.projecte.page.ritual";
	public static final String PAGE_CAULDRON = "codex.projecte.page.cauldron";
	public static final String CAULDRON_CONDITIONS = "codex.projecte.cauldron.conditions";
	public static final String CAULDRON_SOULS = "codex.projecte.cauldron.souls";
	public static final String CAULDRON_BECOMES = "codex.projecte.cauldron.becomes";
	public static final String PAGE_STRUCTURE = "codex.projecte.page.structure";
	public static final String PAGE_STRUCTURE_GRAND = "codex.projecte.page.structure.grand";
	public static final String STRUCTURE_SWITCH = "codex.projecte.structure.switch";
	public static final String PAGE_RITUAL_GRAND = "codex.projecte.page.ritual.grand";
	public static final String RITUAL_CATALYST = "codex.projecte.ritual.catalyst";
	public static final String RITUAL_STAGE = "codex.projecte.ritual.stage";
	public static final String RITUAL_TOTAL = "codex.projecte.ritual.total";
	public static final String RITUAL_NEEDS = "codex.projecte.ritual.needs";
	public static final String RITUAL_KEEPS_CATALYST = "codex.projecte.ritual.keeps_catalyst";
	public static final String RITUAL_CATALYST_FULL = "codex.projecte.ritual.catalyst_full";
	public static final String STRUCTURE_LAYER = "codex.projecte.structure.layer";

	public static final List<Chapter> CHAPTERS = List.of(
			new Chapter("basics", "philosophers_stone", List.of(
					Entry.topic("emc", "transmutation_table"),
					Entry.topic("knowledge", "tome"),
					Entry.item("monocle_of_comprehension"),
					Entry.topic("keybinds", "minecraft:compass"),
					Entry.item("alchemical_cauldron"),
					Entry.topic("tria_prima", "alchemical_salt").withRecipes("alchemical_salt", "alchemical_sulphur", "quicksilver"),
					Entry.topic("circles", "salt_chalk").withRecipes("salt_chalk", "sulphur_chalk", "mercury_chalk"),
					Entry.topic("souls", "minecraft:soul_lantern").withRecipes("soul_vial"),
					Entry.topic("dragons_pulse", "divining_rod_3"),
					Entry.item("pulse_stake"),
					Entry.item("alkahest"),
					Entry.topic("alchemists_garb", "alchemists_robe").withRecipes("alchemists_hood", "alchemists_robe", "alchemists_trousers", "alchemists_boots"),
					Entry.item("false_philosophers_stone"),
					Entry.item("transmutation_table"),
					Entry.item("alchemical_crucible").withStructure(),
					Entry.topic("magnum_opus", "minecraft:nether_star").withRecipes("philosophers_stone"),
					Entry.item("philosophers_stone"),
					Entry.item("transmutation_tablet"),
					Entry.item("tome"),
					Entry.topic("covalence_dust", "low_covalence_dust").withRecipes("low_covalence_dust", "medium_covalence_dust", "high_covalence_dust"),
					Entry.topic("fuels", "alchemical_coal").withRecipes("alchemical_coal", "mobius_fuel", "aeternalis_fuel"),
					Entry.item("dark_matter"),
					Entry.item("red_matter"),
					Entry.topic("klein_stars", "klein_star_ein")
							.withRecipes("klein_star_ein", "klein_star_zwei", "klein_star_drei", "klein_star_vier", "klein_star_sphere", "klein_star_omega")
			)),
			new Chapter("machines", "condenser_mk1", List.of(
					Entry.item("alchemical_chest"),
					Entry.item("alchemical_barrel"),
					Entry.topic("alchemical_bags", "white_alchemical_bag").withRecipes("white_alchemical_bag"),
					Entry.item("condenser_mk1"),
					Entry.item("condenser_mk2"),
					Entry.topic("collectors", "collector_mk1").withRecipes("collector_mk1", "collector_mk2", "collector_mk3"),
					Entry.topic("relays", "relay_mk1").withRecipes("relay_mk1", "relay_mk2", "relay_mk3"),
					Entry.item("dm_furnace"),
					Entry.item("rm_furnace"),
					Entry.item("dm_pedestal"),
					Entry.topic("interdiction", "interdiction_torch").withRecipes("interdiction_torch", "interdiction_lantern"),
					Entry.topic("nova", "nova_catalyst").withRecipes("nova_catalyst", "nova_cataclysm")
			)),
			new Chapter("tools", "dm_pick", List.of(
					Entry.topic("dm_tools", "dm_pick").withRecipes("dm_pick", "dm_axe", "dm_shovel", "dm_sword", "dm_hoe", "dm_shears", "dm_hammer"),
					Entry.topic("rm_tools", "rm_pick").withRecipes("rm_pick", "rm_axe", "rm_shovel", "rm_sword", "rm_hoe", "rm_shears", "rm_hammer"),
					Entry.item("rm_katar"),
					Entry.item("rm_morning_star"),
					Entry.topic("tridents", "dark_matter_trident").withRecipes("dark_matter_trident", "red_matter_trident"),
					Entry.topic("shields", "dark_matter_shield").withRecipes("dark_matter_shield", "red_matter_shield"),
					Entry.topic("dm_armor", "dm_chestplate").withRecipes("dm_helmet", "dm_chestplate", "dm_leggings", "dm_boots"),
					Entry.topic("rm_armor", "rm_chestplate").withRecipes("rm_helmet", "rm_chestplate", "rm_leggings", "rm_boots"),
					Entry.topic("gem_armor", "gem_chestplate").withRecipes("gem_helmet", "gem_chestplate", "gem_leggings", "gem_boots"),
					Entry.item("destruction_catalyst"),
					Entry.item("hyperkinetic_lens"),
					Entry.item("catalytic_lens"),
					Entry.item("mercurial_eye"),
					Entry.topic("divining_rods", "divining_rod_1").withRecipes("divining_rod_1", "divining_rod_2", "divining_rod_3"),
					Entry.topic("alchemical_collection", "minecraft:enchanted_book")
			)),
			new Chapter("trinkets", "arcana_ring", List.of(
					Entry.item("iron_band"),
					Entry.item("swiftwolf_rending_gale"),
					Entry.item("zero_ring"),
					Entry.item("ignition_ring"),
					Entry.item("harvest_goddess_band"),
					Entry.item("archangel_smite"),
					Entry.item("black_hole_band"),
					Entry.item("void_ring"),
					Entry.item("arcana_ring"),
					Entry.item("body_stone"),
					Entry.item("soul_stone"),
					Entry.item("life_stone"),
					Entry.item("mind_stone"),
					Entry.item("watch_of_flowing_time"),
					Entry.item("gem_of_eternal_density"),
					Entry.item("evertide_amulet"),
					Entry.item("volcanite_amulet"),
					Entry.item("repair_talisman")
			)),
			new Chapter("beyond", "magenta_matter", List.of(
					Entry.topic("matter_tiers", "magenta_matter")
							.withRecipes("magenta_matter", "pink_matter", "purple_matter", "violet_matter", "blue_matter", "cyan_matter", "green_matter",
									"lime_matter", "yellow_matter", "orange_matter", "white_matter", "fading_matter"),
					Entry.topic("tier_fuels", "magenta_fuel").withRecipes("magenta_fuel"),
					Entry.topic("tier_collectors", "basic_collector").withRecipes("basic_collector"),
					Entry.topic("compressed_collectors", "basic_compressed_collector").withRecipes("basic_compressed_collector"),
					Entry.topic("tier_relays", "basic_relay").withRecipes("basic_relay"),
					Entry.topic("power_flowers", "basic_power_flower").withRecipes("basic_power_flower"),
					Entry.topic("emc_links", "basic_emc_link").withRecipes("basic_emc_link"),
					Entry.item("compact_sun"),
					Entry.item("matter_upgrader"),
					Entry.item("condenser_mk3"),
					Entry.item("transmutation_interface"),
					Entry.topic("advanced_alchemical_chests", "white_advanced_alchemical_chest").withRecipes("white_advanced_alchemical_chest"),
					Entry.topic("tier_table", "final_collector")
			)),
			new Chapter("artifacts", "final_star", List.of(
					Entry.topic("boss_drops", "warden_heart"),
					Entry.topic("big_stars", "magnum_star_ein").withRecipes("magnum_star_ein"),
					Entry.item("final_star_shard"),
					Entry.item("final_star"),
					Entry.topic("alchemical_books", "basic_alchemical_book").withRecipes("basic_alchemical_book"),
					Entry.item("arcane_transmutation_tablet"),
					Entry.item("knowledge_sharing_book"),
					Entry.item("infinite_fuel"),
					Entry.item("infinite_steak")
			)),
			new Chapter("server", "minecraft:command_block", List.of(
					Entry.topic("commands", "minecraft:command_block"),
					Entry.topic("emc_values", "minecraft:writable_book"),
					Entry.topic("integrations", "minecraft:chest"),
					Entry.topic("configs", "minecraft:comparator")
			))
	);

	private Codex() {
	}

	private static ItemStack stack(Identifier id) {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(id));
	}

	/**
	 * Ids without a namespace are ProjectE items.
	 */
	private static Identifier id(String id) {
		return id.contains(":") ? Identifier.parse(id) : Identifier.fromNamespaceAndPath(PECore.MODID, id);
	}

	public record Chapter(String id, Identifier iconId, List<Entry> entries) {

		public Chapter(String id, String icon, List<Entry> entries) {
			this(id, Codex.id(icon), entries);
		}

		public String titleKey() {
			return "codex.projecte.chapter." + id;
		}

		public String introKey() {
			return titleKey() + ".intro";
		}

		public Component title() {
			return Component.translatable(titleKey());
		}

		public Component intro() {
			return Component.translatable(introKey());
		}

		public ItemStack icon() {
			return stack(iconId);
		}
	}

	/**
	 * @param itemTitle   whether the entry is about a single item and is titled by its name, rather than by a title of its own
	 * @param recipeItems the items whose recipes the entry shows after its text
	 * @param structure   whether the entry shows the layers of the Athanor
	 */
	public record Entry(String id, Identifier iconId, boolean itemTitle, List<Identifier> recipeItems, boolean structure) {

		public static Entry item(String id) {
			return new Entry(id, Codex.id(id), true, List.of(Codex.id(id)), false);
		}

		/**
		 * A topic shows no recipes, unless it lists the items with {@link #withRecipes(String...)}.
		 */
		public static Entry topic(String id, String icon) {
			return new Entry(id, Codex.id(icon), false, List.of(), false);
		}

		public Entry withRecipes(String... items) {
			return new Entry(id, iconId, itemTitle, java.util.Arrays.stream(items).map(Codex::id).toList(), structure);
		}

		public Entry withStructure() {
			return new Entry(id, iconId, itemTitle, recipeItems, true);
		}

		public String titleKey() {
			return "codex.projecte.entry." + id;
		}

		public String textKey() {
			return titleKey() + ".text";
		}

		public Component title() {
			return itemTitle ? icon().getHoverName() : Component.translatable(titleKey());
		}

		public Component text() {
			return Component.translatable(textKey());
		}

		public ItemStack icon() {
			return stack(iconId);
		}
	}
}
