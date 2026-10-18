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

	public static final List<Chapter> CHAPTERS = List.of(
			new Chapter("basics", "philosophers_stone", List.of(
					Entry.topic("emc", "transmutation_table"),
					Entry.topic("knowledge", "tome"),
					Entry.topic("keybinds", "minecraft:compass"),
					Entry.item("false_philosophers_stone"),
					Entry.item("transmutation_table"),
					Entry.item("alchemical_crucible"),
					Entry.topic("magnum_opus", "minecraft:nether_star"),
					Entry.item("philosophers_stone"),
					Entry.item("transmutation_tablet"),
					Entry.item("tome"),
					Entry.topic("covalence_dust", "low_covalence_dust"),
					Entry.topic("fuels", "alchemical_coal"),
					Entry.item("dark_matter"),
					Entry.item("red_matter"),
					Entry.topic("klein_stars", "klein_star_ein")
			)),
			new Chapter("machines", "condenser_mk1", List.of(
					Entry.item("alchemical_chest"),
					Entry.item("alchemical_barrel"),
					Entry.topic("alchemical_bags", "white_alchemical_bag"),
					Entry.item("condenser_mk1"),
					Entry.item("condenser_mk2"),
					Entry.topic("collectors", "collector_mk1"),
					Entry.topic("relays", "relay_mk1"),
					Entry.item("dm_furnace"),
					Entry.item("rm_furnace"),
					Entry.item("dm_pedestal"),
					Entry.topic("interdiction", "interdiction_torch"),
					Entry.topic("nova", "nova_catalyst")
			)),
			new Chapter("tools", "dm_pick", List.of(
					Entry.topic("dm_tools", "dm_pick"),
					Entry.topic("rm_tools", "rm_pick"),
					Entry.item("rm_katar"),
					Entry.item("rm_morning_star"),
					Entry.topic("tridents", "dark_matter_trident"),
					Entry.topic("shields", "dark_matter_shield"),
					Entry.topic("dm_armor", "dm_chestplate"),
					Entry.topic("rm_armor", "rm_chestplate"),
					Entry.topic("gem_armor", "gem_chestplate"),
					Entry.item("destruction_catalyst"),
					Entry.item("hyperkinetic_lens"),
					Entry.item("catalytic_lens"),
					Entry.item("mercurial_eye"),
					Entry.topic("divining_rods", "divining_rod_1"),
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
					Entry.topic("matter_tiers", "magenta_matter"),
					Entry.topic("tier_fuels", "magenta_fuel"),
					Entry.topic("tier_collectors", "basic_collector"),
					Entry.topic("compressed_collectors", "basic_compressed_collector"),
					Entry.topic("tier_relays", "basic_relay"),
					Entry.topic("power_flowers", "basic_power_flower"),
					Entry.topic("emc_links", "basic_emc_link"),
					Entry.item("compact_sun"),
					Entry.item("matter_upgrader"),
					Entry.item("condenser_mk3"),
					Entry.item("transmutation_interface"),
					Entry.topic("advanced_alchemical_chests", "white_advanced_alchemical_chest"),
					Entry.topic("tier_table", "final_collector")
			)),
			new Chapter("artifacts", "final_star", List.of(
					Entry.topic("big_stars", "magnum_star_ein"),
					Entry.item("final_star_shard"),
					Entry.item("final_star"),
					Entry.topic("alchemical_books", "basic_alchemical_book"),
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
	 * @param itemTitle whether the entry is about a single item and is titled by its name, rather than by a title of its own
	 */
	public record Entry(String id, Identifier iconId, boolean itemTitle) {

		public static Entry item(String id) {
			return new Entry(id, Codex.id(id), true);
		}

		public static Entry topic(String id, String icon) {
			return new Entry(id, Codex.id(icon), false);
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
