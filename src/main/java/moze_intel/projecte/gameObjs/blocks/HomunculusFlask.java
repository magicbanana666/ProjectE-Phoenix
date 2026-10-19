package moze_intel.projecte.gameObjs.blocks;

import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.block_entities.HomunculusFlaskBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The flask a homunculus lives in. See {@link HomunculusFlaskBlockEntity}.
 */
public class HomunculusFlask extends Block implements PEEntityBlock<HomunculusFlaskBlockEntity>, IBlockTooltip {

	private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);

	public HomunculusFlask(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	@Deprecated
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity by, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, by, stack);
		HomunculusFlaskBlockEntity flask = WorldHelper.getBlockEntity(HomunculusFlaskBlockEntity.class, level, pos, true);
		if (flask != null && by instanceof Player player) {
			flask.setOwner(player.getUUID());
		}
	}

	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flags) {
		tooltip.accept(PELang.TOOLTIP_HOMUNCULUS_FLASK.translateColored(ChatFormatting.GRAY));
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<HomunculusFlaskBlockEntity> getType() {
		return PEBlockEntityTypes.HOMUNCULUS_FLASK;
	}

	@Override
	@Deprecated
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return triggerBlockEntityEvent(state, level, pos, id, param);
	}
}
