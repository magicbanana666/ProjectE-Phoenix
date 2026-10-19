package moze_intel.projecte.gameObjs.items;

import java.util.function.Consumer;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.gameObjs.Comprehension;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

/**
 * Looking at a block through the monocle for a few seconds counts as studying it, the same as burning one, see
 * {@link Comprehension}. Once it is understood, it is learned, without ever having held it.
 */
public class MonocleOfComprehension extends Item {

	private static final int STUDY_TIME = 3 * 20;

	public MonocleOfComprehension(Properties props) {
		super(props);
	}

	@Override
	public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity user) {
		return STUDY_TIME;
	}

	@NotNull
	@Override
	public ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
		return ItemUseAnimation.SPYGLASS;
	}

	@NotNull
	@Override
	public InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@NotNull
	@Override
	public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
		if (entity instanceof ServerPlayer player) {
			study(player);
			player.getCooldowns().addCooldown(stack, 20);
		}
		return stack;
	}

	private static void study(ServerPlayer player) {
		HitResult hit = player.pick(player.blockInteractionRange() + 3, 1, false);
		if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		Item item = player.level().getBlockState(blockHit.getBlockPos()).getBlock().asItem();
		if (item == Items.AIR) {
			return;
		}
		ItemInfo info = IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromItem(item));
		IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (provider == null || !IEMCProxy.INSTANCE.hasValue(info)) {
			player.sendOverlayMessage(PELang.MONOCLE_NOTHING.translateColored(ChatFormatting.GRAY));
			return;
		} else if (provider.hasKnowledge(info)) {
			player.sendOverlayMessage(PELang.MONOCLE_KNOWN.translateColored(ChatFormatting.GRAY, info.createStack().getHoverName()));
			return;
		}
		if (Comprehension.study(player, info, 1) && !NeoForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, info, info)).isCanceled() && provider.addKnowledge(info)) {
			provider.syncKnowledgeChange(player, info, true);
			player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
			player.sendOverlayMessage(PELang.MONOCLE_LEARNED.translateColored(ChatFormatting.LIGHT_PURPLE, info.createStack().getHoverName()));
		}
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(PELang.TOOLTIP_MONOCLE.translateColored(ChatFormatting.GRAY));
	}
}
