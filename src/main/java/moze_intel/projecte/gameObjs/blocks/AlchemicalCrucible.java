package moze_intel.projecte.gameObjs.blocks;

import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalCrucibleBlockEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The controller of the Alchemical Crucible, where the Magnum Opus takes place. See {@link AlchemicalCrucibleBlockEntity}.
 */
public class AlchemicalCrucible extends CruciblePart implements PEEntityBlock<AlchemicalCrucibleBlockEntity> {

	public AlchemicalCrucible(Properties props) {
		super(props, Block.box(0, 0, 0, 16, 13, 16));
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
			@NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			//The client does not know the rituals, the server decides what happens with the item
			return InteractionResult.SUCCESS;
		}
		AlchemicalCrucibleBlockEntity core = WorldHelper.getBlockEntity(AlchemicalCrucibleBlockEntity.class, level, pos, true);
		if (core == null) {
			return InteractionResult.FAIL;
		}
		return core.use(player, stack);
	}

	@NotNull
	@Override
	@Deprecated
	protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		AlchemicalCrucibleBlockEntity core = WorldHelper.getBlockEntity(AlchemicalCrucibleBlockEntity.class, level, pos, true);
		return core == null ? InteractionResult.FAIL : core.use(player, ItemStack.EMPTY);
	}

	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flags) {
		tooltip.accept(PELang.TOOLTIP_CRUCIBLE_1.translateColored(ChatFormatting.GRAY));
		tooltip.accept(PELang.TOOLTIP_CRUCIBLE_2.translateColored(ChatFormatting.DARK_GRAY));
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<AlchemicalCrucibleBlockEntity> getType() {
		return PEBlockEntityTypes.ALCHEMICAL_CRUCIBLE;
	}

	@Override
	@Deprecated
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}
}
