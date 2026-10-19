package moze_intel.projecte.gameObjs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.jetbrains.annotations.Nullable;

/**
 * What happens to an alchemist who tries to make a human. The work always fails: the alchemist stands before the Gate and pays
 * its toll, two hearts that do not come back, and something misshapen is left in the crucible. In return they have seen the
 * Truth, and can transmute with their hands, by clapping them, without a stone.
 */
public final class TheGate {

	/**
	 * Health taken by the Gate, two hearts.
	 */
	private static final double TOLL = 4;
	private static final Identifier TOLL_MODIFIER = PECore.rl("truths_toll");

	private TheGate() {
	}

	/**
	 * @param tolls how many times the player paid the toll; every time takes two hearts
	 * @param seen  whether the player has seen the Truth, and can clap
	 */
	public record Truth(int tolls, boolean seen) {

		public static final Truth NONE = new Truth(0, false);
		public static final Codec<Truth> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.optionalFieldOf("tolls", 0).forGetter(Truth::tolls),
				Codec.BOOL.optionalFieldOf("seen", false).forGetter(Truth::seen)
		).apply(instance, Truth::new));
	}

	/**
	 * Opens the Gate before the owner of a forbidden ritual.
	 */
	public static void open(ServerLevel level, BlockPos crucible, @Nullable ServerPlayer owner) {
		double x = crucible.getX() + 0.5;
		double y = crucible.getY() + 1.2;
		double z = crucible.getZ() + 0.5;
		level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 200, 1.5, 1.5, 1.5, 0.1);
		level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 40, 1, 1, 1, 0.05);
		level.playSound(null, crucible, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.5F, 0.4F);
		level.playSound(null, crucible, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 1.0F, 0.5F);
		ItemEntity remains = new ItemEntity(level, x, y, z, new ItemStack(PEItems.MALFORMED_HOMUNCULUS.get()));
		remains.setDefaultPickUpDelay();
		level.addFreshEntity(remains);
		if (owner == null) {
			return;
		}
		Truth truth = owner.getData(PEAttachmentTypes.TRUTH);
		owner.setData(PEAttachmentTypes.TRUTH, new Truth(truth.tolls() + 1, true));
		applyToll(owner);
		owner.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20));
		owner.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 15 * 20));
		owner.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 15 * 20));
		owner.sendSystemMessage(PELang.GATE_1.translateColored(ChatFormatting.DARK_GRAY));
		owner.sendSystemMessage(PELang.GATE_2.translateColored(ChatFormatting.GRAY));
		owner.sendSystemMessage(PELang.GATE_3.translateColored(ChatFormatting.DARK_RED));
	}

	/**
	 * Takes away the hearts the player paid, or gives them back if the toll was lifted.
	 */
	public static void applyToll(Player player) {
		AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth == null) {
			return;
		}
		maxHealth.removeModifier(TOLL_MODIFIER);
		int tolls = player.getData(PEAttachmentTypes.TRUTH).tolls();
		if (tolls > 0) {
			//Never down to nothing, the Gate wants the alchemist to live with it
			double taken = Math.min(TOLL * tolls, maxHealth.getBaseValue() - 2);
			maxHealth.addPermanentModifier(new AttributeModifier(TOLL_MODIFIER, -taken, AttributeModifier.Operation.ADD_VALUE));
		}
		if (player.getHealth() > player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
	}

	/**
	 * Lifts the toll: the hearts come back. The Truth stays seen.
	 */
	public static void liftToll(Player player) {
		player.setData(PEAttachmentTypes.TRUTH, new Truth(0, player.getData(PEAttachmentTypes.TRUTH).seen()));
		applyToll(player);
	}

	public static void onJoinLevel(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
			applyToll(player);
		}
	}

	/**
	 * Clapping: one who has seen the Truth transmutes the block they look at with their hands, as a Philosopher's Stone would.
	 *
	 * @return whether the player clapped
	 */
	public static boolean clap(ServerPlayer player) {
		if (!player.getData(PEAttachmentTypes.TRUTH).seen() || !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
			return false;
		}
		HitResult hit = player.pick(player.blockInteractionRange(), 1, false);
		if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		ServerLevel level = player.level();
		Object2ReferenceMap<BlockPos, BlockState> changes = PhilosophersStone.getChanges(level, blockHit.getBlockPos(), blockHit.getDirection(),
				player.getDirection(), player.isShiftKeyDown(), PhilosophersStone.PhilosophersStoneMode.CUBE, 0);
		if (changes.isEmpty()) {
			return false;
		}
		changes.forEach((pos, state) -> PlayerHelper.checkedReplaceBlock(player, level, pos, state));
		PlayerHelper.swingItem(player, InteractionHand.MAIN_HAND);
		level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.6F);
		level.playSound(null, player.blockPosition(), PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, blockHit.getBlockPos().getX() + 0.5, blockHit.getBlockPos().getY() + 1, blockHit.getBlockPos().getZ() + 0.5, 12, 0.4, 0.2, 0.4, 0.05);
		return true;
	}
}
