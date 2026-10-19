package moze_intel.projecte.gameObjs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.math.BigInteger;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The Phoenix, the sign of the finished work: rebirth from the ashes. It is not a creature but what the alchemist becomes after
 * taking the Phoenix Ash, for good.
 * <ul>
 *     <li>Rebirth: death burns them to ash, and they rise again with full health. Then the fire needs half an hour to return.</li>
 *     <li>Ember Wings: flight, for a little EMC every second in the air.</li>
 *     <li>Child of Fire: fire and lava do not burn them, and standing in fire heals them.</li>
 *     <li>Restoration: becoming a Phoenix gives back the hearts the Gate took.</li>
 * </ul>
 */
public final class Phoenix {

	public static final long REBIRTH_COOLDOWN = 30 * 60 * 20;
	/**
	 * The EMC the wings burn for every second in the air.
	 */
	private static final long FLIGHT_COST = 64;
	private static final Identifier WINGS = PECore.rl("ember_wings");
	private static final AttributeModifier WINGS_MODIFIER = new AttributeModifier(WINGS, 1, AttributeModifier.Operation.ADD_VALUE);

	private Phoenix() {
	}

	/**
	 * @param risen     whether the player has become a Phoenix
	 * @param readyAt   the game time from which the next rebirth is possible
	 */
	public record State(boolean risen, long readyAt) {

		public static final State NONE = new State(false, 0);
		public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("risen", false).forGetter(State::risen),
				Codec.LONG.optionalFieldOf("ready_at", 0L).forGetter(State::readyAt)
		).apply(instance, State::new));
	}

	public static boolean isPhoenix(Player player) {
		return player.getData(PEAttachmentTypes.PHOENIX).risen();
	}

	/**
	 * Makes the player a Phoenix.
	 *
	 * @return false if they already were one
	 */
	public static boolean rise(ServerPlayer player) {
		if (isPhoenix(player)) {
			return false;
		}
		player.setData(PEAttachmentTypes.PHOENIX, new State(true, 0));
		TheGate.liftToll(player);
		ServerLevel level = player.level();
		burst(level, player);
		level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.5F, 0.6F);
		level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
		player.sendSystemMessage(PELang.PHOENIX_RISEN.translateColored(ChatFormatting.GOLD));
		return true;
	}

	private static void burst(ServerLevel level, Player player) {
		level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 120, 0.6, 1, 0.6, 0.15);
		level.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 1, player.getZ(), 30, 0.4, 0.6, 0.4, 0);
	}

	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !isPhoenix(player)) {
			return;
		}
		ServerLevel level = player.level();
		State state = player.getData(PEAttachmentTypes.PHOENIX);
		if (level.getGameTime() < state.readyAt()) {
			return;
		}
		event.setCanceled(true);
		player.setHealth(player.getMaxHealth());
		player.removeAllEffects();
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60 * 20));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 1));
		player.setData(PEAttachmentTypes.PHOENIX, new State(true, level.getGameTime() + REBIRTH_COOLDOWN));
		burst(level, player);
		level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
		level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
		player.sendSystemMessage(PELang.PHOENIX_REBIRTH.translateColored(ChatFormatting.GOLD));
	}

	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof Player player && isPhoenix(player) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
			event.setCanceled(true);
		}
	}

	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
			return;
		}
		AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
		if (!isPhoenix(player)) {
			if (flight != null && flight.hasModifier(WINGS)) {
				flight.removeModifier(WINGS);
			}
			return;
		}
		//Child of Fire: fire heals instead of burning
		BlockPos pos = player.blockPosition();
		if (player.isInLava() || player.level().getBlockState(pos).is(BlockTags.FIRE)) {
			player.heal(1);
		}
		//Ember Wings: they burn EMC while in the air, and fold when there is none left
		IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		boolean canPay = player.isCreative() || provider != null && provider.getEmc().compareTo(BigInteger.valueOf(FLIGHT_COST)) >= 0;
		if (flight != null) {
			if (canPay && !flight.hasModifier(WINGS)) {
				flight.addTransientModifier(WINGS_MODIFIER);
			} else if (!canPay && flight.hasModifier(WINGS)) {
				flight.removeModifier(WINGS);
			}
		}
		if (player.getAbilities().flying && !player.isCreative() && provider != null && canPay) {
			provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(FLIGHT_COST)));
			provider.syncEmc(player);
			player.level().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 6, 0.4, 0.2, 0.4, 0.01);
		}
	}
}
