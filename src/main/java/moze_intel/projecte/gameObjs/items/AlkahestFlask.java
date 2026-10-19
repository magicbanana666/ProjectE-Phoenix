package moze_intel.projecte.gameObjs.items;

import java.math.BigInteger;
import java.util.function.Consumer;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * The alkahest, the universal solvent. A drop of it dissolves a block back into the worth it was made of, without a table,
 * though some of that worth is lost in the vapour.
 */
public class AlkahestFlask extends Item {

	/**
	 * How much of the worth of a dissolved block reaches the alchemist.
	 */
	private static final double RETURNED = 0.75;

	public AlkahestFlask(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	public InteractionResult useOn(@NotNull UseOnContext ctx) {
		Level level = ctx.getLevel();
		if (!(ctx.getPlayer() instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (state.getDestroySpeed(level, pos) < 0 || level.getBlockEntity(pos) != null || !PlayerHelper.hasBreakPermission(player, level, pos)) {
			//Unbreakable blocks, and blocks with something inside them, resist it
			return InteractionResult.FAIL;
		}
		long worth = IEMCProxy.INSTANCE.getValue(state.getBlock().asItem());
		IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (worth <= 0 || provider == null) {
			player.sendOverlayMessage(PELang.ALKAHEST_NOTHING.translateColored(ChatFormatting.GRAY));
			return InteractionResult.FAIL;
		}
		level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
		provider.setEmc(provider.getEmc().add(BigInteger.valueOf((long) (worth * RETURNED))));
		provider.syncEmc(player);
		serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.02);
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.6F);
		if (!player.isCreative()) {
			ctx.getItemInHand().hurtAndBreak(1, player, ctx.getHand());
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(PELang.TOOLTIP_ALKAHEST.translateColored(ChatFormatting.GRAY));
	}
}
