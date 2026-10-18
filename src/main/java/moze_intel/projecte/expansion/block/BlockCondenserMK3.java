package moze_intel.projecte.expansion.block;

import org.jetbrains.annotations.NotNull;
import moze_intel.projecte.expansion.block.entity.BlockEntityCondenserMK3;
import moze_intel.projecte.expansion.registries.ExpansionBlockEntityTypes;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.blocks.IBlockTooltip;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
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
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BlockCondenserMK3 extends BaseEntityBlock implements SimpleWaterloggedBlock, IBlockTooltip {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
	public BlockCondenserMK3(Properties properties) {
		super(properties);
		this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(BlockStateProperties.WATERLOGGED, false));
	}

	public static Properties getProperties(Properties properties) {
		return properties.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(10, 3_600_000);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
		builder.add(BlockStateProperties.WATERLOGGED);
	}

	//26.3 removed Block#appendHoverText, the tooltip lines are served through IBlockTooltip and PEBlockItem
	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		tooltip.accept(Lang.Blocks.CONDENSER_MK3_TOOLTIP2.translateColored(ChatFormatting.GRAY));
		tooltip.accept(Lang.SEE_WIKI.translateColored(ChatFormatting.AQUA));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	@Override
	protected RenderShape getRenderShape(BlockState state) {
		//26.3 RenderShape only has INVISIBLE and MODEL, the animated shape is selected by the block entity renderer
		return RenderShape.INVISIBLE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult rtr) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		BlockEntityCondenserMK3 chest = WorldHelper.getBlockEntity(BlockEntityCondenserMK3.class, level, pos, true);
		if (chest != null) {
			player.openMenu(chest, pos);
			player.awardStat(Stats.OPEN_CHEST);
			PiglinAi.angerNearbyPiglins((ServerLevel) level, player, true);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}

	@SuppressWarnings("unused")
	public boolean triggerBlockEntityEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		BlockEntity blockEntity = WorldHelper.getBlockEntity(BlockEntityCondenserMK3.class, level, pos);
		return blockEntity != null && blockEntity.triggerEvent(id, param);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		EmcChestBlockEntity chest = WorldHelper.getBlockEntity(EmcChestBlockEntity.class, level, pos);
		if (chest != null) {
			chest.recheckOpen();
		}
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		ResourceHandler<ItemResource> handler = WorldHelper.getCapability(level, Capabilities.Item.BLOCK, pos, state, null, null);
		return handler == null ? 0 : ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(handler);
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
	protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack stack = player.getMainHandItem();
			if (!stack.isEmpty() && stack.is(PEItems.PHILOSOPHERS_STONE)) {
				level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(FACING, player.getDirection().getOpposite()));
			}
		}
	}

	@Deprecated
	protected BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Deprecated
	protected BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BlockEntityCondenserMK3(blockPos, blockState);
	}

	@javax.annotation.Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ExpansionBlockEntityTypes.CONDENSER_MK3.get()) {
			if (level.isClientSide()) return BlockEntityCondenserMK3::tickClient;
			else return BlockEntityCondenserMK3::tickServer;
		}
		return null;
	}
}
