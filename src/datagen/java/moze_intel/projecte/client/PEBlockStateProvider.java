package moze_intel.projecte.client;

import java.util.stream.Stream;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

public class PEBlockStateProvider extends ModelProvider {

	private static final TextureSlot CHEST = TextureSlot.create("chest");
	private static final TextureSlot PEDESTAL = TextureSlot.create("pedestal");

	public PEBlockStateProvider(PackOutput output) {
		super(output, PECore.MODID);
	}

	@Override
	protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		simpleBlocks(blockModels, PEBlocks.ALCHEMICAL_COAL, PEBlocks.MOBIUS_FUEL, PEBlocks.AETERNALIS_FUEL,
				PEBlocks.DARK_MATTER, PEBlocks.RED_MATTER);
		registerTieredOrientable(blockModels, "collectors", PEBlocks.COLLECTOR, PEBlocks.COLLECTOR_MK2,
				PEBlocks.COLLECTOR_MK3);
		registerTieredOrientable(blockModels, "relays", PEBlocks.RELAY, PEBlocks.RELAY_MK2, PEBlocks.RELAY_MK3);
		registerFurnace(blockModels, PEBlocks.DARK_MATTER_FURNACE, "dm", "dark_matter_block");
		registerFurnace(blockModels, PEBlocks.RED_MATTER_FURNACE, "rm", "red_matter_block");
		registerChests(blockModels);
		registerExplosives(blockModels);
		registerInterdictionTorch(blockModels);
		registerPedestal(blockModels);
		registerTransmutationTable(blockModels);
		registerAlchemicalBarrel(blockModels);
		registerInterdictionLantern(blockModels);
	}

	@Override
	protected Stream<? extends Holder<Block>> getKnownBlocks() {
		return Stream.of(
				PEBlocks.ALCHEMICAL_COAL.getBlock(), PEBlocks.MOBIUS_FUEL.getBlock(), PEBlocks.AETERNALIS_FUEL.getBlock(),
				PEBlocks.DARK_MATTER.getBlock(), PEBlocks.RED_MATTER.getBlock(), PEBlocks.COLLECTOR.getBlock(),
				PEBlocks.COLLECTOR_MK2.getBlock(), PEBlocks.COLLECTOR_MK3.getBlock(), PEBlocks.RELAY.getBlock(),
				PEBlocks.RELAY_MK2.getBlock(), PEBlocks.RELAY_MK3.getBlock(), PEBlocks.DARK_MATTER_FURNACE.getBlock(),
				PEBlocks.RED_MATTER_FURNACE.getBlock(), PEBlocks.ALCHEMICAL_CHEST.getBlock(), PEBlocks.CONDENSER.getBlock(),
				PEBlocks.CONDENSER_MK2.getBlock(), PEBlocks.NOVA_CATALYST.getBlock(), PEBlocks.NOVA_CATACLYSM.getBlock(),
				PEBlocks.INTERDICTION_TORCH.getBlock(), PEBlocks.INTERDICTION_TORCH.getWallBlock(),
				PEBlocks.DARK_MATTER_PEDESTAL.getBlock(), PEBlocks.TRANSMUTATION_TABLE.getBlock(),
				PEBlocks.ALCHEMICAL_BARREL.getBlock(), PEBlocks.INTERDICTION_LANTERN.getBlock())
				.map(Block::builtInRegistryHolder);
	}

	@Override
	protected Stream<? extends Holder<net.minecraft.world.item.Item>> getKnownItems() {
		return Stream.empty();
	}

	@Override
	public String getName() {
		return "ProjectE Block Models";
	}

	private void registerAlchemicalBarrel(BlockModelGenerators models) {
		Identifier closed = model(models, PEBlocks.ALCHEMICAL_BARREL.getBlock(), ModelTemplates.CUBE_BOTTOM_TOP,
				new TextureMapping()
						.put(TextureSlot.SIDE, material("block/alchemical_barrel_side"))
						.put(TextureSlot.BOTTOM, material("block/alchemical_barrel_bottom"))
						.put(TextureSlot.TOP, material("block/alchemical_barrel_top")));
		Identifier open = ModelTemplates.CUBE_BOTTOM_TOP.create(closed.withSuffix("_open"),
				new TextureMapping()
						.put(TextureSlot.SIDE, material("block/alchemical_barrel_side"))
						.put(TextureSlot.BOTTOM, material("block/alchemical_barrel_bottom"))
						.put(TextureSlot.TOP, material("block/alchemical_barrel_top_open")),
				models.modelOutput);
		models.blockStateOutput.accept(MultiVariantGenerator.dispatch(PEBlocks.ALCHEMICAL_BARREL.getBlock(),
				BlockModelGenerators.plainVariant(open))
				.with(modelDispatch(BlockStateProperties.OPEN, false, closed, true, open))
				//The barrel is a DirectionalBlock whose model has its top facing up (like the vanilla barrel), so it
				//needs the column rotations rather than ROTATION_FACING (which assumes a north facing model)
				.with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
	}

	private void registerInterdictionLantern(BlockModelGenerators models) {
		Block block = PEBlocks.INTERDICTION_LANTERN.getBlock();
		TextureMapping mapping = TextureMapping.lantern(block);
		Identifier lantern = ModelTemplates.LANTERN.create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/interdiction_lantern"), mapping, models.modelOutput);
		Identifier hanging = ModelTemplates.HANGING_LANTERN.create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/interdiction_lantern_hanging"), mapping, models.modelOutput);
		models.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(lantern))
				.with(modelDispatch(BlockStateProperties.HANGING, false, lantern, true, hanging)));
	}

	private void registerChests(BlockModelGenerators models) {
		createBaseChestModel(models);
		particleOnly(models, PEBlocks.ALCHEMICAL_CHEST);
		particleOnly(models, PEBlocks.CONDENSER);
		particleOnly(models, PEBlocks.CONDENSER_MK2);
	}

	private void createBaseChestModel(BlockModelGenerators models) {
		ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder()
				.parent(Identifier.withDefaultNamespace("block/block"))
				.requiredTextureSlot(CHEST)
				//The template builder silently drops any mapping for a slot that is not required, and every chest item model
				//inherits from this model, so the particle has to be requested explicitly or the slot is missing entirely.
				.requiredTextureSlot(TextureSlot.PARTICLE)
				//Body
				.element(element -> element.from(1, 0, 1).to(15, 10, 15)
						.face(Direction.NORTH, face -> face.uvs(10.5F, 10.65F, 14, 8.25F).texture(CHEST))
						.face(Direction.EAST, face -> face.uvs(7, 10.65F, 10.5F, 8.25F).texture(CHEST))
						.face(Direction.SOUTH, face -> face.uvs(3.5F, 10.65F, 7, 8.25F).texture(CHEST))
						.face(Direction.WEST, face -> face.uvs(0, 10.7F, 3.5F, 8.3F).texture(CHEST))
						.face(Direction.UP, face -> face.uvs(7, 8.2F, 10.5F, 4.8F).texture(CHEST))
						.face(Direction.DOWN, face -> face.uvs(3.5F, 8.3F, 7, 4.7F).texture(CHEST)))
				//Lid
				.element(element -> element.from(1, 10, 1).to(15, 15, 15)
						.face(Direction.NORTH, face -> face.uvs(10.5F, 4.65F, 14, 3.5F).texture(CHEST))
						.face(Direction.EAST, face -> face.uvs(7, 4.7F, 10.5F, 3.5F).texture(CHEST))
						.face(Direction.SOUTH, face -> face.uvs(3.5F, 4.7F, 7, 3.5F).texture(CHEST))
						.face(Direction.WEST, face -> face.uvs(0, 4.7F, 3.5F, 3.5F).texture(CHEST))
						.face(Direction.UP, face -> face.uvs(7, 3.5F, 10.5F, 0).texture(CHEST))
						.face(Direction.DOWN, face -> face.uvs(3.5F, 3.5F, 7, 0).texture(CHEST)))
				//Top
				.element(element -> element.from(7, 8, 0).to(9, 12, 1)
						.face(Direction.NORTH, face -> face.uvs(0.75F, 1.25F, 0.25F, 0.25F).texture(CHEST))
						.face(Direction.EAST, face -> face.uvs(1.25F, 1.25F, 1.5F, 0.25F).texture(CHEST))
						.face(Direction.SOUTH, face -> face.uvs(0.75F, 1.25F, 1.25F, 0.25F).texture(CHEST))
						.face(Direction.WEST, face -> face.uvs(0, 1.25F, 0.25F, 0.25F).texture(CHEST))
						.face(Direction.UP, face -> face.uvs(0.25F, 0.25F, 0.75F, 0).texture(CHEST))
						.face(Direction.DOWN, face -> face.uvs(0.75F, 0.25F, 1.25F, 0).texture(CHEST)));
		builder.build().create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/base_chest"),
				new TextureMapping().put(CHEST, material("block/alchemical_chest"))
						.put(TextureSlot.PARTICLE, material("block/alchemical_chest")), models.modelOutput);
	}

	private void particleOnly(BlockModelGenerators models, moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?> block) {
		String name = block.getName();
		Identifier model = ModelTemplates.PARTICLE_ONLY.create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/" + name),
				TextureMapping.particle(material("block/" + name)), models.modelOutput);
		simpleBlock(models, block.getBlock(), model);
	}

	private void registerPedestal(BlockModelGenerators models) {
		Material darkMatter = material("block/dark_matter_block");
		Identifier model = ExtendedModelTemplateBuilder.builder()
				.parent(Identifier.withDefaultNamespace("block/block"))
				.requiredTextureSlot(PEDESTAL)
				.requiredTextureSlot(TextureSlot.PARTICLE)
				//Base
				.element(element -> element.from(3, 0, 3).to(13, 2, 13)
						.face(Direction.NORTH, face -> face.uvs(3, 0, 10, 2).texture(PEDESTAL))
						.face(Direction.EAST, face -> face.uvs(3, 0, 10, 2).texture(PEDESTAL))
						.face(Direction.SOUTH, face -> face.uvs(3, 0, 10, 2).texture(PEDESTAL))
						.face(Direction.WEST, face -> face.uvs(3, 0, 10, 2).texture(PEDESTAL))
						.face(Direction.UP, face -> face.uvs(3, 3, 10, 10).texture(PEDESTAL))
						.face(Direction.DOWN, face -> face.uvs(3, 3, 10, 10).texture(PEDESTAL).cullface(Direction.DOWN)))
				//Post
				.element(element -> element.from(6, 2, 6).to(10, 9, 10)
						.face(Direction.NORTH, face -> face.uvs(6, 4, 4, 7).texture(PEDESTAL))
						.face(Direction.EAST, face -> face.uvs(6, 4, 4, 7).texture(PEDESTAL))
						.face(Direction.SOUTH, face -> face.uvs(6, 4, 4, 7).texture(PEDESTAL))
						.face(Direction.WEST, face -> face.uvs(6, 4, 4, 7).texture(PEDESTAL)))
				//Top
				.element(element -> element.from(5, 9, 5).to(11, 10, 11)
						.face(Direction.NORTH, face -> face.uvs(0, 0, 6, 1).texture(PEDESTAL))
						.face(Direction.EAST, face -> face.uvs(0, 0, 6, 1).texture(PEDESTAL))
						.face(Direction.SOUTH, face -> face.uvs(0, 0, 6, 1).texture(PEDESTAL))
						.face(Direction.WEST, face -> face.uvs(0, 0, 6, 1).texture(PEDESTAL))
						.face(Direction.UP, face -> face.uvs(6, 6, 6, 6).texture(PEDESTAL))
						.face(Direction.DOWN, face -> face.uvs(6, 6, 6, 6).texture(PEDESTAL)))
				//The model has to be named after the block, the item definition of a block item points at block/<id>
				.build().create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/dm_pedestal"),
						new TextureMapping().put(PEDESTAL, darkMatter).put(TextureSlot.PARTICLE, darkMatter), models.modelOutput);
		simpleBlock(models, PEBlocks.DARK_MATTER_PEDESTAL.getBlock(), model);
	}

	private void registerTransmutationTable(BlockModelGenerators models) {
		Material top = material("block/transmutation_stone/top");
		Identifier model = ExtendedModelTemplateBuilder.builder()
				.parent(Identifier.withDefaultNamespace("block/block"))
				.requiredTextureSlot(TextureSlot.BOTTOM).requiredTextureSlot(TextureSlot.TOP)
				.requiredTextureSlot(TextureSlot.SIDE).requiredTextureSlot(TextureSlot.PARTICLE)
				.element(element -> element.from(0, 0, 0).to(16, 4, 16)
						.face(Direction.DOWN, face -> face.texture(TextureSlot.BOTTOM).cullface(Direction.DOWN))
						.face(Direction.UP, face -> face.texture(TextureSlot.TOP))
						.face(Direction.NORTH, face -> face.texture(TextureSlot.SIDE).cullface(Direction.NORTH))
						.face(Direction.SOUTH, face -> face.texture(TextureSlot.SIDE).cullface(Direction.SOUTH))
						.face(Direction.WEST, face -> face.texture(TextureSlot.SIDE).cullface(Direction.WEST))
						.face(Direction.EAST, face -> face.texture(TextureSlot.SIDE).cullface(Direction.EAST)))
				.build().create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/transmutation_table"),
						new TextureMapping()
								.put(TextureSlot.BOTTOM, material("block/transmutation_stone/bottom"))
								.put(TextureSlot.TOP, top)
								.put(TextureSlot.SIDE, material("block/transmutation_stone/side"))
								.put(TextureSlot.PARTICLE, top), models.modelOutput);
		models.blockStateOutput.accept(MultiVariantGenerator.dispatch(PEBlocks.TRANSMUTATION_TABLE.getBlock(),
				BlockModelGenerators.plainVariant(model)).with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
	}

	private void registerExplosives(BlockModelGenerators models) {
		Identifier catalyst = model(models, PEBlocks.NOVA_CATALYST.getBlock(), ModelTemplates.CUBE_BOTTOM_TOP,
				new TextureMapping()
						.put(TextureSlot.SIDE, material("block/explosives/nova_side"))
						.put(TextureSlot.BOTTOM, material("block/explosives/bottom"))
						.put(TextureSlot.TOP, material("block/explosives/top")));
		simpleBlock(models, PEBlocks.NOVA_CATALYST.getBlock(), catalyst);
		Identifier cataclysm = ModelTemplates.CUBE_BOTTOM_TOP.create(
				Identifier.fromNamespaceAndPath(PECore.MODID, "block/nova_cataclysm"),
				new TextureMapping()
						.put(TextureSlot.SIDE, material("block/explosives/nova1_side"))
						.put(TextureSlot.BOTTOM, material("block/explosives/bottom"))
						.put(TextureSlot.TOP, material("block/explosives/top")), models.modelOutput);
		simpleBlock(models, PEBlocks.NOVA_CATACLYSM.getBlock(), cataclysm);
	}

	private void registerInterdictionTorch(BlockModelGenerators models) {
		Block standing = PEBlocks.INTERDICTION_TORCH.getBlock();
		Identifier torch = ModelTemplates.TORCH.create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/interdiction_torch"),
				TextureMapping.torch(material("block/interdiction_torch").withForceTranslucent(true)), models.modelOutput);
		simpleBlock(models, standing, torch);
		Block wall = PEBlocks.INTERDICTION_TORCH.getWallBlock();
		Identifier wallTorch = ModelTemplates.WALL_TORCH.create(Identifier.fromNamespaceAndPath(PECore.MODID, "block/interdiction_torch_wall"),
				TextureMapping.torch(material("block/interdiction_torch").withForceTranslucent(true)), models.modelOutput);
		models.blockStateOutput.accept(MultiVariantGenerator.dispatch(wall, BlockModelGenerators.plainVariant(wallTorch))
				.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
	}

	private void registerFurnace(BlockModelGenerators models,
			moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?> furnace, String prefix, String sideTexture) {
		Block block = furnace.getBlock();
		Material side = material("block/" + sideTexture);
		Identifier off = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(modelLocation(block),
				orientable(side, material("block/matter_furnace/" + prefix + "_off"), side), models.modelOutput);
		Identifier on = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(modelLocation(block).withSuffix("_on"),
				orientable(side, material("block/matter_furnace/" + prefix + "_on"), side), models.modelOutput);
		models.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(off))
				.with(modelDispatch(AbstractFurnaceBlock.LIT, false, off, true, on))
				.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
	}

	private void registerTieredOrientable(BlockModelGenerators models, String type,
			moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?> base,
			moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?> mk2,
			moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?> mk3) {
		Material side = material("block/" + type + "/other");
		Identifier baseModel = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(modelLocation(base.getBlock()),
				orientable(side, material("block/" + type + "/front"), side, material("block/" + type + "/top_1")), models.modelOutput);
		horizontalBlock(models, base.getBlock(), baseModel);
		for (int tier = 2; tier <= 3; tier++) {
			moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?> block = tier == 2 ? mk2 : mk3;
			Identifier id = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(modelLocation(block.getBlock()),
					orientable(side, material("block/" + type + "/front"), side, material("block/" + type + "/top_" + tier)), models.modelOutput);
			horizontalBlock(models, block.getBlock(), id);
		}
	}

	private void simpleBlocks(BlockModelGenerators models,
			moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?>... blocks) {
		for (moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject<?, ?> block : blocks) {
			Identifier id = TexturedModel.createAllSame(material("block/" + block.getName())).create(block.getBlock(), models.modelOutput);
			simpleBlock(models, block.getBlock(), id);
		}
	}

	private void horizontalBlock(BlockModelGenerators models, Block block, Identifier model) {
		models.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
				.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
	}

	private void simpleBlock(BlockModelGenerators models, Block block, Identifier model) {
		models.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
	}

	private Identifier model(BlockModelGenerators models, Block block, ModelTemplate template, TextureMapping mapping) {
		return template.create(modelLocation(block), mapping, models.modelOutput);
	}

	private static Identifier modelLocation(Block block) {
		return net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation(block);
	}

	private static TextureMapping orientable(Material side, Material front, Material top) {
		return orientable(side, front, side, top);
	}

	private static TextureMapping orientable(Material side, Material front, Material top, Material bottom) {
		return new TextureMapping()
				.put(TextureSlot.SIDE, side).put(TextureSlot.FRONT, front)
				.put(TextureSlot.TOP, top).put(TextureSlot.BOTTOM, bottom);
	}

	private static <T extends Comparable<T>> PropertyDispatch<VariantMutator> modelDispatch(net.minecraft.world.level.block.state.properties.Property<T> property,
			T firstValue, Identifier firstModel, T secondValue, Identifier secondModel) {
		return PropertyDispatch.modify(property)
				.select(firstValue, VariantMutator.MODEL.withValue(firstModel))
				.select(secondValue, VariantMutator.MODEL.withValue(secondModel));
	}

	private static Material material(String path) {
		return new Material(PECore.rl(path));
	}
}
