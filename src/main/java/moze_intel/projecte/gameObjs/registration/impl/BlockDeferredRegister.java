package moze_intel.projecte.gameObjs.registration.impl;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.items.PEBlockItem;
import moze_intel.projecte.gameObjs.registration.DoubleDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject.WallOrFloorBlockRegistryObject;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BlockDeferredRegister extends DoubleDeferredRegister<Block, Item> {

	private final String modid;

	public BlockDeferredRegister(String modid) {
		super(Registries.BLOCK, new ItemDeferredRegister(modid), modid);
		this.modid = modid;
	}

	public BlockRegistryObject<Block, BlockItem> register(String name, BlockBehaviour.Properties properties) {
		return register(name, () -> new Block(properties.setId(blockKey(name))), block -> PEBlockItem.of(block, new Item.Properties().setId(itemKey(name)).useBlockDescriptionPrefix()), BlockRegistryObject::new);
	}

	public <BLOCK extends Block> BlockRegistryObject<BLOCK, BlockItem> register(String name, Function<BlockBehaviour.Properties, BLOCK> blockFactory) {
		return register(name, blockFactory, (block, properties) -> PEBlockItem.of(block, properties));
	}

	public <BLOCK extends Block, WALL_BLOCK extends Block> WallOrFloorBlockRegistryObject<BLOCK, WALL_BLOCK, StandingAndWallBlockItem> registerWallOrFloorItem(String name,
			Function<BlockBehaviour.Properties, BLOCK> blockSupplier, Function<BlockBehaviour.Properties, WALL_BLOCK> wallBlockSupplier,
			Supplier<BlockBehaviour.Properties> propertiesSupplier) {
		//Each block needs its own properties instance, otherwise both blocks would end up sharing the id of whichever block was created last
		DeferredHolder<Block, BLOCK> primaryObject = primaryRegister.register(name, () -> blockSupplier.apply(propertiesSupplier.get().setId(blockKey(name))));
		DeferredHolder<Block, WALL_BLOCK> wallObject = primaryRegister.register("wall_" + name, () -> wallBlockSupplier.apply(propertiesSupplier.get().setId(blockKey("wall_" + name)).overrideLootTable(
				Optional.of(ResourceKey.create(Registries.LOOT_TABLE, primaryObject.getKey().identifier())))));
		return new WallOrFloorBlockRegistryObject<>(primaryObject, wallObject, secondaryRegister.register(name, () -> new StandingAndWallBlockItem(primaryObject.get(), wallObject.get(),
				Direction.DOWN, new Item.Properties().setId(itemKey(name)).useBlockDescriptionPrefix())));
	}

	public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> register(String name, Function<BlockBehaviour.Properties, BLOCK> blockFactory,
			BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
		return register(name, () -> blockFactory.apply(BlockBehaviour.Properties.of().setId(blockKey(name))), block -> itemCreator.apply(block, new Item.Properties().setId(itemKey(name)).useBlockDescriptionPrefix()), BlockRegistryObject::new);
	}

	private ResourceKey<Block> blockKey(String name) {
		return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modid, name));
	}

	private ResourceKey<Item> itemKey(String name) {
		return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modid, name));
	}
}