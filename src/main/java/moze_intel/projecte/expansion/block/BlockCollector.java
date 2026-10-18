package moze_intel.projecte.expansion.block;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.expansion.block.entity.BlockEntityCollector;
import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.registries.ExpansionBlockEntityTypes;
import moze_intel.projecte.expansion.util.*;
import moze_intel.projecte.gameObjs.IMatterType;
import moze_intel.projecte.gameObjs.blocks.BlockDirection;
import moze_intel.projecte.gameObjs.blocks.IBlockTooltip;
import moze_intel.projecte.gameObjs.blocks.IMatterBlock;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class BlockCollector extends BlockDirection implements IHasMatter, EntityBlock, IMatterBlock, IBlockTooltip {
	private final Matter matter;

	public BlockCollector(BlockBehaviour.Properties properties, Matter matter) {
		super(properties);
		this.matter = matter;
	}

	public static BlockBehaviour.Properties getProperties(BlockBehaviour.Properties properties, Matter matter) {
		return properties.strength(getDestroyTime(matter), getExplosionResistance(matter)).requiresCorrectToolForDrops().lightLevel((state) -> Math.min(matter.ordinal(), 15));
	}

	private static float getDestroyTime(Matter matter) {
		return switch (matter) {
			case BASIC -> 0.3F;
			case DARK -> 1_000_000;
			default -> 2_000_000;
		};
	}

	private static float getExplosionResistance(Matter matter) {
		return switch (matter) {
			case BASIC -> 0.9F;
			case DARK -> 3_000_000;
			default -> 6_000_000;
		};
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityCollector(pos, state);
	}

	@Override
	public Matter getMatter() {
		return matter;
	}

	@Override
	public IMatterType getMatterType() {
		return Util.getMatterForProjectE(getMatter());
	}

	//26.3 removed Block#appendHoverText, the tooltip lines are served through IBlockTooltip and PEBlockItem
	@Override
	public void appendBlockTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		tooltip.accept(Lang.Blocks.COLLECTOR_TOOLTIP.translateColored(ChatFormatting.GRAY));
		tooltip.accept(Lang.Blocks.COLLECTOR_EMC.translateColored(ChatFormatting.GRAY, EMCFormat.getComponent(getMatter().getCollectorOutputForTicks(Config.server.tickDelay.get())).setStyle(ColorStyle.GREEN)));
		if(stack.getCount() > 1) {
			tooltip.accept(Lang.Blocks.COLLECTOR_STACK_EMC.translateColored(ChatFormatting.GRAY, EMCFormat.getComponent(getMatter().getCollectorOutputForTicks(Config.server.tickDelay.get()).multiply(BigDecimal.valueOf(stack.getCount()))).setStyle(ColorStyle.GREEN)));
		}
		tooltip.accept(Lang.Blocks.COLLECTOR_MAX_STORAGE.translateColored(ChatFormatting.GRAY, EMCFormat.getComponent(Fuel.getCollectorEMCLimit(getMatter())).setStyle(ColorStyle.GREEN)));
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type == ExpansionBlockEntityTypes.COLLECTOR.get() && !level.isClientSide()) return BlockEntityCollector::tickServer;
		return null;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockEntityCollector collector = WorldHelper.getBlockEntity(BlockEntityCollector.class, level, pos);
		if (collector == null) return InteractionResult.FAIL;
		player.openMenu(collector, pos);

		return InteractionResult.CONSUME;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		BlockEntityCollector collector = WorldHelper.getBlockEntity(BlockEntityCollector.class, level, pos, true);
		if (collector == null) {
			//If something went wrong fallback to default implementation
			return super.getAnalogOutputSignal(state, level, pos, direction);
		}
		ResourceHandler<ItemResource> handler = WorldHelper.getCapability(level, Capabilities.Item.BLOCK, pos, state, collector, Direction.UP);
		if (handler == null) {
			//If something went wrong fallback to default implementation
			return super.getAnalogOutputSignal(state, level, pos, direction);
		}
		ItemStack charging = ItemUtil.getStack(handler, BlockEntityCollector.UPGRADING_SLOT);
		if (charging.isEmpty()) {
			return MathUtils.scaleToRedstone(collector.getStoredEmc(), collector.getMaximumEmc());
		}
		IItemEmcHolder emcHolder = charging.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
		if (emcHolder != null) {
			return MathUtils.scaleToRedstone(emcHolder.getStoredEmc(charging), emcHolder.getMaximumEmc(charging));
		}
		return MathUtils.scaleToRedstone(collector.getStoredEmc(), collector.getEmcToNextGoal());
	}

	@Override
	public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
		return matter.mapColor == null ? super.getMapColor(state, level, pos, defaultColor) : matter.mapColor.get();
	}

}
