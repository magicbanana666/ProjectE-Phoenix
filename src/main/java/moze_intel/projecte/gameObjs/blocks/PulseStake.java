package moze_intel.projecte.gameObjs.blocks;

import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.block_entities.PulseStakeBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A stake driven into the Dragon's Pulse. See {@link PulseStakeBlockEntity}.
 */
public class PulseStake extends Block implements PEEntityBlock<PulseStakeBlockEntity>, IBlockTooltip {

	private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 14, 10);

	public PulseStake(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	@Deprecated
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
		return SHAPE;
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
			@NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			return PulseStakeBlockEntity.isFocus(stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		PulseStakeBlockEntity stake = WorldHelper.getBlockEntity(PulseStakeBlockEntity.class, level, pos, true);
		return stake == null ? InteractionResult.PASS : stake.use(player, stack);
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		PulseStakeBlockEntity stake = WorldHelper.getBlockEntity(PulseStakeBlockEntity.class, level, pos, true);
		return stake == null ? InteractionResult.FAIL : stake.use(player, ItemStack.EMPTY);
	}

	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flags) {
		tooltip.accept(PELang.TOOLTIP_PULSE_STAKE.translateColored(ChatFormatting.GRAY));
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<PulseStakeBlockEntity> getType() {
		return PEBlockEntityTypes.PULSE_STAKE;
	}

	@Override
	@Deprecated
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}
}
