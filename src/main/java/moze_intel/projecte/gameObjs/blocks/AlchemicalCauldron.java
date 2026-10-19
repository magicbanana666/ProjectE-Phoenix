package moze_intel.projecte.gameObjs.blocks;

import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalCauldronBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Alchemical Cauldron, where the three principles are refined and souls are brewed into Red Water. See
 * {@link AlchemicalCauldronBlockEntity}.
 */
public class AlchemicalCauldron extends Block implements PEEntityBlock<AlchemicalCauldronBlockEntity>, IBlockTooltip {

	//The shape of a vanilla cauldron: open at the top, on four short legs
	private static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(
			Block.box(0, 0, 4, 16, 3, 12),
			Block.box(4, 0, 0, 12, 3, 16),
			Block.box(2, 0, 2, 14, 3, 14),
			Block.box(2, 4, 2, 14, 16, 14)
	), BooleanOp.ONLY_FIRST);

	public AlchemicalCauldron(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	@Deprecated
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	@Deprecated
	public boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity by, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, by, stack);
		AlchemicalCauldronBlockEntity cauldron = WorldHelper.getBlockEntity(AlchemicalCauldronBlockEntity.class, level, pos, true);
		if (cauldron != null && by instanceof Player player) {
			cauldron.setOwner(player.getUUID());
		}
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
			@NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			//The server decides whether the item goes in
			return InteractionResult.SUCCESS;
		}
		AlchemicalCauldronBlockEntity cauldron = WorldHelper.getBlockEntity(AlchemicalCauldronBlockEntity.class, level, pos, true);
		return cauldron == null ? InteractionResult.FAIL : cauldron.use(player, hand, stack);
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		AlchemicalCauldronBlockEntity cauldron = WorldHelper.getBlockEntity(AlchemicalCauldronBlockEntity.class, level, pos, true);
		return cauldron == null ? InteractionResult.FAIL : cauldron.use(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flags) {
		tooltip.accept(PELang.TOOLTIP_CAULDRON.translateColored(ChatFormatting.GRAY));
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<AlchemicalCauldronBlockEntity> getType() {
		return PEBlockEntityTypes.ALCHEMICAL_CAULDRON;
	}

	@Override
	@Deprecated
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}
}
