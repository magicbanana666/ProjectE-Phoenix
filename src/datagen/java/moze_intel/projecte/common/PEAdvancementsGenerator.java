package moze_intel.projecte.common;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class PEAdvancementsGenerator extends AdvancementSubProvider {

	private final HolderGetter<Item> items;

	public PEAdvancementsGenerator(BootstrapContext<Advancement> output) {
		super(output);
		this.items = output.lookup(Registries.ITEM);
	}

	@Override
	public void generate() {
		AdvancementHolder root = Advancement.Builder.advancement()
				.rootDisplay(PEItems.PHILOSOPHERS_STONE.asItem(),
						PELang.PROJECTE.translate(),
						PELang.ADVANCEMENTS_PROJECTE_DESCRIPTION.translate(),
						Identifier.withDefaultNamespace("gui/advancements/backgrounds/stone"),
						AdvancementType.TASK,
						false,
						false,
						false)
				.addCriterion("philstone_recipe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLOWSTONE_DUST, Items.DIAMOND, Items.REDSTONE))
				.save(output, PECore.rl("root"));
		addTransmutation(root);
		addStorage(root);
		addMatters(root);
	}

	private static Advancement.Builder childDisplay(AdvancementHolder parent, ItemLike icon, ILangEntry title, ILangEntry description) {
		return Advancement.Builder.advancement()
				.parent(parent)
				.display(icon.asItem(), title.translate(), description.translate(), AdvancementType.TASK, true, true, false);
	}

	private void addTransmutation(AdvancementHolder parent) {
		AdvancementHolder root = childDisplay(parent, PEItems.PHILOSOPHERS_STONE, PELang.ADVANCEMENTS_PHILO_STONE, PELang.ADVANCEMENTS_PHILO_STONE_DESCRIPTION)
				.addCriterion("philosophers_stone", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.PHILOSOPHERS_STONE))
				.save(output, PECore.rl("philosophers_stone"));
		//Branch 1
		AdvancementHolder transmutationTable = childDisplay(root, PEBlocks.TRANSMUTATION_TABLE, PELang.ADVANCEMENTS_TRANSMUTATION_TABLE,
				PELang.ADVANCEMENTS_TRANSMUTATION_TABLE_DESCRIPTION)
				.addCriterion("trans_table", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.TRANSMUTATION_TABLE))
				.save(output, PECore.rl("transmutation_table"));
		childDisplay(transmutationTable, PEItems.TRANSMUTATION_TABLET, PELang.ADVANCEMENTS_TRANSMUTATION_TABLET, PELang.ADVANCEMENTS_TRANSMUTATION_TABLET_DESCRIPTION)
				.addCriterion("trans_tablet", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.TRANSMUTATION_TABLET))
				.save(output, PECore.rl("transmutation_tablet"));
		//Branch 2
		AdvancementHolder kleinStarEin = childDisplay(root, PEItems.KLEIN_STAR_EIN, PELang.ADVANCEMENTS_KLEIN_STAR, PELang.ADVANCEMENTS_KLEIN_STAR_DESCRIPTION)
				.addCriterion("klein_star", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.KLEIN_STAR_EIN))
				.save(output, PECore.rl("klein_star_ein"));
		childDisplay(kleinStarEin, PEItems.KLEIN_STAR_OMEGA, PELang.ADVANCEMENTS_KLEIN_STAR_BIG, PELang.ADVANCEMENTS_KLEIN_STAR_BIG_DESCRIPTION)
				.addCriterion("klein_star", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.KLEIN_STAR_OMEGA))
				.save(output, PECore.rl("klein_star_omega"));
	}

	private void addStorage(AdvancementHolder parent) {
		AdvancementHolder root = childDisplay(parent, PEBlocks.ALCHEMICAL_CHEST, PELang.ADVANCEMENTS_ALCH_CHEST, PELang.ADVANCEMENTS_ALCH_CHEST_DESCRIPTION)
				.addCriterion("alch_chest", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.ALCHEMICAL_CHEST))
				.save(output, PECore.rl("alchemical_chest"));
		//Branch 1
		childDisplay(root, PEItems.WHITE_ALCHEMICAL_BAG, PELang.ADVANCEMENTS_ALCH_BAG, PELang.ADVANCEMENTS_ALCH_BAG_DESCRIPTION)
				.addCriterion("bag", InventoryChangeTrigger.TriggerInstance.hasItems(
						ItemPredicate.Builder.item().of(items, PETags.Items.ALCHEMICAL_BAGS).build()))
				.save(output, PECore.rl("alchemical_bag"));
		//Alchemical Barrel
		addStorageBarrels(root);
		//Branch 2
		AdvancementHolder condenser = childDisplay(root, PEBlocks.CONDENSER, PELang.ADVANCEMENTS_CONDENSER, PELang.ADVANCEMENTS_CONDENSER_DESCRIPTION)
				.addCriterion("condenser", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.CONDENSER))
				.save(output, PECore.rl("condenser"));
		AdvancementHolder collector = childDisplay(condenser, PEBlocks.COLLECTOR, PELang.ADVANCEMENTS_COLLECTOR, PELang.ADVANCEMENTS_COLLECTOR_DESCRIPTION)
				.addCriterion("collector", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.COLLECTOR))
				.save(output, PECore.rl("collector"));
		childDisplay(collector, PEBlocks.RELAY, PELang.ADVANCEMENTS_RELAY, PELang.ADVANCEMENTS_RELAY_DESCRIPTION)
				.addCriterion("relay", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.RELAY))
				.save(output, PECore.rl("relay"));
	}

	private void addMatters(AdvancementHolder parent) {
		AdvancementHolder root = childDisplay(parent, PEItems.DARK_MATTER, PELang.ADVANCEMENTS_DARK_MATTER, PELang.ADVANCEMENTS_DARK_MATTER_DESCRIPTION)
				.addCriterion("dm", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.DARK_MATTER))
				.save(output, PECore.rl("dark_matter"));
		//Branch 1
		AdvancementHolder dmPickaxe = childDisplay(root, PEItems.DARK_MATTER_PICKAXE, PELang.ADVANCEMENTS_DARK_MATTER_PICKAXE,
				PELang.ADVANCEMENTS_DARK_MATTER_PICKAXE_DESCRIPTION)
				.addCriterion("dm_pick", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.DARK_MATTER_PICKAXE))
				.save(output, PECore.rl("dark_matter_pickaxe"));
		childDisplay(dmPickaxe, PEItems.RED_MATTER_PICKAXE, PELang.ADVANCEMENTS_RED_MATTER_PICKAXE, PELang.ADVANCEMENTS_RED_MATTER_PICKAXE_DESCRIPTION)
				.addCriterion("rm_pick", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.RED_MATTER_PICKAXE))
				.save(output, PECore.rl("red_matter_pickaxe"));
		//Branch 2
		AdvancementHolder redMatter = childDisplay(root, PEItems.RED_MATTER, PELang.ADVANCEMENTS_RED_MATTER, PELang.ADVANCEMENTS_RED_MATTER_DESCRIPTION)
				.addCriterion("rm", InventoryChangeTrigger.TriggerInstance.hasItems(PEItems.RED_MATTER))
				.save(output, PECore.rl("red_matter"));
		AdvancementHolder redMatterBlock = childDisplay(redMatter, PEBlocks.RED_MATTER, PELang.ADVANCEMENTS_RED_MATTER_BLOCK, PELang.ADVANCEMENTS_RED_MATTER_BLOCK_DESCRIPTION)
				.addCriterion("rm_block", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.RED_MATTER))
				.save(output, PECore.rl("red_matter_block"));
		childDisplay(redMatterBlock, PEBlocks.RED_MATTER_FURNACE, PELang.ADVANCEMENTS_RED_MATTER_FURNACE, PELang.ADVANCEMENTS_RED_MATTER_FURNACE_DESCRIPTION)
				.addCriterion("rm_furnace", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.RED_MATTER_FURNACE))
				.save(output, PECore.rl("red_matter_furnace"));
		//Branch 3
		AdvancementHolder darkMatterBlock = childDisplay(root, PEBlocks.DARK_MATTER, PELang.ADVANCEMENTS_DARK_MATTER_BLOCK, PELang.ADVANCEMENTS_DARK_MATTER_BLOCK_DESCRIPTION)
				.addCriterion("dm_block", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.DARK_MATTER))
				.save(output, PECore.rl("dark_matter_block"));
		childDisplay(darkMatterBlock, PEBlocks.DARK_MATTER_FURNACE, PELang.ADVANCEMENTS_DARK_MATTER_FURNACE, PELang.ADVANCEMENTS_DARK_MATTER_FURNACE_DESCRIPTION)
				.addCriterion("dm_furnace", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.DARK_MATTER_FURNACE))
				.save(output, PECore.rl("dark_matter_furnace"));
	}

	private void addStorageBarrels(AdvancementHolder parent) {
		childDisplay(parent, PEBlocks.ALCHEMICAL_BARREL, PELang.ADVANCEMENTS_ALCHEMICAL_BARREL, PELang.ADVANCEMENTS_ALCHEMICAL_BARREL_DESCRIPTION)
				.addCriterion("alchemical_barrel", InventoryChangeTrigger.TriggerInstance.hasItems(PEBlocks.ALCHEMICAL_BARREL))
				.save(output, PECore.rl("alchemical_barrel"));
	}
}
