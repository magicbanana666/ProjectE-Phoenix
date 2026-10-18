package moze_intel.projecte.expansion.block;

import moze_intel.projecte.expansion.block.entity.BlockEntityRelay;
import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.registries.ExpansionBlockEntityTypes;
import moze_intel.projecte.expansion.util.*;
import moze_intel.projecte.gameObjs.IMatterType;
import moze_intel.projecte.gameObjs.blocks.IBlockTooltip;
import moze_intel.projecte.gameObjs.blocks.IMatterBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BlockRelay extends Block implements IHasMatter, EntityBlock, IMatterBlock, IBlockTooltip {
	private final Matter matter;

	public BlockRelay(BlockBehaviour.Properties properties, Matter matter) {
		super(properties);
		this.matter = matter;
	}

	public static BlockBehaviour.Properties getProperties(BlockBehaviour.Properties properties, Matter matter) {
		return properties.strength(getDestroyTime(matter), getExplosionResistance(matter)).requiresCorrectToolForDrops().lightLevel((state) -> Math.min(matter.ordinal(), 15));
	}

	private static float getDestroyTime(Matter matter) {
		return switch (matter) {
			case BASIC -> 10F;
			case DARK -> 1_000_000;
			default -> 2_000_000;
		};
	}

	private static float getExplosionResistance(Matter matter) {
		return switch (matter) {
			case BASIC -> 30F;
			case DARK -> 3_000_000;
			default -> 6_000_000;
		};
	}

	@Override
	public Matter getMatter() {
		return matter;
	}

	@Override
	public IMatterType getMatterType() {
		return Util.getMatterForProjectE(getMatter());
	}


	@org.jetbrains.annotations.Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityRelay(pos, state);
	}

	//26.3 removed Block#appendHoverText, the tooltip lines are served through IBlockTooltip and PEBlockItem
	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		tooltip.accept(Lang.Blocks.RELAY_TOOLTIP.translateColored(ChatFormatting.GRAY));
		tooltip.accept(Lang.Blocks.RELAY_BONUS.translateColored(ChatFormatting.GRAY, EMCFormat.getComponent(getMatter().getRelayBonusForTicks(Config.server.tickDelay.get())).setStyle(ColorStyle.GREEN)));
		tooltip.accept(Lang.Blocks.RELAY_TRANSFER.translateColored(ChatFormatting.GRAY, getMatter().getRelayTransferComponent().setStyle(ColorStyle.GREEN)));
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ExpansionBlockEntityTypes.RELAY.get() && !level.isClientSide()) return BlockEntityRelay::tickServer;
		return null;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
		return matter.mapColor == null ? super.getMapColor(state, level, pos, defaultColor) : matter.mapColor.get();
	}

}
