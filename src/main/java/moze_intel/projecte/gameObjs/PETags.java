package moze_intel.projecte.gameObjs;

import moze_intel.projecte.PECore;
import moze_intel.projecte.integration.IntegrationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PETags {

	private PETags() {
	}

	public static class Items {

		private Items() {
		}

		public static final TagKey<Item> ALCHEMICAL_BAGS = tag("alchemical_bags");
		public static final TagKey<Item> KLEIN_STARS = tag("klein_stars");
		public static final TagKey<Item> COLLECTORS = tag("collectors");
		public static final TagKey<Item> RELAYS = tag("relays");
		public static final TagKey<Item> MATTER_FURNACES = tag("matter_furnaces");
		/**
		 * Items in this tag will be used for the various collector fuel upgrade recipes.
		 */
		public static final TagKey<Item> COLLECTOR_FUEL = tag("collector_fuel");
		public static final TagKey<Item> EXPANSION_COLLECTOR_FUEL = tag("expansion_collector_fuel");
		public static final TagKey<Item> EXPANSION_TRANSMUTATION_TABLETS = tag("expansion_transmutation_tablets");
		public static final TagKey<Item> EXPANSION_CHESTS = ItemTags.create(Identifier.fromNamespaceAndPath("c", "expansion_chests"));
		public static final TagKey<Item> CURIOS_EXPANSION_KLEIN_STAR = curiosTag("expansion_klein_star");
		/**
		 * Items in this tag can have their Data Components duped by condensers and transmutation tables
		 */
		public static final TagKey<Item> DATA_COMPONENT_WHITELIST = tag("data_component_whitelist");
		/**
		 * Items in this tag can contribute and are "valid dusts" for the covalence repair recipe
		 */
		public static final TagKey<Item> COVALENCE_DUST = tag("covalence_dust");
		/**
		 * Items in this tag won't be listed by the dump missing emc command even if they are missing emc.
		 */
		public static final TagKey<Item> IGNORE_MISSING_EMC = tag("ignore_missing_emc");
		/**
		 * Items in this tag will be checked against being a BlockItem, and attempt to be placed by the harvest goddess band.
		 */
		public static final TagKey<Item> PLANTABLE_SEEDS = tag("plantable_seeds");
		/**
		 * Items in this tag will be valid for the Transmutation Tablet curios slot, and should implement {@link moze_intel.projecte.api.item.ITransmutationTablet}
		 */
		public static final TagKey<Item> TRANSMUTATION_TABLET = tag("transmutation_tablets");
		/**
		 * Items in this tag will be blacklisted from being set as a target in the condenser.
		 */
		public static final TagKey<Item> BLACKLIST_CONDENSER = tag("blacklist_condenser");
		/**
		 * Items in this tag will be blacklisted from being learned in a transmutation table.
		 */
		public static final TagKey<Item> BLACKLIST_LEARNING = tag("blacklist_learning");
		/**
		 * Items only a ritual of the Athanor makes. They keep their EMC, but are in both blacklists, so the Athanor can not be skipped.
		 */
		public static final TagKey<Item> RITUAL_ONLY = tag("ritual_only");

		//Curios tags
		public static final TagKey<Item> CURIOS_BELT = curiosTag("belt");
		public static final TagKey<Item> CURIOS_KLEIN_STAR = curiosTag("klein_star");
		public static final TagKey<Item> CURIOS_TRANSMUTATION_TABLET = curiosTag("transmutation_tablet");
		public static final TagKey<Item> CURIOS_NECKLACE = curiosTag("necklace");
		public static final TagKey<Item> CURIOS_RING = curiosTag("ring");
		//Forge tools/armor tags

		public static final TagKey<Item> TOOLS_HAMMERS = commonTag("tools/hammers");
		public static final TagKey<Item> TOOLS_KATARS = commonTag("tools/katars");
		public static final TagKey<Item> TOOLS_MORNING_STARS = commonTag("tools/morning_stars");

		private static TagKey<Item> tag(String name) {
			return ItemTags.create(PECore.rl(name));
		}

		private static TagKey<Item> curiosTag(String name) {
			return ItemTags.create(Identifier.fromNamespaceAndPath(IntegrationHelper.CURIO_MODID, name));
		}

		private static TagKey<Item> commonTag(String name) {
			return ItemTags.create(Identifier.fromNamespaceAndPath("c", name));
		}
	}

	public static class Blocks {

		private Blocks() {
		}

		/**
		 * Blocks added here will not be broken by the harvest goddess band when unable to continue growing.
		 */
		public static final TagKey<Block> BLACKLIST_HARVEST = tag("blacklist/harvest");
		/**
		 * Blocks that heat an Alchemical Cauldron placed on top of them, one tag per heat. A campfire only counts while it is lit.
		 */
		public static final TagKey<Block> CAULDRON_HEAT_COLD = tag("cauldron_heat/cold");
		public static final TagKey<Block> CAULDRON_HEAT_LOW = tag("cauldron_heat/low");
		public static final TagKey<Block> CAULDRON_HEAT_HIGH = tag("cauldron_heat/high");
		public static final TagKey<Block> CAULDRON_HEAT_SOUL = tag("cauldron_heat/soul");
		/**
		 * Blocks added here will be considered plantable and affected by the harvest goddess band.
		 *
		 * @apiNote This does not include all blocks that will be affected due to being bonemealable.
		 */
		public static final TagKey<Block> OVERRIDE_PLANTABLE = tag("override/plantable");
		/**
		 * Blocks added will not receive extra random ticks from the Watch of Flowing Time
		 */
		public static final TagKey<Block> BLACKLIST_TIME_WATCH = tag("blacklist/time_watch");
		/**
		 * Blocks added will be valid veins for matter shovels
		 */
		public static final TagKey<Block> VEIN_SHOVEL = tag("vein/shovel");

		public static final TagKey<Block> FARMING_OVERRIDE = tag("farming_override");

		public static final TagKey<Block> NEEDS_DARK_MATTER_TOOL = tag("needs_dark_matter_tool");
		public static final TagKey<Block> NEEDS_RED_MATTER_TOOL = tag("needs_red_matter_tool");

		public static final TagKey<Block> INCORRECT_FOR_DARK_MATTER_TOOL = tag("incorrect_for_dark_matter_tool");
		public static final TagKey<Block> INCORRECT_FOR_RED_MATTER_TOOL = tag("incorrect_for_red_matter_tool");

		public static final TagKey<Block> MINEABLE_WITH_PE_KATAR = tag("mineable/katar");
		public static final TagKey<Block> MINEABLE_WITH_PE_HAMMER = tag("mineable/hammer");
		public static final TagKey<Block> MINEABLE_WITH_PE_MORNING_STAR = tag("mineable/morning_star");
		public static final TagKey<Block> MINEABLE_WITH_PE_SHEARS = tag("mineable/shears");
		public static final TagKey<Block> MINEABLE_WITH_PE_SWORD = tag("mineable/sword");

		public static final TagKey<Block> MINEABLE_WITH_HAMMER = commonTag("mineable/hammer");
		public static final TagKey<Block> MINEABLE_WITH_KATAR = commonTag("mineable/katar");
		public static final TagKey<Block> MINEABLE_WITH_MORNING_STAR = commonTag("mineable/morning_star");


		public static final TagKey<Block> EXPANSION_NEEDS_DARK_MATTER_TOOL = tag("expansion_needs_dark_matter_tool");
		public static final TagKey<Block> EXPANSION_NEEDS_RED_MATTER_TOOL = tag("expansion_needs_red_matter_tool");
		public static final TagKey<Block> EXPANSION_CHESTS = BlockTags.create(Identifier.fromNamespaceAndPath("c", "expansion_chests"));
		public static final TagKey<Block> EXPANSION_BEACON_BASE_BLOCKS = BlockTags.create(Identifier.fromNamespaceAndPath("minecraft", "expansion_beacon_base_blocks"));
		public static final TagKey<Block> EXPANSION_DRAGON_IMMUNE = BlockTags.create(Identifier.fromNamespaceAndPath("minecraft", "expansion_dragon_immune"));
		public static final TagKey<Block> EXPANSION_GUARDED_BY_PIGLINS = BlockTags.create(Identifier.fromNamespaceAndPath("minecraft", "expansion_guarded_by_piglins"));
		public static final TagKey<Block> EXPANSION_INFINIBURN_OVERWORLD = BlockTags.create(Identifier.fromNamespaceAndPath("minecraft", "expansion_infiniburn_overworld"));
		public static final TagKey<Block> EXPANSION_WITHER_IMMUNE = BlockTags.create(Identifier.fromNamespaceAndPath("minecraft", "expansion_wither_immune"));
		public static final TagKey<Block> EXPANSION_MINEABLE_WITH_PICKAXE = BlockTags.create(Identifier.fromNamespaceAndPath("minecraft", "mineable/expansion_pickaxe"));

		private static TagKey<Block> tag(String name) {
			return BlockTags.create(PECore.rl(name));
		}

		private static TagKey<Block> commonTag(String name) {
			return BlockTags.create(Identifier.fromNamespaceAndPath("c", name));
		}
	}

	public static class Entities {

		private Entities() {
		}

		/**
		 * Entity types added here will not be repelled by the Swiftwolf Rending Gale's repel effect.
		 */
		public static final TagKey<EntityType<?>> BLACKLIST_SWRG = tag("blacklist/swrg");
		/**
		 * Entity types added here will not be repelled by the Interdiction Torch.
		 */
		public static final TagKey<EntityType<?>> BLACKLIST_INTERDICTION = tag("blacklist/interdiction");
		/**
		 * Philosopher stone's (peaceful) entity randomizer list (Only supports Mob Entities)
		 */
		public static final TagKey<EntityType<?>> RANDOMIZER_PEACEFUL = tag("randomizer/peaceful");
		/**
		 * Philosopher stone's (hostile) entity randomizer list (Only supports Mob Entities)
		 */
		public static final TagKey<EntityType<?>> RANDOMIZER_HOSTILE = tag("randomizer/hostile");

		private static TagKey<EntityType<?>> tag(String name) {
			return TagKey.create(Registries.ENTITY_TYPE, PECore.rl(name));
		}
	}

	public static class BlockEntities {

		private BlockEntities() {
		}

		/**
		 * Block Entity Types added will not receive extra ticks from the Watch of Flowing Time
		 */
		public static final TagKey<BlockEntityType<?>> BLACKLIST_TIME_WATCH = tag("blacklist/time_watch");

		private static TagKey<BlockEntityType<?>> tag(String name) {
			return TagKey.create(Registries.BLOCK_ENTITY_TYPE, PECore.rl(name));
		}
	}

	public static class Potions {

		private Potions() {
		}

		/**
		 * Items that contain potions in this tag won't be listed by the dump missing emc command even if they are missing emc.
		 */
		public static final TagKey<Potion> IGNORE_MISSING_EMC = tag("ignore_missing_emc");

		private static TagKey<Potion> tag(String name) {
			return TagKey.create(Registries.POTION, PECore.rl(name));
		}
	}
}