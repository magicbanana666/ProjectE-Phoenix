package moze_intel.projecte.expansion.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import moze_intel.projecte.client.lang.PELangProvider;
import moze_intel.projecte.expansion.codex.Codex;
import moze_intel.projecte.expansion.registries.ExpansionSoundEvents;
import moze_intel.projecte.gameObjs.items.ExpansionItemDescriptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

/**
 * Adds every ProjectExpansion translation to ProjectE's {@code en_us.json}.
 *
 * <p>This <b>extends</b> {@link PELangProvider} instead of being a second language provider:
 * {@link net.neoforged.neoforge.common.data.LanguageProvider#run} writes the whole file from its own
 * map, so two providers for the same mod id and locale would silently overwrite each other, and
 * {@code add(String, String)} throws on a duplicate key. Extending keeps a single writer and lets
 * the ProjectE keys be added first.</p>
 *
 * <p>All keys were renamed from the {@code projectexpansion} namespace to {@code projecte}; the four
 * advancements whose file had to be renamed ({@code collector}, {@code relay}) use the
 * {@code expansion_} prefixed id so that they do not collide with the core ProjectE advancements.</p>
 */
public class ExpansionLangProvider extends PELangProvider {

	public ExpansionLangProvider(PackOutput output) {
		super(output);
	}

	@Override
	protected void addTranslations() {
		//The core ProjectE keys have to be registered first, they win over any Expansion duplicate
		super.addTranslations();
		addExpansion();
		addItemDescriptions();
		addSoundEvents();
		addJadeConfig();
		addCodex();
	}

	/**
	 * The text of the Alchemist's Codex. Fails data generation when a chapter or entry has no text, or shows an item
	 * that does not exist, so that the book never shows a raw translation key or an empty icon.
	 */
	private void addCodex() {
		add("item.projecte.alchemists_codex", "Alchemist's Codex");
		add("item.projecte.alchemists_codex.tooltip", "Everything there is to know about alchemy.");
		Set<String> keys = new HashSet<>();
		CodexLang.add((key, value) -> {
			keys.add(key);
			add(key, value);
		});
		List<String> problems = new ArrayList<>();
		for (Codex.Chapter chapter : Codex.CHAPTERS) {
			checkCodex(problems, keys, chapter.titleKey(), chapter.iconId());
			checkCodex(problems, keys, chapter.introKey(), chapter.iconId());
			for (Codex.Entry entry : chapter.entries()) {
				checkCodex(problems, keys, entry.textKey(), entry.iconId());
				if (!entry.itemTitle()) {
					checkCodex(problems, keys, entry.titleKey(), entry.iconId());
				}
			}
		}
		if (!problems.isEmpty()) {
			throw new IllegalStateException("Problems in the Alchemist's Codex: " + String.join(", ", problems));
		}
	}

	private static void checkCodex(List<String> problems, Set<String> keys, String key, Identifier icon) {
		if (!keys.contains(key)) {
			problems.add("missing text " + key);
		}
		if (!BuiltInRegistries.ITEM.containsKey(icon)) {
			problems.add("unknown item " + icon + " for " + key);
		}
	}

	/**
	 * Subtitles of the sound events the addon registers. They can not be taken from its own en_us.json because the
	 * translation key of a sound event is built by the game out of the sound registry name.
	 */
	private void addSoundEvents() {
		add(ExpansionSoundEvents.KNOWLEDGE_SHARING_BOOK_STORE, "Knowledge Stored");
		add(ExpansionSoundEvents.KNOWLEDGE_SHARING_BOOK_USE, "Knowledge Gained");
		add(ExpansionSoundEvents.KNOWLEDGE_SHARING_BOOK_USE_NONE, "No Knowledge Gained");
		add(ExpansionSoundEvents.ALCHEMICAL_COLLECTION_COLLECT, "Collected with Alchemical Collection");
	}

	/**
	 * Jade builds the config key of a data provider from its uid, and asserts that the key has a translation when the
	 * game runs from a development environment. The addon used to live in its own namespace, so the key of its provider
	 * has to be added under the ProjectE namespace now.
	 */
	/**
	 * Adds the name of a block together with the {@code item.*} alias its block item needs. Since 26.x the tooltip of
	 * a block item resolves the {@code item.*} key, and a plain {@link #add(String, String)} does not create that alias.
	 */
	private void addBlock(String key, String value) {
		add(key, value);
		add("item." + key.substring("block.".length()), value);
	}

	private void addJadeConfig() {
		add("config.jade.plugin_projecte.expansion_provider", "ProjectE Phoenix Provider");
	}

	private void addItemDescriptions() {
		ExpansionItemDescriptions.descriptions().forEach(this::add);
	}

