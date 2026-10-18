package moze_intel.projecte.common.tag;

import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class PEBlockTagsProvider extends BlockTagsProvider {

	public PEBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, PECore.MODID);
	}

	@Override
	protected ElementTagAppender<Block> tag(TagKey<Block> tag) {
		return new ElementTagAppender<>(super.tag(tag), block -> block.builtInRegistryHolder().key());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(PETags.Blocks.FARMING_OVERRIDE).add(Blocks.PINK_PETALS);
		ElementTagAppender<Block> blacklistHarvest = tag(PETags.Blocks.BLACKLIST_HARVEST);
		//Add blocks that sometimes return false from isValidBonemealTarget, but that we don't actually want to be broken
		blacklistHarvest.add(
				//If there is no neighboring nylium we don't want to cause the netherrack to be broken
				Blocks.NETHERRACK,
				//If it doesn't have air above it
				Blocks.BAMBOO_SAPLING,
				//If it doesn't have air below it
				Blocks.ROOTED_DIRT,
				//If it has a fluid above it
				Blocks.AZALEA,
				Blocks.FLOWERING_AZALEA,
				//If it doesn't have air
				Blocks.BIG_DRIPLEAF,
				Blocks.BIG_DRIPLEAF_STEM
		);
		ElementTagAppender<Block> overridePlantable = tag(PETags.Blocks.OVERRIDE_PLANTABLE);
		addTags(overridePlantable,
				BlockTags.LEAVES,
				//Note: All vanilla tall flowers are bonemealable, so will get handled before being used by this tag
				// but if a mod adds a tall flower that doesn't inherit the class hierarchy, having this could be useful
				BlockTags.FLOWERS,
				Tags.Blocks.PUMPKINS_NORMAL);
		overridePlantable.add(Blocks.MELON);
		for (Block block : BuiltInRegistries.BLOCK) {
			if (WorldHelper.isPlantableImplementation(block)) {
				overridePlantable.add(block);
			}
			if (WorldHelper.isUnharvestableImplementation(block)) {
				blacklistHarvest.add(block);
			}
		}
		tag(PETags.Blocks.BLACKLIST_TIME_WATCH);
		tag(PETags.Blocks.VEIN_SHOVEL).add(Blocks.CLAY).addTag(Tags.Blocks.GRAVELS);
		//Vanilla/Forge Tags
		tag(Tags.Blocks.BARRELS).add(projecteBlock(PEBlocks.ALCHEMICAL_BARREL));
		tag(Tags.Blocks.CHESTS).add(projecteBlock(PEBlocks.ALCHEMICAL_CHEST));
		tag(Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES).add(
				projecteBlock(PEBlocks.DARK_MATTER_FURNACE), projecteBlock(PEBlocks.RED_MATTER_FURNACE));
		tag(BlockTags.BEACON_BASE_BLOCKS).addTag(PETags.Blocks.EXPANSION_BEACON_BASE_BLOCKS).add(projecteBlock(PEBlocks.DARK_MATTER), projecteBlock(PEBlocks.RED_MATTER));
		tag(BlockTags.GUARDED_BY_PIGLINS).addTag(PETags.Blocks.EXPANSION_GUARDED_BY_PIGLINS).add(
				projecteBlock(PEBlocks.ALCHEMICAL_CHEST), projecteBlock(PEBlocks.ALCHEMICAL_BARREL),
				projecteBlock(PEBlocks.CONDENSER), projecteBlock(PEBlocks.CONDENSER_MK2));
		tag(BlockTags.PIGLIN_REPELLENTS).add(projecteBlock(PEBlocks.INTERDICTION_LANTERN));
		tag(BlockTags.INFINIBURN_OVERWORLD).addTag(PETags.Blocks.EXPANSION_INFINIBURN_OVERWORLD).add(
				projecteBlock(PEBlocks.ALCHEMICAL_COAL), projecteBlock(PEBlocks.MOBIUS_FUEL),
				projecteBlock(PEBlocks.AETERNALIS_FUEL));
		addImmuneBlocks(BlockTags.DRAGON_IMMUNE, PETags.Blocks.EXPANSION_DRAGON_IMMUNE);
		addImmuneBlocks(BlockTags.WITHER_IMMUNE, PETags.Blocks.EXPANSION_WITHER_IMMUNE);

		tag(PETags.Blocks.MINEABLE_WITH_HAMMER);
		tag(PETags.Blocks.MINEABLE_WITH_KATAR);
		tag(PETags.Blocks.MINEABLE_WITH_MORNING_STAR);

		tag(PETags.Blocks.NEEDS_DARK_MATTER_TOOL).addTag(PETags.Blocks.EXPANSION_NEEDS_DARK_MATTER_TOOL).add(
				projecteBlock(PEBlocks.DARK_MATTER), projecteBlock(PEBlocks.DARK_MATTER_FURNACE),
				projecteBlock(PEBlocks.DARK_MATTER_PEDESTAL));
		tag(PETags.Blocks.NEEDS_RED_MATTER_TOOL).addTag(PETags.Blocks.EXPANSION_NEEDS_RED_MATTER_TOOL).add(
				projecteBlock(PEBlocks.RED_MATTER), projecteBlock(PEBlocks.RED_MATTER_FURNACE));
		tag(PETags.Blocks.INCORRECT_FOR_RED_MATTER_TOOL);
		addTags(tag(PETags.Blocks.INCORRECT_FOR_DARK_MATTER_TOOL), PETags.Blocks.NEEDS_RED_MATTER_TOOL);
		addTags(tag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL), PETags.Blocks.NEEDS_DARK_MATTER_TOOL,
				PETags.Blocks.NEEDS_RED_MATTER_TOOL);
		addTags(tag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL), PETags.Blocks.NEEDS_DARK_MATTER_TOOL,
				PETags.Blocks.NEEDS_RED_MATTER_TOOL);
		addTags(tag(BlockTags.INCORRECT_FOR_IRON_TOOL), PETags.Blocks.NEEDS_DARK_MATTER_TOOL,
				PETags.Blocks.NEEDS_RED_MATTER_TOOL);
		addTags(tag(BlockTags.INCORRECT_FOR_STONE_TOOL), PETags.Blocks.NEEDS_DARK_MATTER_TOOL,
				PETags.Blocks.NEEDS_RED_MATTER_TOOL);
		addTags(tag(BlockTags.INCORRECT_FOR_GOLD_TOOL), PETags.Blocks.NEEDS_DARK_MATTER_TOOL,
				PETags.Blocks.NEEDS_RED_MATTER_TOOL);
		addTags(tag(BlockTags.INCORRECT_FOR_WOODEN_TOOL), PETags.Blocks.NEEDS_DARK_MATTER_TOOL,
				PETags.Blocks.NEEDS_RED_MATTER_TOOL);

		tag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(PETags.Blocks.EXPANSION_MINEABLE_WITH_PICKAXE).add(
				projecteBlock(PEBlocks.ALCHEMICAL_CHEST), projecteBlock(PEBlocks.ALCHEMICAL_COAL),
				projecteBlock(PEBlocks.ALCHEMICAL_BARREL), projecteBlock(PEBlocks.MOBIUS_FUEL),
				projecteBlock(PEBlocks.AETERNALIS_FUEL), projecteBlock(PEBlocks.COLLECTOR),
				projecteBlock(PEBlocks.COLLECTOR_MK2), projecteBlock(PEBlocks.COLLECTOR_MK3),
				projecteBlock(PEBlocks.CONDENSER), projecteBlock(PEBlocks.CONDENSER_MK2),
				projecteBlock(PEBlocks.DARK_MATTER_PEDESTAL), projecteBlock(PEBlocks.DARK_MATTER_FURNACE),
				projecteBlock(PEBlocks.RED_MATTER_FURNACE), projecteBlock(PEBlocks.DARK_MATTER),
				projecteBlock(PEBlocks.RED_MATTER), projecteBlock(PEBlocks.TRANSMUTATION_TABLE),
				projecteBlock(PEBlocks.RELAY), projecteBlock(PEBlocks.RELAY_MK2),
				projecteBlock(PEBlocks.RELAY_MK3), projecteBlock(PEBlocks.INTERDICTION_LANTERN));

		//MINEABLE_WITH_PE_SHEARS
		addTags(tag(PETags.Blocks.MINEABLE_WITH_PE_HAMMER), PETags.Blocks.MINEABLE_WITH_HAMMER,
				BlockTags.MINEABLE_WITH_PICKAXE);
		tag(PETags.Blocks.MINEABLE_WITH_PE_SHEARS).add(Blocks.COBWEB);
		tag(PETags.Blocks.MINEABLE_WITH_PE_SWORD).add(Blocks.COBWEB);
		addTags(tag(PETags.Blocks.MINEABLE_WITH_PE_KATAR), PETags.Blocks.MINEABLE_WITH_KATAR,
				BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_HOE, PETags.Blocks.MINEABLE_WITH_PE_SHEARS,
				PETags.Blocks.MINEABLE_WITH_PE_SWORD).add(Blocks.COBWEB);
		addTags(tag(PETags.Blocks.MINEABLE_WITH_PE_MORNING_STAR), PETags.Blocks.MINEABLE_WITH_MORNING_STAR,
				PETags.Blocks.MINEABLE_WITH_PE_HAMMER, BlockTags.MINEABLE_WITH_SHOVEL);

		tag(BlockTags.WALL_POST_OVERRIDE).add(projecteBlock(PEBlocks.INTERDICTION_TORCH));
		addExpansionTags();
	}

	private void addExpansionTags() {
		//Tags of the content that used to live in the ProjectExpansion addon
		tag(PETags.Blocks.EXPANSION_NEEDS_DARK_MATTER_TOOL).add(
				projecteBlock("dark_collector"), projecteBlock("dark_power_flower"), projecteBlock("dark_relay"), projecteBlock("dark_emc_link"));
		tag(PETags.Blocks.EXPANSION_NEEDS_RED_MATTER_TOOL).add(
				projecteBlock("magenta_matter_block"), projecteBlock("pink_matter_block"), projecteBlock("purple_matter_block"),
				projecteBlock("violet_matter_block"), projecteBlock("blue_matter_block"), projecteBlock("cyan_matter_block"),
				projecteBlock("green_matter_block"), projecteBlock("lime_matter_block"), projecteBlock("yellow_matter_block"),
				projecteBlock("orange_matter_block"), projecteBlock("white_matter_block"), projecteBlock("fading_matter_block"),
				projecteBlock("compact_sun"), projecteBlock("red_collector"), projecteBlock("magenta_collector"), projecteBlock("pink_collector"),
				projecteBlock("purple_collector"), projecteBlock("violet_collector"), projecteBlock("blue_collector"), projecteBlock("cyan_collector"),
				projecteBlock("green_collector"), projecteBlock("lime_collector"), projecteBlock("yellow_collector"), projecteBlock("orange_collector"),
				projecteBlock("white_collector"), projecteBlock("fading_collector"), projecteBlock("final_collector"),
				projecteBlock("red_power_flower"), projecteBlock("magenta_power_flower"), projecteBlock("pink_power_flower"),
				projecteBlock("purple_power_flower"), projecteBlock("violet_power_flower"), projecteBlock("blue_power_flower"),
				projecteBlock("cyan_power_flower"), projecteBlock("green_power_flower"), projecteBlock("lime_power_flower"),
				projecteBlock("yellow_power_flower"), projecteBlock("orange_power_flower"), projecteBlock("white_power_flower"),
				projecteBlock("fading_power_flower"), projecteBlock("final_power_flower"), projecteBlock("red_relay"), projecteBlock("magenta_relay"),
				projecteBlock("pink_relay"), projecteBlock("purple_relay"), projecteBlock("violet_relay"), projecteBlock("blue_relay"),
				projecteBlock("cyan_relay"), projecteBlock("green_relay"), projecteBlock("lime_relay"), projecteBlock("yellow_relay"),
				projecteBlock("orange_relay"), projecteBlock("white_relay"), projecteBlock("fading_relay"), projecteBlock("final_relay"),
				projecteBlock("red_emc_link"), projecteBlock("magenta_emc_link"), projecteBlock("pink_emc_link"), projecteBlock("purple_emc_link"),
				projecteBlock("violet_emc_link"), projecteBlock("blue_emc_link"), projecteBlock("cyan_emc_link"), projecteBlock("green_emc_link"),
				projecteBlock("lime_emc_link"), projecteBlock("yellow_emc_link"), projecteBlock("orange_emc_link"), projecteBlock("white_emc_link"),
				projecteBlock("fading_emc_link"), projecteBlock("final_emc_link"));
	}

	//The ids of the addon blocks are resolved through the registry. Resolving them is done hard so a typo fails datagen instead of writing a bad tag.
	private static Block projecteBlock(String name) {
		return Objects.requireNonNull(BuiltInRegistries.BLOCK.getValue(PECore.rl(name)), "Unknown block " + name);
	}

	private void addImmuneBlocks(TagKey<Block> tag, TagKey<Block>... additional) {
		tag(tag).addTags(additional).add(
				projecteBlock(PEBlocks.DARK_MATTER), projecteBlock(PEBlocks.DARK_MATTER_FURNACE),
				projecteBlock(PEBlocks.DARK_MATTER_PEDESTAL), projecteBlock(PEBlocks.RED_MATTER),
				projecteBlock(PEBlocks.RED_MATTER_FURNACE), projecteBlock(PEBlocks.CONDENSER_MK2));
	}

	private static Block projecteBlock(BlockRegistryObject<?, ?> block) {
		return block.getBlock();
	}

	private static ElementTagAppender<Block> addTags(ElementTagAppender<Block> appender, TagKey<Block>... tags) {
		for (TagKey<Block> tag : tags) {
			appender.addTag(tag);
		}
		return appender;
	}
}
