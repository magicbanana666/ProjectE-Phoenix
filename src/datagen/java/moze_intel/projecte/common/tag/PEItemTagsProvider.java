package moze_intel.projecte.common.tag;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.blacklist.BlacklistType;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.KleinStar.KleinTier;
import moze_intel.projecte.gameObjs.registration.impl.ItemRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

public class PEItemTagsProvider extends ItemTagsProvider {

	public PEItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, PECore.MODID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		addBags();
		addGear();
		addIgnoreMissing();
		addExpansionTags();
		addRitualOnly();
		for (BlacklistType blacklistType : BlacklistType.values()) {
			tag(blacklistType.getBlacklist()).addTag(PETags.Items.RITUAL_ONLY);
		}
		tag(ItemTags.BOOKSHELF_BOOKS).add(projecteItem(PEItems.TOME_OF_KNOWLEDGE));
		tag(ItemTags.FREEZE_IMMUNE_WEARABLES).add(projecteItem(PEItems.GEM_CHESTPLATE));
		tag(PETags.Items.COLLECTOR_FUEL).add(
				Items.CHARCOAL,
				Items.REDSTONE,
				Items.REDSTONE_BLOCK,
				Items.COAL,
				Items.COAL_BLOCK,
				Items.GUNPOWDER,
				Items.GLOWSTONE_DUST,
				Items.BLAZE_POWDER,
				Items.GLOWSTONE,
				projecteItem(PEItems.ALCHEMICAL_COAL),
				projecteItem(PEBlocks.ALCHEMICAL_COAL),
				projecteItem(PEItems.MOBIUS_FUEL),
				projecteItem(PEBlocks.MOBIUS_FUEL),
				projecteItem(PEItems.AETERNALIS_FUEL),
				projecteItem(PEBlocks.AETERNALIS_FUEL)
		);
		tag(PETags.Items.COVALENCE_DUST).add(
				projecteItem(PEItems.LOW_COVALENCE_DUST),
				projecteItem(PEItems.MEDIUM_COVALENCE_DUST),
				projecteItem(PEItems.HIGH_COVALENCE_DUST)
		);
		tag(PETags.Items.DATA_COMPONENT_WHITELIST);
		tag(PETags.Items.CURIOS_BELT).add(
				projecteItem(PEItems.REPAIR_TALISMAN),
				projecteItem(PEItems.WATCH_OF_FLOWING_TIME)
		);
		tag(PETags.Items.TRANSMUTATION_TABLET).add(projecteItem(PEItems.TRANSMUTATION_TABLET));
		tag(PETags.Items.RELAYS).add(
				projecteItem(PEBlocks.RELAY),
				projecteItem(PEBlocks.RELAY_MK2),
				projecteItem(PEBlocks.RELAY_MK3)
		);
		tag(PETags.Items.COLLECTORS).add(
				projecteItem(PEBlocks.COLLECTOR),
				projecteItem(PEBlocks.COLLECTOR_MK2),
				projecteItem(PEBlocks.COLLECTOR_MK3)
		);
		tag(PETags.Items.MATTER_FURNACES).add(
				projecteItem(PEBlocks.DARK_MATTER_FURNACE),
				projecteItem(PEBlocks.RED_MATTER_FURNACE)
		);
		TagAppender<Item, Item> kleinStars = tag(PETags.Items.KLEIN_STARS);
		for (KleinTier tier : KleinTier.values()) {
			kleinStars.add(projecteItem(PEItems.getStar(tier)));
		}
		tag(PETags.Items.CURIOS_KLEIN_STAR).addTags(PETags.Items.KLEIN_STARS, PETags.Items.CURIOS_EXPANSION_KLEIN_STAR);
		//The Curio slot of the transmutation tablet has to accept the arcane tablet of the addon as well, it is the
		//same kind of item from the user's point of view
		tag(PETags.Items.CURIOS_TRANSMUTATION_TABLET).addTags(PETags.Items.TRANSMUTATION_TABLET, PETags.Items.EXPANSION_TRANSMUTATION_TABLETS);
		tag(PETags.Items.CURIOS_NECKLACE).add(
				projecteItem(PEItems.BODY_STONE),
				projecteItem(PEItems.EVERTIDE_AMULET),
				projecteItem(PEItems.LIFE_STONE),
				projecteItem(PEItems.SOUL_STONE),
				projecteItem(PEItems.VOLCANITE_AMULET)
		);
		tag(PETags.Items.CURIOS_RING).add(
				projecteItem(PEItems.ARCANA_RING),
				projecteItem(PEItems.BLACK_HOLE_BAND),
				projecteItem(PEItems.GEM_OF_ETERNAL_DENSITY),
				projecteItem(PEItems.IGNITION_RING),
				projecteItem(PEItems.SWIFTWOLF_RENDING_GALE),
				projecteItem(PEItems.VOID_RING),
				projecteItem(PEItems.ZERO_RING)
		);
		addTags(tag(PETags.Items.PLANTABLE_SEEDS),
				ItemTags.VILLAGER_PLANTABLE_SEEDS,
				//Note: Try adding any seeds that aren't in the villager plantable ones, in case we are able to plant them
				Tags.Items.SEEDS);
		//Vanilla/Forge Tags
		tag(Tags.Items.BARRELS).add(projecteItem(PEBlocks.ALCHEMICAL_BARREL));
		tag(Tags.Items.TOOLS_SHEAR).add(
				projecteItem(PEItems.DARK_MATTER_SHEARS),
				projecteItem(PEItems.RED_MATTER_SHEARS),
				projecteItem(PEItems.RED_MATTER_KATAR)
		);
		tag(Tags.Items.CHESTS).add(projecteItem(PEBlocks.ALCHEMICAL_CHEST));
		tag(Tags.Items.PLAYER_WORKSTATIONS_FURNACES).add(
				projecteItem(PEBlocks.DARK_MATTER_FURNACE),
				projecteItem(PEBlocks.RED_MATTER_FURNACE)
		);
		tag(ItemTags.BEACON_PAYMENT_ITEMS).add(
				projecteItem(PEItems.DARK_MATTER),
				projecteItem(PEItems.RED_MATTER)
		);
	}

	private void addIgnoreMissing() {
		TagAppender<Item, Item> ignoreMissingEMC = tag(PETags.Items.IGNORE_MISSING_EMC).add(
				Items.DEBUG_STICK, Items.KNOWLEDGE_BOOK, Items.STRUCTURE_VOID, Items.FROGSPAWN,
				Items.PETRIFIED_OAK_SLAB, Items.REINFORCED_DEEPSLATE, Items.SPAWNER, Items.TRIAL_SPAWNER,
				Items.VAULT, Items.TRIAL_KEY, Items.OMINOUS_TRIAL_KEY, Items.ELYTRA,
				Items.TOTEM_OF_UNDYING, Items.EXPERIENCE_BOTTLE, Items.OMINOUS_BOTTLE, Items.DRAGON_HEAD,
				Items.PLAYER_HEAD, Items.WITHER_SKELETON_SKULL, Items.BEE_NEST, Items.FARMLAND,
				Items.COMMAND_BLOCK_MINECART, Items.BUDDING_AMETHYST, Items.SMALL_AMETHYST_BUD,
				Items.MEDIUM_AMETHYST_BUD, Items.LARGE_AMETHYST_BUD,
				//Blocks that have no emc because it is less than one:
				Items.STONE_SLAB, Items.COBBLESTONE_SLAB, Items.SMOOTH_STONE_SLAB,
				Items.STONE_BRICK_SLAB, Items.END_STONE_BRICK_SLAB, Items.GLASS_PANE,
				Items.CYAN_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE, Items.LIME_STAINED_GLASS_PANE,
				Items.MAGENTA_STAINED_GLASS_PANE, Items.PINK_STAINED_GLASS_PANE
		);
		addTags(ignoreMissingEMC, Tags.Items.CLUSTERS, Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS);
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof SpawnEggItem || item instanceof MobBucketItem) {
				ignoreMissingEMC.add(item);
			} else if (item instanceof BlockItem blockItem) {
				Block block = blockItem.getBlock();
				if (block instanceof InfestedBlock || block instanceof HugeMushroomBlock) {
					ignoreMissingEMC.add(item);
				}
			}
		}
	}

	private void addBags() {
		TagAppender<Item, Item> alchemicalBags = tag(PETags.Items.ALCHEMICAL_BAGS);
		for (DyeColor color : Constants.COLORS) {
			ItemRegistryObject<AlchemicalBag> bag = PEItems.getBagReference(color);
			alchemicalBags.add(projecteItem(bag));
			tag(color.getDyedTag()).add(projecteItem(bag));
		}
	}

	private void addGear() {
		addArmor();
		addTags(tag(Tags.Items.TOOLS), PETags.Items.TOOLS_HAMMERS, PETags.Items.TOOLS_KATARS, PETags.Items.TOOLS_MORNING_STARS);
		addTool(ItemTags.SWORDS, new Item[]{
				projecteItem(PEItems.DARK_MATTER_SWORD), projecteItem(PEItems.RED_MATTER_SWORD)},
				ItemTags.WEAPON_ENCHANTABLE, ItemTags.SHARP_WEAPON_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE);
		addTool(ItemTags.AXES, new Item[]{
				projecteItem(PEItems.DARK_MATTER_AXE), projecteItem(PEItems.RED_MATTER_AXE)},
				ItemTags.SHARP_WEAPON_ENCHANTABLE, ItemTags.MINING_ENCHANTABLE, ItemTags.MINING_LOOT_ENCHANTABLE,
				ItemTags.DURABILITY_ENCHANTABLE);
		addTool(ItemTags.PICKAXES, new Item[]{
				projecteItem(PEItems.DARK_MATTER_PICKAXE), projecteItem(PEItems.RED_MATTER_PICKAXE)},
				ItemTags.MINING_ENCHANTABLE, ItemTags.MINING_LOOT_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE);
		addTool(ItemTags.SHOVELS, new Item[]{
				projecteItem(PEItems.DARK_MATTER_SHOVEL), projecteItem(PEItems.RED_MATTER_SHOVEL)},
				ItemTags.MINING_ENCHANTABLE, ItemTags.MINING_LOOT_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE);
		addTool(ItemTags.HOES, new Item[]{
				projecteItem(PEItems.DARK_MATTER_HOE), projecteItem(PEItems.RED_MATTER_HOE)},
				ItemTags.MINING_ENCHANTABLE, ItemTags.MINING_LOOT_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE);

		//Note: For our tool types these aren't added to any of the enchantable tags, but we remove them just in case someone else adds the base tags to an enchantable one
		addTool(PETags.Items.TOOLS_HAMMERS, new Item[]{
				projecteItem(PEItems.DARK_MATTER_HAMMER), projecteItem(PEItems.RED_MATTER_HAMMER)},
				ItemTags.MINING_ENCHANTABLE, ItemTags.MINING_LOOT_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE);
		addTool(PETags.Items.TOOLS_KATARS, new Item[]{projecteItem(PEItems.RED_MATTER_KATAR)},
				ItemTags.WEAPON_ENCHANTABLE, ItemTags.SHARP_WEAPON_ENCHANTABLE, ItemTags.MINING_ENCHANTABLE,
				ItemTags.MINING_LOOT_ENCHANTABLE);
		addTool(PETags.Items.TOOLS_MORNING_STARS, new Item[]{projecteItem(PEItems.RED_MATTER_MORNING_STAR)},
				ItemTags.MINING_ENCHANTABLE, ItemTags.MINING_LOOT_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE);

		tag(Tags.Items.MELEE_WEAPON_TOOLS).addTag(PETags.Items.TOOLS_KATARS);
		addTags(tag(Tags.Items.MINING_TOOL_TOOLS), PETags.Items.TOOLS_HAMMERS, PETags.Items.TOOLS_MORNING_STARS);
		addTags(tag(ItemTags.BREAKS_DECORATED_POTS), PETags.Items.TOOLS_HAMMERS, PETags.Items.TOOLS_KATARS,
				PETags.Items.TOOLS_MORNING_STARS);
		//Shields
		tag(Tags.Items.TOOLS_SHIELD).add(projecteItem(PEItems.DARK_MATTER_SHIELD), projecteItem(PEItems.RED_MATTER_SHIELD));
		//Tridents
		Item darkTrident = projecteItem(PEItems.DARK_MATTER_TRIDENT);
		Item redTrident = projecteItem(PEItems.RED_MATTER_TRIDENT);
		tag(ItemTags.SPEARS).add(darkTrident, redTrident);
		tag(ItemTags.BREAKS_DECORATED_POTS).add(darkTrident, redTrident);
		tag(Tags.Items.MELEE_WEAPON_TOOLS).add(darkTrident, redTrident);
		tag(Tags.Items.RANGED_WEAPON_TOOLS).add(darkTrident, redTrident);
	}

	private void addArmor() {
		addArmor(ItemTags.HEAD_ARMOR, ItemTags.HEAD_ARMOR_ENCHANTABLE,
				projecteItem(PEItems.DARK_MATTER_HELMET), projecteItem(PEItems.RED_MATTER_HELMET),
				projecteItem(PEItems.GEM_HELMET));
		addArmor(ItemTags.CHEST_ARMOR, ItemTags.CHEST_ARMOR_ENCHANTABLE,
				projecteItem(PEItems.DARK_MATTER_CHESTPLATE), projecteItem(PEItems.RED_MATTER_CHESTPLATE),
				projecteItem(PEItems.GEM_CHESTPLATE));
		addArmor(ItemTags.LEG_ARMOR, ItemTags.LEG_ARMOR_ENCHANTABLE,
				projecteItem(PEItems.DARK_MATTER_LEGGINGS), projecteItem(PEItems.RED_MATTER_LEGGINGS),
				projecteItem(PEItems.GEM_LEGGINGS));
		addArmor(ItemTags.FOOT_ARMOR, ItemTags.FOOT_ARMOR_ENCHANTABLE,
				projecteItem(PEItems.DARK_MATTER_BOOTS), projecteItem(PEItems.RED_MATTER_BOOTS),
				projecteItem(PEItems.GEM_BOOTS));
	}

	private void addTool(TagKey<Item> toolTag, Item[] items, TagKey<Item>... enchantableTags) {
		tag(toolTag).add(items);
		for (TagKey<Item> enchantableTag : enchantableTags) {
			tag(enchantableTag).add(items);
		}
	}

	private void addArmor(TagKey<Item> armorTag, TagKey<Item> armorTagEnchantable, Item... items) {
		tag(armorTag).add(items);
		tag(armorTagEnchantable).add(items);
	}

	/**
	 * Everything the rituals in data/projecte/recipe/athanor make, the stone the cauldron brews, and the blocks that would give the matter back.
	 */
	private void addRitualOnly() {
		TagAppender<Item, Item> ritualOnly = tag(PETags.Items.RITUAL_ONLY).add(
				projecteItem(PEItems.PHILOSOPHERS_STONE),
				projecteItem(PEItems.FALSE_PHILOSOPHERS_STONE),
				projecteItem(PEItems.KLEIN_STAR_EIN),
				projecteItem(PEItems.KLEIN_STAR_ZWEI),
				projecteItem(PEItems.KLEIN_STAR_DREI),
				projecteItem(PEItems.KLEIN_STAR_VIER),
				projecteItem(PEItems.KLEIN_STAR_SPHERE),
				projecteItem(PEItems.KLEIN_STAR_OMEGA),
				projecteItem("infinite_fuel"),
				projecteItem("infinite_steak")
		);
		for (String color : List.of("magenta", "pink", "purple", "violet", "blue", "cyan", "green", "lime", "yellow", "orange", "white")) {
			ritualOnly.add(projecteItem(color + "_matter"), projecteItem(color + "_matter_block"));
		}
	}

	private static Item projecteItem(ItemLike item) {
		return item.asItem();
	}

	private static TagAppender<Item, Item> addTags(TagAppender<Item, Item> appender, TagKey<Item>... tags) {
		for (TagKey<Item> tag : tags) {
			appender.addTag(tag);
		}
		return appender;
	}

	private void addExpansionTags() {
	//Tags of the content that used to live in the ProjectExpansion addon
		//Tags of the content that used to live in the ProjectExpansion addon
		tag(PETags.Items.EXPANSION_COLLECTOR_FUEL).add(
				projecteItem("magenta_fuel"), projecteItem("pink_fuel"), projecteItem("purple_fuel"), projecteItem("violet_fuel"),
				projecteItem("blue_fuel"), projecteItem("cyan_fuel"), projecteItem("green_fuel"), projecteItem("lime_fuel"),
				projecteItem("yellow_fuel"), projecteItem("orange_fuel"), projecteItem("white_fuel"), projecteItem("magenta_fuel_block"),
				projecteItem("pink_fuel_block"), projecteItem("purple_fuel_block"), projecteItem("violet_fuel_block"), projecteItem("blue_fuel_block"),
				projecteItem("cyan_fuel_block"), projecteItem("green_fuel_block"), projecteItem("lime_fuel_block"), projecteItem("yellow_fuel_block"),
				projecteItem("orange_fuel_block"), projecteItem("white_fuel_block"));
		//Tags of the content that used to live in the ProjectExpansion addon
		tag(PETags.Items.EXPANSION_TRANSMUTATION_TABLETS).add(
				projecteItem("arcane_transmutation_tablet"));
	}

	//26.1: the tag appenders still take the registry objects themselves, so the ids of the addon items have to be
	// resolved through the registry. Resolving them is done hard so a typo fails datagen instead of writing a bad tag.
	private static Item projecteItem(String name) {
		return Objects.requireNonNull(BuiltInRegistries.ITEM.getValue(PECore.rl(name)), "Unknown item " + name);
	}

}