	private void addExpansion() {
		//611 keys, generated from the ProjectExpansion en_us.json
		add("advancements.projecte.aeternalis_fuel",
				"Aeternalis Fuel");
		add("advancements.projecte.aeternalis_fuel.description",
				"Obtain aeternalis fuel.");
		add("advancements.projecte.aeternalis_fuel_block",
				"Aeternalis Fuel Block");
		add("advancements.projecte.aeternalis_fuel_block.description",
				"Obtain an aeternalis fuel block.");
		add("advancements.projecte.alchemical_coal",
				"Alchemical Coal");
		add("advancements.projecte.alchemical_coal.description",
				"Obtain alchemical coal.");
		add("advancements.projecte.alchemical_coal_block",
				"Alchemical Coal Block");
		add("advancements.projecte.alchemical_coal_block.description",
				"Obtain an alchemical coal block.");
		add("advancements.projecte.condenser_mk2",
				"An even better condenser");
		add("advancements.projecte.condenser_mk2.description",
				"Obtain a Mark 2 Energy Condenser.");
		add("advancements.projecte.mobius_fuel",
				"Mobius Fuel");
		add("advancements.projecte.mobius_fuel.description",
				"Obtain mobius fuel.");
		add("advancements.projecte.mobius_fuel_block",
				"Mobius Fuel Block");
		add("advancements.projecte.mobius_fuel_block.description",
				"Obtain a mobius fuel block.");
		add("advancements.projecte.tome",
				"Tome of Knowledge");
		add("advancements.projecte.tome.description",
				"The entire universe at your fingertips.");
		add("advancements.projecte.advanced_alchemical_book",
				"Advanced Alchemical Book");
		add("advancements.projecte.advanced_alchemical_book.description",
				"Obtain an advanced alchemical book.");
		add("advancements.projecte.advanced_alchemical_chest",
				"Take it, and push it somewhere else!");
		add("advancements.projecte.advanced_alchemical_chest.description",
				"An even better upgrade??");
		add("advancements.projecte.arcane_alchemical_book",
				"Arcane Alchemical Book");
		add("advancements.projecte.arcane_alchemical_book.description",
				"Obtain an arcane alchemical book.");
		add("advancements.projecte.arcane_transmutation_tablet",
				"Arcane Transmutation Tablet");
		add("advancements.projecte.arcane_transmutation_tablet.description",
				"Crafting included!");
		add("advancements.projecte.basic_alchemical_book",
				"Alchemical Teleportation");
		add("advancements.projecte.basic_alchemical_book.description",
				"Obtain a basic alchemical book.");
		add("advancements.projecte.blinded_by_the_light",
				"Blinded By The Light");
		add("advancements.projecte.blinded_by_the_light.description",
				"You were warned.");
		add("advancements.projecte.blue_fuel",
				"Blue Fuel");
		add("advancements.projecte.blue_fuel.description",
				"Obtain blue fuel.");
		add("advancements.projecte.blue_fuel_block",
				"Blue Fuel Block");
		add("advancements.projecte.blue_fuel_block.description",
				"Obtain a blue fuel block.");
		add("advancements.projecte.blue_matter",
				"Blue Matter");
		add("advancements.projecte.blue_matter.description",
				"Obtain blue matter.");
		add("advancements.projecte.blue_matter_block",
				"Blue Matter Block");
		add("advancements.projecte.blue_matter_block.description",
				"Obtain a blue matter block.");
		add("advancements.projecte.expansion_collector",
				"Even more sun power");
		add("advancements.projecte.expansion_collector.description",
				"Now the ridiculousness begins.");
		add("advancements.projecte.colossal_star_omega",
				"Why do you even need this much EMC?");
		add("advancements.projecte.colossal_star_omega.description",
				"This is getting ridiculous.");
		add("advancements.projecte.compact_sun",
				"The power of the sun in your hands");
		add("advancements.projecte.compact_sun.description",
				"Don't stare at it for too long..");
		add("advancements.projecte.compressed_collector",
				"Hey look, I took some collectors and made them useless!");
		add("advancements.projecte.compressed_collector.description",
				"Useless until they're crafted into something else.");
		add("advancements.projecte.condenser_mk3",
				"Condensed Condensers?");
		add("advancements.projecte.condenser_mk3.description",
				"Obtain a Mark 3 Energy Condenser.");
		add("advancements.projecte.cyan_fuel",
				"Cyan Fuel");
		add("advancements.projecte.cyan_fuel.description",
				"Obtain cyan fuel.");
		add("advancements.projecte.cyan_fuel_block",
				"Cyan Fuel Block");
		add("advancements.projecte.cyan_fuel_block.description",
				"Obtain a cyan fuel block.");
		add("advancements.projecte.cyan_matter",
				"Cyan Matter");
		add("advancements.projecte.cyan_matter.description",
				"Obtain cyan matter.");
		add("advancements.projecte.cyan_matter_block",
				"Cyan Matter Block");
		add("advancements.projecte.cyan_matter_block.description",
				"Obtain a cyan matter block.");
		add("advancements.projecte.emc_link",
				"Wait, you can do that?");
		add("advancements.projecte.emc_link.description",
				"Condensers be damned.");
		add("advancements.projecte.fading_fuel",
				"Fading Fuel");
		add("advancements.projecte.fading_fuel.description",
				"Obtain fading fuel.");
		add("advancements.projecte.fading_fuel_block",
				"Fading Fuel Block");
		add("advancements.projecte.fading_fuel_block.description",
				"Obtain a fading fuel block.");
		add("advancements.projecte.fading_matter",
				"Fading Matter");
		add("advancements.projecte.fading_matter.description",
				"Obtain fading matter.");
		add("advancements.projecte.fading_matter_block",
				"Fading Matter Block");
		add("advancements.projecte.fading_matter_block.description",
				"Obtain a fading matter block.");
		add("advancements.projecte.final_collector",
				"Final Collector");
		add("advancements.projecte.final_collector.description",
				"Do you even need this much EMC?");
		add("advancements.projecte.final_compressed_collector",
				"Final Compressed Collector");
		add("advancements.projecte.final_compressed_collector.description",
				"The universe just might implode.");
		add("advancements.projecte.final_emc_link",
				"Final EMC Link");
		add("advancements.projecte.final_emc_link.description",
				"Go emc some water for me.");
		add("advancements.projecte.final_power_flower",
				"Final Power Flower");
		add("advancements.projecte.final_power_flower.description",
				"Go touch some grass, please.");
		add("advancements.projecte.final_relay",
				"Final Relay");
		add("advancements.projecte.final_relay.description",
				"No really, do you need this much EMC?");
		add("advancements.projecte.final_star",
				"Final Star");
		add("advancements.projecte.final_star.description",
				"You're done. Go outside.");
		add("advancements.projecte.final_star_shard",
				"You've played this game too much");
		add("advancements.projecte.final_star_shard.description",
				"Seriously, go outside.");
		add("advancements.projecte.green_fuel",
				"Green Fuel");
		add("advancements.projecte.green_fuel.description",
				"Obtain green fuel.");
		add("advancements.projecte.green_fuel_block",
				"Green Fuel Block");
		add("advancements.projecte.green_fuel_block.description",
				"Obtain a green fuel block.");
		add("advancements.projecte.green_matter",
				"Green Matter");
		add("advancements.projecte.green_matter.description",
				"Obtain green matter.");
		add("advancements.projecte.green_matter_block",
				"Green Matter Block");
		add("advancements.projecte.green_matter_block.description",
				"Obtain a green matter block.");
		add("advancements.projecte.infinite_fuel",
				"Is it a star?");
		add("advancements.projecte.infinite_fuel.description",
				"Is it a cross? Who cares, chuck it in a furnace and watch it work.");
		add("advancements.projecte.infinite_steak",
				"Unlimited Steak!!1!11!!!");
		add("advancements.projecte.infinite_steak.description",
				"Bread? That's for peasants.");
		add("advancements.projecte.knowledge_sharing_book",
				"Sharing is caring!");
		add("advancements.projecte.knowledge_sharing_book.description",
				"But I don't care.");
		add("advancements.projecte.lime_fuel",
				"Lime Fuel");
		add("advancements.projecte.lime_fuel.description",
				"Obtain lime fuel.");
		add("advancements.projecte.lime_fuel_block",
				"Lime Fuel Block");
		add("advancements.projecte.lime_fuel_block.description",
				"Obtain a lime fuel block.");
		add("advancements.projecte.lime_matter",
				"Lime Matter");
		add("advancements.projecte.lime_matter.description",
				"Obtain lime matter.");
		add("advancements.projecte.lime_matter_block",
				"Lime Matter Block");
		add("advancements.projecte.lime_matter_block.description",
				"Obtain a lime matter block.");
		add("advancements.projecte.magenta_fuel",
				"Magenta Fuel");
		add("advancements.projecte.magenta_fuel.description",
				"Obtain magenta fuel.");
		add("advancements.projecte.magenta_fuel_block",
				"Magenta Fuel Block");
		add("advancements.projecte.magenta_fuel_block.description",
				"Obtain a magenta fuel block.");
		add("advancements.projecte.magenta_matter",
				"Magenta Matter");
		add("advancements.projecte.magenta_matter.description",
				"Obtain magenta matter.");
		add("advancements.projecte.magenta_matter_block",
				"Magenta Matter Block");
		add("advancements.projecte.magenta_matter_block.description",
				"Obtain a magenta matter block.");
		add("advancements.projecte.magnum_star_omega",
				"Even bigger EMC batteries??");
		add("advancements.projecte.magnum_star_omega.description",
				"No one person should have this much power.");
		add("advancements.projecte.master_alchemical_book",
				"Master Alchemical Book");
		add("advancements.projecte.master_alchemical_book.description",
				"Obtain a master alchemical book.");
		add("advancements.projecte.orange_fuel",
				"Orange Fuel");
		add("advancements.projecte.orange_fuel.description",
				"Obtain orange fuel.");
		add("advancements.projecte.orange_fuel_block",
				"Orange Fuel Block");
		add("advancements.projecte.orange_fuel_block.description",
				"Obtain a orange fuel block.");
		add("advancements.projecte.orange_matter",
				"Orange Matter");
		add("advancements.projecte.orange_matter.description",
				"Obtain orange matter.");
		add("advancements.projecte.orange_matter_block",
				"Orange Matter Block");
		add("advancements.projecte.orange_matter_block.description",
				"Obtain an orange matter block.");
		add("advancements.projecte.pink_fuel",
				"Pink Fuel");
		add("advancements.projecte.pink_fuel.description",
				"Obtain pink fuel.");
		add("advancements.projecte.pink_fuel_block",
				"Pink Fuel Block");
		add("advancements.projecte.pink_fuel_block.description",
				"Obtain a pink fuel block.");
		add("advancements.projecte.pink_matter",
				"Pink Matter");
		add("advancements.projecte.pink_matter.description",
				"Obtain pink matter.");
		add("advancements.projecte.pink_matter_block",
				"Pink Matter Block");
		add("advancements.projecte.pink_matter_block.description",
				"Obtain a pink matter block.");
		add("advancements.projecte.power_flower",
				"The REAL Power Flower");
		add("advancements.projecte.power_flower.description",
				"Now you're cooking with gas.");
		add("advancements.projecte.purple_fuel",
				"Purple Fuel");
		add("advancements.projecte.purple_fuel.description",
				"Obtain purple fuel.");
		add("advancements.projecte.purple_fuel_block",
				"Purple Fuel Block");
		add("advancements.projecte.purple_fuel_block.description",
				"Obtain a purple fuel block.");
		add("advancements.projecte.purple_matter",
				"Purple Matter");
		add("advancements.projecte.purple_matter.description",
				"Obtain purple matter.");
		add("advancements.projecte.purple_matter_block",
				"Purple Matter Block");
		add("advancements.projecte.purple_matter_block.description",
				"Obtain a purple matter block.");
		add("advancements.projecte.expansion_relay",
				"The first step to the REAL power flower.");
		add("advancements.projecte.expansion_relay.description",
				"It's a long way to the top.");
		add("advancements.projecte.transmutation_interface",
				"Transmutation in your computers??");
		add("advancements.projecte.transmutation_interface.description",
				"They just got even better.");
		add("advancements.projecte.violet_fuel",
				"Violet Fuel");
		add("advancements.projecte.violet_fuel.description",
				"Obtain violet fuel.");
		add("advancements.projecte.violet_fuel_block",
				"Violet Fuel Block");
		add("advancements.projecte.violet_fuel_block.description",
				"Obtain a violet fuel block.");
		add("advancements.projecte.violet_matter",
				"Violet Matter");
		add("advancements.projecte.violet_matter.description",
				"Obtain violet matter.");
		add("advancements.projecte.violet_matter_block",
				"Violet Matter Block");
		add("advancements.projecte.violet_matter_block.description",
				"Obtain a violet matter block.");
		add("advancements.projecte.white_fuel",
				"White Fuel");
		add("advancements.projecte.white_fuel.description",
				"Obtain white fuel.");
		add("advancements.projecte.white_fuel_block",
				"White Fuel Block");
		add("advancements.projecte.white_fuel_block.description",
				"Obtain a white fuel block.");
		add("advancements.projecte.white_matter",
				"White Matter");
		add("advancements.projecte.white_matter.description",
				"Obtain white matter.");
		add("advancements.projecte.white_matter_block",
				"White Matter Block");
		add("advancements.projecte.white_matter_block.description",
				"Obtain a white matter block.");
		add("advancements.projecte.yellow_fuel",
				"Yellow Fuel");
		add("advancements.projecte.yellow_fuel.description",
				"Obtain yellow fuel.");
		add("advancements.projecte.yellow_fuel_block",
				"Yellow Fuel Block");
		add("advancements.projecte.yellow_fuel_block.description",
				"Obtain a yellow fuel block.");
		add("advancements.projecte.yellow_matter",
				"Yellow Matter");
		add("advancements.projecte.yellow_matter.description",
				"Obtain yellow matter.");
		add("advancements.projecte.yellow_matter_block",
				"Yellow Matter Block");
		add("advancements.projecte.yellow_matter_block.description",
				"Obtain a yellow matter block.");
		add("attribute.projecte.sun_exposure_protection",
				"Sun Exposure Protection");
		addBlock("block.projecte.advanced_alchemical_chest.color",
				"Current Color: %s");
		addBlock("block.projecte.advanced_alchemical_chest.color_set",
				"The color has been set to %s.");
		addBlock("block.projecte.advanced_alchemical_chest.invalid_item",
				"You must be holding a %s to set the color.");
		addBlock("block.projecte.advanced_alchemical_chest.tooltip",
				"Just like the alchemical bag, but in a chest form. Shift click with a bag to set the color.");
		addBlock("block.projecte.arcane_table",
				"Arcane Table");
		addBlock("block.projecte.basic_collector",
				"Basic Collector [MK 1]");
		addBlock("block.projecte.basic_emc_link",
				"Basic EMC Link [MK 1]");
		addBlock("block.projecte.basic_power_flower",
				"Basic Power Flower [MK 1]");
		addBlock("block.projecte.basic_relay",
				"Basic Relay [MK 1]");
		addBlock("block.projecte.black_advanced_alchemical_chest",
				"Black Advanced Alchemical Chest");
		addBlock("block.projecte.blue_advanced_alchemical_chest",
				"Blue Advanced Alchemical Chest");
		addBlock("block.projecte.blue_collector",
				"Blue Collector [MK 8]");
		addBlock("block.projecte.blue_emc_link",
				"Blue EMC Link [MK 8]");
		addBlock("block.projecte.blue_fuel_block",
				"Blue Fuel Block");
		addBlock("block.projecte.blue_matter_block",
				"Blue Matter Block");
		addBlock("block.projecte.blue_power_flower",
				"Blue Power Flower [MK 8]");
		addBlock("block.projecte.blue_relay",
				"Blue Relay [MK 8]");
		addBlock("block.projecte.brown_advanced_alchemical_chest",
				"Brown Advanced Alchemical Chest");
		addBlock("block.projecte.collector",
				"Collector.");
		addBlock("block.projecte.collector.emc",
				"Maximum Produced EMC: %s/s");
		addBlock("block.projecte.collector.max_storage",
				"Maximum Stored EMC: %s");
		addBlock("block.projecte.collector.stack_emc",
				"Stack Produced EMC: %s/s");
		addBlock("block.projecte.collector.tooltip",
				"Generates EMC when exposed to light.");
		addBlock("block.projecte.compact_sun",
				"Compact Sun");
		addBlock("block.projecte.compact_sun.tooltip",
				"The power of the sun in a single block.");
		addBlock("block.projecte.compact_sun.tooltip2",
				"When put above or below certain blocks, multiplies their output by %s.");
		addBlock("block.projecte.condenser_mk3",
				"Energy Condenser MK3");
		addBlock("block.projecte.condenser_mk3.tooltip",
				"5 Condensers in one, each side (except down) is a separate condenser with its own input, processing, and lock slot. The down side is the output for all other sides.");
		addBlock("block.projecte.cyan_advanced_alchemical_chest",
				"Cyan Advanced Alchemical Chest");
		addBlock("block.projecte.cyan_collector",
				"Cyan Collector [MK 9]");
		addBlock("block.projecte.cyan_emc_link",
				"Cyan EMC Link [MK 9]");
		addBlock("block.projecte.cyan_fuel_block",
				"Cyan Fuel Block");
		addBlock("block.projecte.cyan_matter_block",
				"Cyan Matter Block");
		addBlock("block.projecte.cyan_power_flower",
				"Cyan Power Flower [MK 9]");
		addBlock("block.projecte.cyan_relay",
				"Cyan Relay [MK 9]");
		addBlock("block.projecte.dark_collector",
				"Dark Collector [MK 2]");
		addBlock("block.projecte.dark_emc_link",
				"Dark EMC Link [MK 2]");
		addBlock("block.projecte.dark_power_flower",
				"Dark Power Flower [MK 2]");
		addBlock("block.projecte.dark_relay",
				"Dark Relay [MK 2]");
		addBlock("block.projecte.emc_link.already_set",
				"An export item has already been set, clear the current export first.");
		addBlock("block.projecte.emc_link.cleared",
				"Export item has been cleared.");
		addBlock("block.projecte.emc_link.empty_hand",
				"Empty your hand to retrieve items.");
		addBlock("block.projecte.emc_link.fluid_export_efficiency",
				"Fluid Export Efficiency: %s");
		addBlock("block.projecte.emc_link.limit_emc",
				"EMC Limit: %s/s");
		addBlock("block.projecte.emc_link.limit_fluids",
				"Fluid Export Limit: %s mB/s");
		addBlock("block.projecte.emc_link.limit_items",
				"Item Import/Export Limit: %s/s");
		addBlock("block.projecte.emc_link.no_emc_value",
				"%s does not have an emc value.");
		addBlock("block.projecte.emc_link.no_export_remaining",
				"Export limit has been reached, please wait a second.");
		addBlock("block.projecte.emc_link.not_enough_emc",
				"You do not have enough emc to purchase this, you need %s.");
		addBlock("block.projecte.emc_link.not_set",
				"An export item has not been set.");
		addBlock("block.projecte.emc_link.set",
				"Export item has been set to %s.");
		addBlock("block.projecte.emc_link.tooltip",
				"Can be used to import emc & items, and export items & fluids.");
		addBlock("block.projecte.fading_collector",
				"Fading Collector [MK 15]");
		addBlock("block.projecte.fading_emc_link",
				"Fading EMC Link [MK 15]");
		addBlock("block.projecte.fading_matter_block",
				"Fading Matter Block");
		addBlock("block.projecte.fading_power_flower",
				"Fading Power Flower [MK 15]");
		addBlock("block.projecte.fading_relay",
				"Fading Relay [MK 15]");
		addBlock("block.projecte.final_collector",
				"Final Collector [MK 16]");
		addBlock("block.projecte.final_emc_link",
				"Final EMC Link [MK 16]");
		addBlock("block.projecte.final_power_flower",
				"Final Power Flower [MK 16]");
		addBlock("block.projecte.final_relay",
				"Final Relay [MK 16]");
		addBlock("block.projecte.gray_advanced_alchemical_chest",
				"Gray Advanced Alchemical Chest");
		addBlock("block.projecte.green_advanced_alchemical_chest",
				"Green Advanced Alchemical Chest");
		addBlock("block.projecte.green_collector",
				"Green Collector [MK 10]");
		addBlock("block.projecte.green_emc_link",
				"Green EMC Link [MK 10]");
		addBlock("block.projecte.green_fuel_block",
				"Green Fuel Block");
		addBlock("block.projecte.green_matter_block",
				"Green Matter Block");
		addBlock("block.projecte.green_power_flower",
				"Green Power Flower [MK 10]");
		addBlock("block.projecte.green_relay",
				"Green Relay [MK 10]");
		addBlock("block.projecte.light_blue_advanced_alchemical_chest",
				"Light Blue Advanced Alchemical Chest");
		addBlock("block.projecte.light_gray_advanced_alchemical_chest",
				"Light Gray Advanced Alchemical Chest");
		addBlock("block.projecte.lime_advanced_alchemical_chest",
				"Lime Advanced Alchemical Chest");
		addBlock("block.projecte.lime_collector",
				"Lime Collector [MK 11]");
		addBlock("block.projecte.lime_emc_link",
				"Lime EMC Link [MK 11]");
		addBlock("block.projecte.lime_fuel_block",
				"Lime Fuel Block");
		addBlock("block.projecte.lime_matter_block",
				"Lime Matter Block");
		addBlock("block.projecte.lime_power_flower",
				"Lime Power Flower [MK 11]");
		addBlock("block.projecte.lime_relay",
				"Lime Relay [MK 11]");
		addBlock("block.projecte.magenta_advanced_alchemical_chest",
				"Magenta Advanced Alchemical Chest");
		addBlock("block.projecte.magenta_collector",
				"Magenta Collector [MK 4]");
		addBlock("block.projecte.magenta_emc_link",
				"Magenta EMC Link [MK 4]");
		addBlock("block.projecte.magenta_fuel_block",
				"Magenta Fuel Block");
		addBlock("block.projecte.magenta_matter_block",
				"Magenta Matter Block");
		addBlock("block.projecte.magenta_power_flower",
				"Magenta Power Flower [MK 4]");
		addBlock("block.projecte.magenta_relay",
				"Magenta Relay [MK 4]");
		addBlock("block.projecte.orange_advanced_alchemical_chest",
				"Orange Advanced Alchemical Chest");
		addBlock("block.projecte.orange_collector",
				"Orange Collector [MK 13]");
		addBlock("block.projecte.orange_emc_link",
				"Orange EMC Link [MK 13]");
		addBlock("block.projecte.orange_fuel_block",
				"Orange Fuel Block");
		addBlock("block.projecte.orange_matter_block",
				"Orange Matter Block");
		addBlock("block.projecte.orange_power_flower",
				"Orange Power Flower [MK 13]");
		addBlock("block.projecte.orange_relay",
				"Orange Relay [MK 13]");
		addBlock("block.projecte.pink_advanced_alchemical_chest",
				"Pink Advanced Alchemical Chest");
		addBlock("block.projecte.pink_collector",
				"Pink Collector [MK 5]");
		addBlock("block.projecte.pink_emc_link",
				"Pink EMC Link [MK 5]");
		addBlock("block.projecte.pink_fuel_block",
				"Pink Fuel Block");
		addBlock("block.projecte.pink_matter_block",
				"Pink Matter Block");
		addBlock("block.projecte.pink_power_flower",
				"Pink Power Flower [MK 5]");
		addBlock("block.projecte.pink_relay",
				"Pink Relay [MK 5]");
		addBlock("block.projecte.power_flower.emc",
				"Produced EMC: %s/s");
		addBlock("block.projecte.power_flower.stack_emc",
				"Stack Produced EMC: %s/s");
		addBlock("block.projecte.power_flower.tooltip",
				"Generates EMC once per %s tick%s. (20/second)");
		addBlock("block.projecte.purple_advanced_alchemical_chest",
				"Purple Advanced Alchemical Chest");
		addBlock("block.projecte.purple_collector",
				"Purple Collector [MK 6]");
		addBlock("block.projecte.purple_emc_link",
				"Purple EMC Link [MK 6]");
		addBlock("block.projecte.purple_fuel_block",
				"Purple Fuel Block");
		addBlock("block.projecte.purple_matter_block",
				"Purple Matter Block");
		addBlock("block.projecte.purple_power_flower",
				"Purple Power Flower [MK 6]");
		addBlock("block.projecte.purple_relay",
				"Purple Relay [MK 6]");
		addBlock("block.projecte.red_advanced_alchemical_chest",
				"Red Advanced Alchemical Chest");
		addBlock("block.projecte.red_collector",
				"Red Collector [MK 3]");
		addBlock("block.projecte.red_emc_link",
				"Red EMC Link [MK 3]");
		addBlock("block.projecte.red_power_flower",
				"Red Power Flower [MK 3]");
		addBlock("block.projecte.red_relay",
				"Red Relay [MK 3]");
		addBlock("block.projecte.relay.bonus",
				"Relay Bonus: %s/s");
		addBlock("block.projecte.relay.tooltip",
				"Transfers EMC once per second.");
		addBlock("block.projecte.relay.transfer",
				"Max EMC Transfer: %s/s");
		addBlock("block.projecte.transmutation_interface",
				"Transmutation Interface");
		addBlock("block.projecte.transmutation_interface.tooltip",
				"This can be used with compatible mods like Applied Energistics to access your transmutation inventory.");
		addBlock("block.projecte.violet_collector",
				"Violet Collector [MK 7]");
		addBlock("block.projecte.violet_emc_link",
				"Violet EMC Link [MK 7]");
		addBlock("block.projecte.violet_fuel_block",
				"Violet Fuel Block");
		addBlock("block.projecte.violet_matter_block",
				"Violet Matter Block");
		addBlock("block.projecte.violet_power_flower",
				"Violet Power Flower [MK 7]");
		addBlock("block.projecte.violet_relay",
				"Violet Relay [MK 7]");
		addBlock("block.projecte.white_advanced_alchemical_chest",
				"White Advanced Alchemical Chest");
		addBlock("block.projecte.white_collector",
				"White Collector [MK 14]");
		addBlock("block.projecte.white_emc_link",
				"White EMC Link [MK 14]");
		addBlock("block.projecte.white_fuel_block",
				"White Fuel Block");
		addBlock("block.projecte.white_matter_block",
				"White Matter Block");
		addBlock("block.projecte.white_power_flower",
				"White Power Flower [MK 14]");
		addBlock("block.projecte.white_relay",
				"White Relay [MK 14]");
		addBlock("block.projecte.yellow_advanced_alchemical_chest",
				"Yellow Advanced Alchemical Chest");
		addBlock("block.projecte.yellow_collector",
				"Yellow Collector [MK 12]");
		addBlock("block.projecte.yellow_emc_link",
				"Yellow EMC Link [MK 12]");
		addBlock("block.projecte.yellow_fuel_block",
				"Yellow Fuel Block");
		addBlock("block.projecte.yellow_matter_block",
				"Yellow Matter Block");
		addBlock("block.projecte.yellow_power_flower",
				"Yellow Power Flower [MK 12]");
		addBlock("block.projecte.yellow_relay",
				"Yellow Relay [MK 12]");
		add("command.projecte.book.add.duplicate_name",
				"A location with that name already exists.");
		add("command.projecte.book.add.invalid_name",
				"That name is invalid.");
		add("command.projecte.book.add.itemstack_success",
				"That location has been successfully added to that book.");
		add("command.projecte.book.add.player_notification",
				"The location \"%s\" has been added to your teleport locations by %s.");
		add("command.projecte.book.add.player_success",
				"That location has been successfully added to %s's teleport locations.");
		add("command.projecte.book.add.player_success_self",
				"That location has been successfully added to your teleport locations.");
		add("command.projecte.book.bound_to_player.add",
				"This book is bound to %s. To add to those locations, use %s.");
		add("command.projecte.book.bound_to_player.clear",
				"This book is bound to %s. To clear those locations, use %s.");
		add("command.projecte.book.bound_to_player.dump",
				"This book is bound to %s. To dump those locations, use %s.");
		add("command.projecte.book.bound_to_player.list",
				"This book is bound to %s. To list those locations, use %s.");
		add("command.projecte.book.bound_to_player.remove",
				"This book is bound to %s. To remove from those locations, use %s.");
		add("command.projecte.book.clear.itemstack_success",
				"The locations in that book have been cleared.");
		add("command.projecte.book.clear.player_notification",
				"Your teleportation locations were cleared by %s.");
		add("command.projecte.book.clear.player_success",
				"%s's locations have been cleared.");
		add("command.projecte.book.clear.player_success_self",
				"Your locations have been cleared.");
		add("command.projecte.book.click_to_copy",
				"Click to copy. This can be used to provide to developers for debugging.");
		add("command.projecte.book.empty",
				"No locations were found.");
		add("command.projecte.book.failed_to_get_capability",
				"Failed to get capability.");
		add("command.projecte.book.invalid_hand_item",
				"You are not holding an alchemical book.");
		add("command.projecte.book.list.location",
				"Name: %s Pos: %s Dimension: %s");
		add("command.projecte.book.reindex.itemstack_success",
				"That book's teleport locations have successfully been reindexed.");
		add("command.projecte.book.reindex.player_notifcation",
				"Your teleport locations were reindexed by %s.");
		add("command.projecte.book.reindex.player_success",
				"%s's teleport locations have successfully been reindexed.");
		add("command.projecte.book.reindex.player_success_self",
				"Your teleport locations have successfully been reindexed..");
		add("command.projecte.book.remove.backup",
				"Location Dump: %s");
		add("command.projecte.book.remove.backup_info",
				"A dump of the location, in case you accidentally removed it.");
		add("command.projecte.book.remove.internal_location",
				"That location is an internal location, and cannot be removed.");
		add("command.projecte.book.remove.invalid_location",
				"No location was found by that name.");
		add("command.projecte.book.remove.itemstack_success",
				"That location has been successfully removed from that book.");
		add("command.projecte.book.remove.player_notifcation",
				"The location \"%s\" has been removed from your teleport locations by %s.");
		add("command.projecte.book.remove.player_success",
				"That location has been successfully removed from %s's teleport locations.");
		add("command.projecte.book.remove.player_success_self",
				"That location has been successfully removed from your teleport locations.");
		add("command.projecte.console",
				"Console");
		add("command.projecte.dumpfuelmap.emc",
				"EMC");
		add("command.projecte.dumpfuelmap.fuel",
				"Fuel");
		add("command.projecte.dumpfuelmap.index",
				"Index");
		add("command.projecte.emc.add.notification",
				"You were given %s emc by %s, you now have %s");
		add("command.projecte.emc.add.success_self",
				"Successfully added %s to your emc, you now have %s");
		add("command.projecte.emc.get.success_self",
				"You have %s emc");
		add("command.projecte.emc.remove.negative",
				"Cannot remove %s emc from %s as this would make their emc negative.");
		add("command.projecte.emc.remove.notification",
				"%s emc was removed from you by %s, you now have %s");
		add("command.projecte.emc.remove.success_self",
				"Successfully removed %s from your emc, you now have %s");
		add("command.projecte.emc.set.notification",
				"Your emc was set to %s by %s");
		add("command.projecte.emc.set.success_self",
				"Successfully set your emc to %s");
		add("command.projecte.emc.test.fail_self",
				"You do not have enough emc to remove %s.");
		add("command.projecte.emc.test.success_self",
				"You do have enough emc to remove %s.");
		add("command.projecte.knowledge.clear.fail_self",
				"You do not have any knowledge to clear.");
		add("command.projecte.knowledge.clear.notification",
				"Your knowledge was cleared by %s");
		add("command.projecte.knowledge.clear.success_self",
				"Successfully cleared your knowledge");
		add("command.projecte.knowledge.learn.failSelf",
				"You already have knowledge of %s.");
		add("command.projecte.knowledge.learn.notification",
				"You gained knowledge of %s from %s.");
		add("command.projecte.knowledge.learn.success_self",
				"You have successfully learned %s.");
		add("command.projecte.knowledge.test.fail_self",
				"You do not have knowledge of %s.");
		add("command.projecte.knowledge.test.success_self",
				"You do have knowledge of %s.");
		add("command.projecte.knowledge.unlearn.fail_self",
				"You do not have knowledge of %s.");
		add("command.projecte.knowledge.unlearn.notification",
				"You lost knowledge of %s from %s.");
		add("command.projecte.knowledge.unlearn.success_self",
				"You have successfully unlearned %s.");
		add("command.projecte.player_only",
				"This command must be executed by a player.");
		add("command.projecte.reload_emc.success",
				"EMC successfully reloaded %s emc values in %s. Any open GUIs may need to be reopened to apply changes.");
		add("command.projecte.reload_emc.warning",
				"This reload has not been tested extensively, but it seems to work. If things seem unstable, fully restart the world.");
		add("command.projecte.reloading_emc",
				"Reloading EMC!");
		add("command.projecte.set_owner.failure",
				"Invalid block.");
		add("command.projecte.set_owner.success",
				"Successfully set the owner of the block to %s.");
		add("configuration.projecte.edit_others_alchemical_books.disabled",
				"Disabled");
		add("configuration.projecte.edit_others_alchemical_books.enabled",
				"Enabled");
		add("configuration.projecte.edit_others_alchemical_books.op_only",
				"OP Only");
		add("configuration.projecte.emc_display_position.bottom_left",
				"Bottom Left");
		add("configuration.projecte.emc_display_position.bottom_right",
				"Bottom Right");
		add("configuration.projecte.emc_display_position.top_left",
				"Top Left");
		add("configuration.projecte.emc_display_position.top_right",
				"Top Right");
		add("configuration.projecte.search_type.normal",
				"Normal");
		add("configuration.projecte.search_type.normal_autofocus",
				"Normal (Auto Focus)");
		add("configuration.projecte.search_type.sync",
				"Sync");
		add("configuration.projecte.search_type.sync_autofocus",
				"Sync (Auto Focus)");
		add("death.attack.stare_at_sun",
				"%s decided staring at the sun was a good idea");
		add("death.attack.stare_at_sun.player",
				"%s decided to stare at the sun whilst fighting %s");
		add("death.attack.walk_on_sun",
				"%s tried to walk on the sun");
		add("death.attack.walk_on_sun.player",
				"%s tried to walk on the sun whilst fighting %s");
		add("enchantment.projecte.alchemical_collection",
				"Alchemical Collection");
		add("enchantment.projecte.alchemical_collection.desc",
				"Converts any mined items with an emc value directly into emc.");
		add("gui.projecte.advanced_alchemical_chest.title",
				"Advanced Alchemical Chest");
		add("gui.projecte.alchemical_book",
				"Alchemical Chest");
		add("gui.projecte.alchemical_book.back",
				"Back");
		add("gui.projecte.alchemical_book.close",
				"Close");
		add("gui.projecte.alchemical_book.cost",
				"Cost: %s");
		add("gui.projecte.alchemical_book.create",
				"Create");
		add("gui.projecte.alchemical_book.delete",
				"Delete");
		add("gui.projecte.alchemical_book.dimension",
				"Dimension: %s");
		add("gui.projecte.alchemical_book.distance",
				"Distance: %s");
		add("gui.projecte.alchemical_book.no_back_location",
				"No Back Location");
		add("gui.projecte.arcane_transmutation_tablet",
				"Arcane Transmutation Tablet");
		add("gui.projecte.arcane_transmutation_tablet.balance",
				"Balance");
		add("gui.projecte.arcane_transmutation_tablet.clear",
				"Clear");
		add("gui.projecte.arcane_transmutation_tablet.clear.force",
				"Force!");
		add("gui.projecte.arcane_transmutation_tablet.consume",
				"Consume");
		add("gui.projecte.arcane_transmutation_tablet.learned",
				"Learned");
		add("gui.projecte.arcane_transmutation_tablet.lock",
				"Lock");
		add("gui.projecte.arcane_transmutation_tablet.rotate",
				"Rotate");
		add("gui.projecte.arcane_transmutation_tablet.rotate.clockwise",
				"Clockwise");
		add("gui.projecte.arcane_transmutation_tablet.rotate.counter_clockwise",
				"Counter Clockwise");
		add("gui.projecte.arcane_transmutation_tablet.search_type",
				"Search Type");
		add("gui.projecte.arcane_transmutation_tablet.search_type.autoselected",
				"Autoselected");
		add("gui.projecte.arcane_transmutation_tablet.search_type.autoselected_sync",
				"Autoselected Sync");
		add("gui.projecte.arcane_transmutation_tablet.search_type.normal",
				"Normal");
		add("gui.projecte.arcane_transmutation_tablet.search_type.sync",
				"Sync");
		add("gui.projecte.arcane_transmutation_tablet.spread",
				"Spread");
		add("gui.projecte.arcane_transmutation_tablet.unlearn",
				"Unlearn");
		add("gui.projecte.arcane_transmutation_tablet.unlearned",
				"Unlearned");
		add("gui.projecte.next",
				"Next");
		add("gui.projecte.previous",
				"Previous");
		add("item.projecte.advanced_alchemical_book",
				"Advanced Alchemical Book");
		add("item.projecte.alchemical_book",
				"Alchemical Book");
		add("item.projecte.alchemical_book.bound_to",
				"Bound to %s.");
		add("item.projecte.alchemical_book.corrupted",
				"The locations in this book seem to be corrupted. Check the logs for more information.");
		add("item.projecte.alchemical_book.create_failed",
				"Failed to create new destination: %s");
		add("item.projecte.alchemical_book.delete_failed",
				"Failed to delete destination: %s");
		add("item.projecte.alchemical_book.error.dimension_not_found",
				"Dimension \"%s\" was not found.");
		add("item.projecte.alchemical_book.error.duplicate_name",
				"Name \"%s\" is already in use.");
		add("item.projecte.alchemical_book.error.edit_not_allowed",
				"You do not have permmision to edit that book.");
		add("item.projecte.alchemical_book.error.name_not_found",
				"Destination with name \"%s\" was not found.");
		add("item.projecte.alchemical_book.error.no_back_location",
				"You do not have a back location to go to.");
		add("item.projecte.alchemical_book.error.not_enough_emc",
				"You do not have enough emc to use that location.");
		add("item.projecte.alchemical_book.error.owner_offline",
				"The owner of that book (%s) is not online.");
		add("item.projecte.alchemical_book.error.wrong_dimension",
				"Cannot teleport between dimensions.");
		add("item.projecte.alchemical_book.no_longer_bound",
				"This book is no longer bound to you. It now has its own set of teleportation locations. If you lose the book, its locations will also be lost.");
		add("item.projecte.alchemical_book.now_bound",
				"This book is now bound to you. Its teleportation locations will be stored with your player, and not the book.");
		add("item.projecte.alchemical_book.owner_not_online",
				"The owner of this book is not online.");
		add("item.projecte.alchemical_book.teleport_failed",
				"Failed to teleport to destination: %s");
		add("item.projecte.alchemical_book.tooltip",
				"Teleport anywhere, simply using emc.");
		add("item.projecte.alchemical_book.tooltip_across_dimensions",
				"Can teleport across dimensions.");
		add("item.projecte.alchemical_book.tooltip_advanced",
				"50%% Cheaper Teleportation (%s/Block)");
		add("item.projecte.alchemical_book.tooltip_arcane",
				"Free Teleportation (%s/Block)");
		add("item.projecte.alchemical_book.tooltip_basic",
				"Teleportation (%s/Block)");
		add("item.projecte.alchemical_book.tooltip_bind",
				"Can be bound to a player by shifting while using.");
		add("item.projecte.alchemical_book.tooltip_master",
				"80%% Cheaper Teleportation (%s/Block)");
		add("item.projecte.arcane_alchemical_book",
				"Arcane Alchemical Book");
		add("item.projecte.arcane_tablet",
				"Arcane Tablet");
		add("item.projecte.arcane_transmutation_tablet",
				"Arcane Transmutation Tablet");
		add("item.projecte.arcane_transmutation_tablet.tooltip",
				"An upgraded transmutation tablet with a built in crafting table.");
		add("item.projecte.basic_alchemical_book",
				"Basic Alchemical Book");
		add("item.projecte.basic_compressed_collector",
				"Basic Compressed Collector [MK 1]");
		add("item.projecte.blue_compressed_collector",
				"Blue Compressed Collector [MK 8]");
		add("item.projecte.blue_fuel",
				"Blue Fuel");
		add("item.projecte.blue_matter",
				"Blue Matter");
		add("item.projecte.colossal_star_drei",
				"Colossal Star Drei");
		add("item.projecte.colossal_star_ein",
				"Colossal Star Ein");
		add("item.projecte.colossal_star_omega",
				"Colossal Star Omega");
		add("item.projecte.colossal_star_sphere",
				"Colossal Star Sphere");
		add("item.projecte.colossal_star_vier",
				"Colossal Star Vier");
		add("item.projecte.colossal_star_zwei",
				"Colossal Star Zwei");
		add("item.projecte.compressed_collector.tooltip",
				"Crafting Ingredient");
		add("item.projecte.cyan_compressed_collector",
				"Cyan Compressed Collector [MK 9]");
		add("item.projecte.cyan_fuel",
				"Cyan Fuel");
		add("item.projecte.cyan_matter",
				"Cyan Matter");
		add("item.projecte.dark_compressed_collector",
				"Dark Compressed Collector [MK 2]");
		add("item.projecte.fading_compressed_collector",
				"Fading Compressed Collector [MK 15]");
		add("item.projecte.fading_matter",
				"Fading Matter");
		add("item.projecte.final_compressed_collector",
				"Final Compressed Collector [MK 16]");
		add("item.projecte.final_star",
				"Final Star");
		add("item.projecte.final_star.tooltip",
				"Currently exists only to show off, future usages are planned.");
		add("item.projecte.final_star_shard",
				"Final Star Shard");
		add("item.projecte.final_star_shard.tooltip",
				"Crafting ingredient.");
		add("item.projecte.gargantuan_star_drei",
				"Gargantuan Star Drei");
		add("item.projecte.gargantuan_star_ein",
				"Gargantuan Star Ein");
		add("item.projecte.gargantuan_star_omega",
				"Gargantuan Star Omega");
		add("item.projecte.gargantuan_star_sphere",
				"Gargantuan Star Sphere");
		add("item.projecte.gargantuan_star_vier",
				"Gargantuan Star Vier");
		add("item.projecte.gargantuan_star_zwei",
				"Gargantuan Star Zwei");
		add("item.projecte.green_compressed_collector",
				"Green Compressed Collector [MK 10]");
		add("item.projecte.green_fuel",
				"Green Fuel");
		add("item.projecte.green_matter",
				"Green Matter");
		add("item.projecte.infinite_fuel",
				"Infinite Fuel");
		add("item.projecte.infinite_fuel.not_enough_emc",
				"You do not have enough emc to use this, you need %s.");
		add("item.projecte.infinite_fuel.tooltip",
				"This item will not be consumed, fuel will be provided via your emc.");
		add("item.projecte.infinite_steak",
				"Infinite Steak");
		add("item.projecte.infinite_steak.not_enough_emc",
				"You do not have enough emc to use this, you need %s.");
		add("item.projecte.infinite_steak.tooltip",
				"Infinite food via your emc.");
		add("item.projecte.knowledge_sharing_book",
				"Knowledge Sharing Book");
		add("item.projecte.knowledge_sharing_book.learned",
				"You learned %s.");
		add("item.projecte.knowledge_sharing_book.learned_over_100",
				"%s item(s) were learned, but not shown.");
		add("item.projecte.knowledge_sharing_book.learned_total",
				"You have learned %s item(s) from %s.");
		add("item.projecte.knowledge_sharing_book.no_new_knowledge",
				"You learned nothing new.");
		add("item.projecte.knowledge_sharing_book.no_owner",
				"You cannot gain knowledge from an unowned book.");
		add("item.projecte.knowledge_sharing_book.selected",
				"Selected Player: %s");
		add("item.projecte.knowledge_sharing_book.self",
				"You cannot gain your own knowledge.");
		add("item.projecte.knowledge_sharing_book.stored",
				"Knowledge stored.");
		add("item.projecte.lime_compressed_collector",
				"Lime Compressed Collector [MK 11]");
		add("item.projecte.lime_fuel",
				"Lime Fuel");
		add("item.projecte.lime_matter",
				"Lime Matter");
		add("item.projecte.magenta_compressed_collector",
				"Magenta Compressed Collector [MK 4]");
		add("item.projecte.magenta_fuel",
				"Magenta Fuel");
		add("item.projecte.magenta_matter",
				"Magenta Matter");
		add("item.projecte.magnum_star_drei",
				"Magnum Star Drei");
		add("item.projecte.magnum_star_ein",
				"Magnum Star Ein");
		add("item.projecte.magnum_star_omega",
				"Magnum Star Omega");
		add("item.projecte.magnum_star_sphere",
				"Magnum Star Sphere");
		add("item.projecte.magnum_star_vier",
				"Magnum Star Vier");
		add("item.projecte.magnum_star_zwei",
				"Magnum Star Zwei");
		add("item.projecte.master_alchemical_book",
				"Master Alchemical Book");
		add("item.projecte.matter_upgrader",
				"Matter Upgrader");
		add("item.projecte.matter_upgrader.done",
				"Upgrade done, %s emc has been used.");
		add("item.projecte.matter_upgrader.done_creative",
				"Upgrade done, no emc has been used.");
		add("item.projecte.matter_upgrader.max_upgrade",
				"This block has been maxed out.");
		add("item.projecte.matter_upgrader.not_enough_emc",
				"You do not have enough emc, you need %s.");
		add("item.projecte.matter_upgrader.not_learned",
				"You must learn \"%s\" before upgrading to it.");
		add("item.projecte.matter_upgrader.not_owner",
				"You do not own this.");
		add("item.projecte.matter_upgrader.tooltip",
				"Upgrades collectors, power flowers, and relays in-place.");
		add("item.projecte.matter_upgrader.tooltip2",
				"You must have learned the next tier, and have the difference in emc available to upgrade.");
		add("item.projecte.matter_upgrader.tooltip_creative",
				"The above restriction does not apply if you are in creative.");
		add("item.projecte.orange_compressed_collector",
				"Orange Compressed Collector [MK 13]");
		add("item.projecte.orange_fuel",
				"Orange Fuel");
		add("item.projecte.orange_matter",
				"Orange Matter");
		add("item.projecte.pink_compressed_collector",
				"Pink Compressed Collector [MK 5]");
		add("item.projecte.pink_fuel",
				"Pink Fuel");
		add("item.projecte.pink_matter",
				"Pink Matter");
		add("item.projecte.purple_compressed_collector",
				"Purple Compressed Collector [MK 6]");
		add("item.projecte.purple_fuel",
				"Purple Fuel");
		add("item.projecte.purple_matter",
				"Purple Matter");
		add("item.projecte.red_compressed_collector",
				"Red Compressed Collector [MK 3]");
		add("item.projecte.violet_compressed_collector",
				"Violet Compressed Collector [MK 7]");
		add("item.projecte.violet_fuel",
				"Violet Fuel");
		add("item.projecte.violet_matter",
				"Violet Matter");
		add("item.projecte.white_compressed_collector",
				"White Compressed Collector [MK 14]");
		add("item.projecte.white_fuel",
				"White Fuel");
		add("item.projecte.white_matter",
				"White Matter");
		add("item.projecte.yellow_compressed_collector",
				"Yellow Compressed Collector [MK 12]");
		add("item.projecte.yellow_fuel",
				"Yellow Fuel");
		add("item.projecte.yellow_matter",
				"Yellow Matter");
		add("itemGroup.projecte.expansion",
				"ProjectE Phoenix: Beyond Red Matter");
		add("key.projecte.curios.open_transmutation_tablet",
				"Open Transmutation Tablet");
		add("projecte.configuration.alchemicalCollectionSound",
				"Alchemical Collection Sound");
		add("projecte.configuration.collectorMultiplier",
				"Collector Multiplier");
		add("projecte.configuration.compactSunBonus",
				"Compact Sun Bonus");
		add("projecte.configuration.editOthersAlchemicalBooks",
				"Edit Others Alchemical Books");
		add("projecte.configuration.emcDisplay",
				"EMC Display");
		add("projecte.configuration.emcDisplayPosition",
				"EMC Display Position");
		add("projecte.configuration.emcLinkEMCLimitMultiplier",
				"EMC Link EMC Limit Multiplier");
		add("projecte.configuration.emcLinkFluidLimitMultiplier",
				"EMC Link Fluid Limit Multiplier");
		add("projecte.configuration.emcLinkItemLimitMultiplier",
				"EMC Link Item Limit Multiplier");
		add("projecte.configuration.enableCollectorOptimizations",
				"Enable Collector Optimizations");
		add("projecte.configuration.enableFluidEfficiency",
				"Enable Fluid Efficiency");
		add("projecte.configuration.enableLearnedTooltip",
				"Enable Learned Tooltip");
		add("projecte.configuration.formatEMC",
				"Format EMC");
		add("projecte.configuration.fullNumberNames",
				"Full Number Names");
		add("projecte.configuration.infiniteFuelBurnTime",
				"Infinite Fuel Burn Time");
		add("projecte.configuration.infiniteFuelCost",
				"Infinite Fuel Cost");
		add("projecte.configuration.infiniteSteakCost",
				"Infinite Steak Cost");
		add("projecte.configuration.limitEmcLinkVendor",
				"Limit EMC Link Vendor Output");
		add("projecte.configuration.notifyCommandChanges",
				"Notify Command Changes");
		add("projecte.configuration.notifyKnowledgeBookGains",
				"Notify Knowledge Book Gains");
		add("projecte.configuration.persistEnchantedBooksOnly",
				"Persist Enchanted Books Only");
		add("projecte.configuration.powerflowerMultiplier",
				"Power Flower Multiplier");
		add("projecte.configuration.relayBonusMultiplier",
				"Relay Bonus Multiplier");
		add("projecte.configuration.relayTransferMultiplier",
				"Relay Transfer Multiplier");
		add("projecte.configuration.searchType",
				"Search Type");
		add("projecte.configuration.sunMultiplierPriceCompensation",
				"Sun Multiplier Price Compensation");
		add("projecte.configuration.tickDelay",
				"Tick Delay");
		add("projecte.configuration.transmutationInterfaceItemCount",
				"Transmutation Interface Item Count");
		add("projecte.configuration.zeroEmcFluidsAreFree",
				"Zero EMC Fluids are Free");
		add("sounds.projecte.alchemical_collection.collect",
				"Alchemically Collected");
		add("sounds.projecte.knowledge_sharing_book.store",
				"Knowledge Stored");
		add("sounds.projecte.knowledge_sharing_book.use",
				"Knowledge Gained");
		add("sounds.projecte.knowledge_sharing_book.use_none",
				"No Knowledge Gained");
		add("tag.item.projecte.fuel",
				"Fuel");
		add("tag.item.projecte.matter",
				"Matter");
		add("tag.item.projecte.transmutation_tablets",
				"Transmutation Tablets");
		add("tag.item.projecte.advanced_alchemical_chest",
				"Advanced Alchemical Chest");
		add("tag.item.projecte.collector",
				"Collector");
		add("tag.item.projecte.compressed_collector",
				"Compressed Collector");
		add("tag.item.projecte.emc_link",
				"EMC Link");
		add("tag.item.projecte.power_flower",
				"Power Flower");
		add("tag.item.projecte.relay",
				"Relay");
		add("tag.item.projecte.sun_exposure_protection",
				"Sun Exposure Protection");
		add("text.projecte.alchemical_collection",
				"Alchemical Collection: %s");
		add("text.projecte.charge_rate",
				"Charge Rate: %s/s");
		add("text.projecte.color",
				"Color: %s");
		add("text.projecte.cost",
				"Cost: %s");
		add("text.projecte.disabled",
				"Disabled ✗");
		add("text.projecte.emc_import_limit",
				"EMC Import Limit: %s/%s");
		add("text.projecte.emc_limit",
				"EMC Limit: %s");
		add("text.projecte.emc_per_second",
				"EMC Generation: %s/s");
		add("text.projecte.emc_per_second_bonus",
				"EMC Generation: %s/s");
		add("text.projecte.emc_storage",
				"EMC Storage: %s/%s");
		add("text.projecte.emc_transfer_rate",
				"EMC Transfer Rate: %s/s");
		add("text.projecte.enabled",
				"Enabled ✓");
		add("text.projecte.failed_to_get_knowledge_provider",
				"Failed to get knowledge provider for %s.");
		add("text.projecte.filter_status",
				"Filter Status: %s");
		add("text.projecte.fluid_export_efficiency",
				"Fluid Export Efficiency: %s");
		add("text.projecte.fluid_export_limit",
				"Fluid Export Limit: %s/%s");
		add("text.projecte.item_export_limit",
				"Item Export Limit: %s/%s");
		add("text.projecte.item_import_limit",
				"Item Import Limit: %s/%s");
		add("text.projecte.learned",
				"Learned ✓");
		add("text.projecte.matter",
				"Matter: %s");
		add("text.projecte.nbt_filter.disable",
				"NBT Filter Disabled");
		add("text.projecte.nbt_filter.enable",
				"NBT Filter Enabled");
		add("text.projecte.not_learned",
				"Not Learned ✗");
		add("text.projecte.not_owner",
				"You do not own this, %s does.");
		add("text.projecte.owner",
				"Owner: %s");
		add("text.projecte.provider_error",
				"Failed to fetch the ProjectE Phoenix provider.");
		add("text.projecte.relay_bonus",
				"Bonus: %s");
		add("text.projecte.sun_bonus",
				"Sun Bonus: %s");
		add("text.projecte.sun_level",
				"Sun Level: %s");
		add("text.projecte.wip",
				"WIP!");
	}
}
