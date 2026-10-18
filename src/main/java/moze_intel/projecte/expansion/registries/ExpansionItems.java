package moze_intel.projecte.expansion.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.expansion.item.ItemAlchemicalBook;
import moze_intel.projecte.expansion.item.ItemAlchemistsCodex;
import moze_intel.projecte.expansion.item.ItemArcaneTransmutationTablet;
import moze_intel.projecte.expansion.item.ItemFinalStar;
import moze_intel.projecte.expansion.item.ItemFinalStarShard;
import moze_intel.projecte.expansion.item.ItemInfiniteFuel;
import moze_intel.projecte.expansion.item.ItemInfiniteSteak;
import moze_intel.projecte.expansion.item.ItemKnowledgeSharingBook;
import moze_intel.projecte.expansion.item.ItemMatterUpgrader;
import moze_intel.projecte.gameObjs.registration.impl.ItemDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.ItemRegistryObject;

@SuppressWarnings("unused")
public class ExpansionItems {

	public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(PECore.MODID);

	public static final ItemRegistryObject<ItemFinalStarShard> FINAL_STAR_SHARD = ITEMS.registerSimple("final_star_shard", properties -> new ItemFinalStarShard(properties));
	public static final ItemRegistryObject<ItemFinalStar> FINAL_STAR = ITEMS.registerSimple("final_star", properties -> new ItemFinalStar(properties));
	public static final ItemRegistryObject<ItemMatterUpgrader> MATTER_UPGRADER = ITEMS.registerSimple("matter_upgrader", properties -> new ItemMatterUpgrader(properties));
	public static final ItemRegistryObject<ItemInfiniteFuel> INFINITE_FUEL = ITEMS.registerSimple("infinite_fuel", properties -> new ItemInfiniteFuel(properties));
	public static final ItemRegistryObject<ItemInfiniteSteak> INFINITE_STEAK = ITEMS.registerSimple("infinite_steak", properties -> new ItemInfiniteSteak(properties));
	public static final ItemRegistryObject<ItemKnowledgeSharingBook> KNOWLEDGE_SHARING_BOOK = ITEMS.registerSimple("knowledge_sharing_book", properties -> new ItemKnowledgeSharingBook(properties));
	public static final ItemRegistryObject<ItemAlchemicalBook> BASIC_ALCHEMICAL_BOOK = ITEMS.registerSimple("basic_alchemical_book", properties -> new ItemAlchemicalBook(properties, ItemAlchemicalBook.Tier.BASIC));
	public static final ItemRegistryObject<ItemAlchemicalBook> ADVANCED_ALCHEMICAL_BOOK = ITEMS.registerSimple("advanced_alchemical_book", properties -> new ItemAlchemicalBook(properties, ItemAlchemicalBook.Tier.ADVANCED));
	public static final ItemRegistryObject<ItemAlchemicalBook> MASTER_ALCHEMICAL_BOOK = ITEMS.registerSimple("master_alchemical_book", properties -> new ItemAlchemicalBook(properties, ItemAlchemicalBook.Tier.MASTER));
	public static final ItemRegistryObject<ItemAlchemicalBook> ARCANE_ALCHEMICAL_BOOK = ITEMS.registerSimple("arcane_alchemical_book", properties -> new ItemAlchemicalBook(properties, ItemAlchemicalBook.Tier.ARCANE));
	public static final ItemRegistryObject<ItemAlchemistsCodex> ALCHEMISTS_CODEX = ITEMS.registerSimple("alchemists_codex", properties -> new ItemAlchemistsCodex(properties));
	public static final ItemRegistryObject<ItemArcaneTransmutationTablet> ARCANE_TRANSMUTATION_TABLET = ITEMS.registerSimple("arcane_transmutation_tablet", properties -> new ItemArcaneTransmutationTablet(properties));
}
