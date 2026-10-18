package moze_intel.projecte.expansion.block;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import moze_intel.projecte.expansion.block.entity.BlockEntityAdvancedAlchemicalChest;
import moze_intel.projecte.expansion.gui.container.ContainerAdvancedAlchemicalChest;
import moze_intel.projecte.expansion.registries.ExpansionBlockEntityTypes;
import moze_intel.projecte.expansion.util.IHasColor;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.gameObjs.blocks.IBlockTooltip;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

// Many methods lovingly borrowed (stolen) from ProjectE
// https://github.com/sinkillerj/ProjectE/blob/mc1.18.x/src/main/java/moze_intel/projecte/gameObjs/blocks/AlchemicalChest.java
public class BlockAdvancedAlchemicalChest extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock, IHasColor, IBlockTooltip {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
	private final DyeColor color;

	public BlockAdvancedAlchemicalChest(BlockBehaviour.Properties properties, DyeColor color) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
		this.color = color;
	}

	public static BlockBehaviour.Properties getProperties(BlockBehaviour.Properties properties) {
		return properties.requiresCorrectToolForDrops().strength(10, 3_600_000).lightLevel((state) -> 10);
	}

	@Override
	public DyeColor getColor() {
		return color;
	}

	//26.3 removed Block#appendHoverText, the tooltip lines are served through IBlockTooltip and PEBlockItem
	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		tooltip.accept(Lang.Blocks.ADVANCED_ALCHEMICAL_CHEST_TOOLTIP.translateColored(ChatFormatting.GRAY));
		tooltip.accept(Lang.SEE_WIKI.translateColored(ChatFormatting.AQUA));
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		//26.3 RenderShape only has INVISIBLE and MODEL, the animated shape is selected by the block entity renderer
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityAdvancedAlchemicalChest(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockEntityAdvancedAlchemicalChest blockEntity = WorldHelper.getBlockEntity(BlockEntityAdvancedAlchemicalChest.class, level, pos);
		if (blockEntity == null) return InteractionResult.FAIL;
		// a chest placed without an owner (commands, block placers, structures) has no bag to show
		if (blockEntity.getBag() == null) return InteractionResult.FAIL;

		InteractionHand hand = player.getUsedItemHand();
		player.openMenu(new ContainerProvider(blockEntity, hand), (buf) -> {
			buf.writeEnum(hand);
			buf.writeByte(player.getInventory().getSelectedSlot());
			buf.writeBoolean(false);
			buf.writeBlockPos(pos);
		});
		player.awardStat(Stats.OPEN_CHEST);
		PiglinAi.angerNearbyPiglins((ServerLevel) level, player, true);

		return InteractionResult.CONSUME;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockEntityAdvancedAlchemicalChest blockEntity = WorldHelper.getBlockEntity(BlockEntityAdvancedAlchemicalChest.class, level, pos);
		if (blockEntity == null) return InteractionResult.FAIL;
		if (player.isCrouching()) {
			PiglinAi.angerNearbyPiglins((ServerLevel) level, player, true);
			return blockEntity.handleItemActivation(player, stack);
		}

		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
		BlockEntityAdvancedAlchemicalChest blockEntity = WorldHelper.getBlockEntity(BlockEntityAdvancedAlchemicalChest.class, level, pos);
		if (blockEntity == null) return;
		blockEntity.handlePlace(livingEntity, stack);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ExpansionBlockEntityTypes.ADVANCED_ALCHEMICAL_CHEST.get() && level.isClientSide()) return BlockEntityAdvancedAlchemicalChest::tickClient;
		return null;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BlockEntityAdvancedAlchemicalChest be) be.recheckOpen();
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		BlockEntityAdvancedAlchemicalChest blockEntity = WorldHelper.getBlockEntity(BlockEntityAdvancedAlchemicalChest.class, level, pos);
		ResourceHandler<ItemResource> handler = WorldHelper.getCapability(level, Capabilities.Item.BLOCK, pos, state, blockEntity, Direction.UP);
		if (handler == null) {
			return super.getAnalogOutputSignal(state, level, pos, direction);
		}

		return ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(handler);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		BlockEntity blockEntity = WorldHelper.getBlockEntity(BlockEntityAdvancedAlchemicalChest.class, level, pos);
		return blockEntity != null && blockEntity.triggerEvent(id, param);
	}

	@Override
	public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
		return MapColor.byId(this.color.getId());
	}

	// graciously borrowed from ProjectE
	// https://github.com/sinkillerj/ProjectE/blob/98aee771bdb09beecf51b5608938d93de6f1afb6/src/main/java/moze_intel/projecte/gameObjs/items/AlchemicalBag.java#L76-L100
	private record ContainerProvider(BlockEntityAdvancedAlchemicalChest blockEntity, InteractionHand hand) implements MenuProvider {
		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
			IItemHandler bag = blockEntity.getBag();
			if (bag == null) throw new NullPointerException("Bag is null");
			IItemHandlerModifiable inv = bag instanceof IItemHandlerModifiable modifiable ? modifiable : null;
			if (inv == null) throw new NullPointerException("Bag is not modifiable");
			return new ContainerAdvancedAlchemicalChest(windowId, playerInventory, hand, inv, playerInventory.getSelectedSlot(), false, blockEntity);
		}

		@Override
		public Component getDisplayName() {
			return Lang.ADVANCED_ALCHEMICAL_CHEST_TITLE.translate();
		}
	}
}
