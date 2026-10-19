package moze_intel.projecte.gameObjs.registries;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.EnumCollectorTier;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.EnumRelayTier;
import moze_intel.projecte.gameObjs.blocks.AlchemicalBarrel;
import moze_intel.projecte.gameObjs.blocks.CrucibleArch;
import moze_intel.projecte.gameObjs.blocks.CruciblePart;
import moze_intel.projecte.gameObjs.blocks.AlchemicalCauldron;
import moze_intel.projecte.gameObjs.blocks.AlchemicalCrucible;
import moze_intel.projecte.gameObjs.blocks.AlchemicalChest;
import moze_intel.projecte.gameObjs.blocks.Collector;
import moze_intel.projecte.gameObjs.blocks.Condenser;
import moze_intel.projecte.gameObjs.blocks.CondenserMK2;
import moze_intel.projecte.gameObjs.blocks.InterdictionTorchEntityBlock.InterdictionTorch;
import moze_intel.projecte.gameObjs.blocks.InterdictionTorchEntityBlock.InterdictionTorchWall;
import moze_intel.projecte.gameObjs.blocks.InterdictionLantern;
import moze_intel.projecte.gameObjs.blocks.MatterBlock;
import moze_intel.projecte.gameObjs.blocks.MatterFurnace;
import moze_intel.projecte.gameObjs.blocks.Pedestal;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT.TNTEntityCreator;
import moze_intel.projecte.gameObjs.blocks.Relay;
import moze_intel.projecte.gameObjs.blocks.TransmutationStone;
import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.gameObjs.items.PEBlockItem;
import moze_intel.projecte.gameObjs.registration.impl.BlockDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject.WallOrFloorBlockRegistryObject;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PEBlocks {

	public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(PECore.MODID);

	public static final BlockRegistryObject<AlchemicalChest, BlockItem> ALCHEMICAL_CHEST = BLOCKS.register("alchemical_chest", properties -> new AlchemicalChest(properties.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(10, 3_600_000)));
	public static final BlockRegistryObject<Block, BlockItem> ALCHEMICAL_COAL = registerFuelBlock("alchemical_coal_block", MapColor.COLOR_RED);
	public static final BlockRegistryObject<Block, BlockItem> MOBIUS_FUEL = registerFuelBlock("mobius_fuel_block", MapColor.COLOR_RED);
	public static final BlockRegistryObject<Block, BlockItem> AETERNALIS_FUEL = registerFuelBlock("aeternalis_fuel_block", MapColor.COLOR_LIGHT_GRAY);
	public static final BlockRegistryObject<Collector, BlockItem> COLLECTOR = registerCollector("collector_mk1", EnumCollectorTier.MK1, state -> 7);
	public static final BlockRegistryObject<Collector, BlockItem> COLLECTOR_MK2 = registerCollector("collector_mk2", EnumCollectorTier.MK2, state -> 11);
	public static final BlockRegistryObject<Collector, BlockItem> COLLECTOR_MK3 = registerCollector("collector_mk3", EnumCollectorTier.MK3, state -> 15);
	public static final BlockRegistryObject<Condenser, BlockItem> CONDENSER = registerCondenser("condenser_mk1", Condenser::new, (block, properties) -> PEBlockItem.of(block, properties));
	public static final BlockRegistryObject<CondenserMK2, BlockItem> CONDENSER_MK2 = registerCondenser("condenser_mk2", CondenserMK2::new, (block, properties) -> PEBlockItem.of(block, properties.fireResistant()));
	public static final BlockRegistryObject<AlchemicalCauldron, BlockItem> ALCHEMICAL_CAULDRON = BLOCKS.register("alchemical_cauldron", properties -> new AlchemicalCauldron(properties.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3, 6).noOcclusion()));
	//The Alchemical Crucible, the multiblock of the Magnum Opus
	public static final BlockRegistryObject<AlchemicalCrucible, BlockItem> ALCHEMICAL_CRUCIBLE = BLOCKS.register("alchemical_crucible", properties -> new AlchemicalCrucible(crucibleProperties(properties)), (block, properties) -> PEBlockItem.of(block, properties.fireResistant()));
	public static final BlockRegistryObject<CruciblePart, BlockItem> CRUCIBLE_STEP = registerCruciblePart("crucible_step", Block.box(0, 0, 0, 16, 13, 16));
	public static final BlockRegistryObject<CruciblePart, BlockItem> CRUCIBLE_PLINTH = registerCruciblePart("crucible_plinth", Block.box(0, 0, 0, 16, 16, 16));
	public static final BlockRegistryObject<CruciblePart, BlockItem> CRUCIBLE_DAIS = registerCruciblePart("crucible_dais", Block.box(0, 0, 0, 16, 2, 16));
	public static final BlockRegistryObject<CruciblePart, BlockItem> CRUCIBLE_PILLAR = registerCruciblePart("crucible_pillar", Block.box(2, 0, 2, 14, 16, 14));
	public static final BlockRegistryObject<CrucibleArch, BlockItem> CRUCIBLE_ARCH = BLOCKS.register("crucible_arch", properties -> new CrucibleArch(crucibleProperties(properties), Block.box(3, 0, 3, 13, 13, 13)), (block, properties) -> PEBlockItem.of(block, properties));
	public static final BlockRegistryObject<CruciblePart, BlockItem> CRUCIBLE_APEX = registerCruciblePart("crucible_apex", Block.box(3, 0, 3, 13, 16, 13));
	public static final BlockRegistryObject<Pedestal, BlockItem> DARK_MATTER_PEDESTAL = BLOCKS.register("dm_pedestal", properties -> new Pedestal(properties.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1_000_000, 3_000_000).lightLevel(state -> 12)), (block, properties) -> PEBlockItem.of(block, properties.fireResistant()));
	public static final BlockRegistryObject<MatterFurnace, BlockItem> DARK_MATTER_FURNACE = registerFurnace("dm_furnace", EnumMatterType.DARK_MATTER, 1_000_000, 3_000_000);
	public static final BlockRegistryObject<MatterFurnace, BlockItem> RED_MATTER_FURNACE = registerFurnace("rm_furnace", EnumMatterType.RED_MATTER, 2_000_000, 6_000_000);
	public static final BlockRegistryObject<MatterBlock, BlockItem> DARK_MATTER = registerMatterBlock("dark_matter_block", EnumMatterType.DARK_MATTER, 1_000_000, 3_000_000);
	public static final BlockRegistryObject<MatterBlock, BlockItem> RED_MATTER = registerMatterBlock("red_matter_block", EnumMatterType.RED_MATTER, 2_000_000, 6_000_000);
	public static final WallOrFloorBlockRegistryObject<InterdictionTorch, InterdictionTorchWall, StandingAndWallBlockItem> INTERDICTION_TORCH = BLOCKS.registerWallOrFloorItem("interdiction_torch", InterdictionTorch::new, InterdictionTorchWall::new, () -> BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).noCollision().instabreak().strength(0).lightLevel(state -> 14).randomTicks());
	public static final BlockRegistryObject<ProjectETNT, BlockItem> NOVA_CATALYST = registerExplosive("nova_catalyst", EntityNovaCatalystPrimed::new);
	public static final BlockRegistryObject<ProjectETNT, BlockItem> NOVA_CATACLYSM = registerExplosive("nova_cataclysm", EntityNovaCataclysmPrimed::new);
	public static final BlockRegistryObject<TransmutationStone, BlockItem> TRANSMUTATION_TABLE = BLOCKS.register("transmutation_table", properties -> new TransmutationStone(properties.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(10, 30)));
	public static final BlockRegistryObject<Relay, BlockItem> RELAY = registerRelay("relay_mk1", EnumRelayTier.MK1, state -> 7);
	public static final BlockRegistryObject<Relay, BlockItem> RELAY_MK2 = registerRelay("relay_mk2", EnumRelayTier.MK2, state -> 11);
	public static final BlockRegistryObject<Relay, BlockItem> RELAY_MK3 = registerRelay("relay_mk3", EnumRelayTier.MK3, state -> 15);

	public static final BlockRegistryObject<AlchemicalBarrel, BlockItem> ALCHEMICAL_BARREL = BLOCKS.register("alchemical_barrel",
			properties -> new AlchemicalBarrel(properties
					.mapColor(MapColor.STONE)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(10, 3_600_000)
			));
	public static final BlockRegistryObject<InterdictionLantern, BlockItem> INTERDICTION_LANTERN = BLOCKS.register("interdiction_lantern",
			properties -> new InterdictionLantern(properties
					.mapColor(MapColor.METAL)
					.forceSolidOn()
					.requiresCorrectToolForDrops()
					.strength(3.5F)
					.sound(SoundType.LANTERN)
					.lightLevel(state -> 15)
					.noOcclusion()
					.pushReaction(PushReaction.DESTROY)
			));

	private static BlockBehaviour.Properties crucibleProperties(BlockBehaviour.Properties properties) {
		return properties.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
				.strength(10, 1_200).noOcclusion().lightLevel(CruciblePart::getLight);
	}

	private static BlockRegistryObject<CruciblePart, BlockItem> registerCruciblePart(String name, VoxelShape shape) {
		return BLOCKS.register(name, properties -> new CruciblePart(crucibleProperties(properties), shape), (block, properties) -> PEBlockItem.of(block, properties));
	}

	private static BlockRegistryObject<Block, BlockItem> registerFuelBlock(String name, MapColor mapColor) {
		return BLOCKS.register(name, properties -> new Block(properties.mapColor(mapColor).instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops().strength(0.5F, 1.5F)));
	}

	private static BlockRegistryObject<Collector, BlockItem> registerCollector(String name, EnumCollectorTier collectorTier, ToIntFunction<BlockState> lightLevel) {
		return BLOCKS.register(name, properties -> new Collector(collectorTier, properties.mapColor(MapColor.SAND)
				.instrument(NoteBlockInstrument.PLING) .sound(SoundType.GLASS).requiresCorrectToolForDrops().strength(0.3F, 0.9F)
				.lightLevel(lightLevel)));
	}

	private static <CONDENSER extends Condenser> BlockRegistryObject<CONDENSER, BlockItem> registerCondenser(String name,
			Function<BlockBehaviour.Properties, CONDENSER> condenserFunction, BiFunction<CONDENSER, Item.Properties, BlockItem> itemCreator) {
		return BLOCKS.register(name, properties -> condenserFunction.apply(properties.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops().strength(10, 3_600_000)), itemCreator);
	}

	private static BlockRegistryObject<Relay, BlockItem> registerRelay(String name, EnumRelayTier relayTier, ToIntFunction<BlockState> lightLevel) {
		return BLOCKS.register(name, properties -> new Relay(relayTier, properties.mapColor(MapColor.COLOR_BLACK)
				.instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
				.strength(10, 30).lightLevel(lightLevel)));
	}

	private static BlockRegistryObject<ProjectETNT, BlockItem> registerExplosive(String name, TNTEntityCreator tntEntityCreator) {
		return BLOCKS.register(name, properties -> new ProjectETNT(properties.mapColor(MapColor.FIRE).strength(0).instabreak()
				.sound(SoundType.GRASS).ignitedByLava().isRedstoneConductor((state, getter, pos) -> false), tntEntityCreator));
	}

	private static BlockRegistryObject<MatterFurnace, BlockItem> registerFurnace(String name, EnumMatterType matterType, float hardness, float resistance) {
		return BLOCKS.register(name, properties -> new MatterFurnace(properties.requiresCorrectToolForDrops().strength(hardness, resistance)
				.mapColor(matterType.getMapColor()).instrument(NoteBlockInstrument.BASEDRUM).lightLevel(state -> 14), matterType),
				(block, properties) -> PEBlockItem.of(block, properties.fireResistant()));
	}

	private static BlockRegistryObject<MatterBlock, BlockItem> registerMatterBlock(String name, EnumMatterType matterType, float hardness, float resistance) {
		return BLOCKS.register(name, properties -> new MatterBlock(properties.requiresCorrectToolForDrops().strength(hardness, resistance)
				.mapColor(matterType.getMapColor()).instrument(NoteBlockInstrument.BASEDRUM).lightLevel(state -> 14), matterType),
				(block, properties) -> PEBlockItem.of(block, properties.fireResistant()));
	}
}