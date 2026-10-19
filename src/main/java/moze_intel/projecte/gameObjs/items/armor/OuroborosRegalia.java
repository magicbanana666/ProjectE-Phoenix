package moze_intel.projecte.gameObjs.items.armor;

import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.registries.PEArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The Ouroboros Regalia: Gem armor bound in the Grand Athanor with the hearts of the great beasts. Every piece keeps all the
 * powers of the Gem piece it was made from, and adds the power of its heart:
 * <ul>
 *     <li>Crown, the Elder Eye: no darkness or mining fatigue, and hostile mobs nearby glow</li>
 *     <li>Mantle, the Warden's Heart: an attacker may be answered with a sonic boom</li>
 *     <li>Greaves, the Blighted Soul: no wither, and blows heal the wearer</li>
 *     <li>Treads, the Dragon Heart: projectiles pass through</li>
 * </ul>
 * The full set keeps the rituals of its wearer from rebounding.
 */
public final class OuroborosRegalia {

	private static final int SIGHT_RANGE = 24;
	private static final float BOOM_CHANCE = 0.25F;
	private static final float BOOM_DAMAGE = 10;
	private static final float LIFE_STEAL = 0.1F;

	private OuroborosRegalia() {
	}

	public static boolean isWearingFullSet(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Crown && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Mantle
			   && entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Greaves && entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof Treads;
	}

	private static void tooltip(Consumer<Component> tooltip, String piece) {
		tooltip.accept(Component.translatable("tooltip.projecte.ouroboros." + piece).withStyle(ChatFormatting.GOLD));
		tooltip.accept(Component.translatable("tooltip.projecte.ouroboros.set").withStyle(ChatFormatting.DARK_GRAY));
	}

	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
			return;
		}
		if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Crown) {
			player.removeEffect(MobEffects.DARKNESS);
			player.removeEffect(MobEffects.MINING_FATIGUE);
			for (LivingEntity hostile : player.level().getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(SIGHT_RANGE),
					entity -> entity instanceof Enemy)) {
				hostile.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, true, false));
			}
		}
		if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Greaves) {
			player.removeEffect(MobEffects.WITHER);
		}
	}

	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		if (event.getSource().is(DamageTypeTags.IS_PROJECTILE) && player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof Treads) {
			event.setCanceled(true);
			return;
		}
		if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Mantle && event.getSource().getEntity() instanceof LivingEntity attacker
			&& attacker != player && player.level() instanceof ServerLevel level && level.getRandom().nextFloat() < BOOM_CHANCE) {
			attacker.hurtServer(level, level.damageSources().sonicBoom(player), BOOM_DAMAGE);
			level.sendParticles(ParticleTypes.SONIC_BOOM, attacker.getX(), attacker.getY() + attacker.getBbHeight() / 2, attacker.getZ(), 1, 0, 0, 0, 0);
			level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8F, 1.2F);
		}
	}

	public static void onDamageDealt(LivingDamageEvent.Post event) {
		if (event.getSource().getEntity() instanceof Player player && player != event.getEntity()
			&& player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Greaves) {
			player.heal(event.getHealthDamage() * LIFE_STEAL);
		}
	}

	public static class Crown extends GemHelmet {

		public Crown(Properties props) {
			super(PEArmorMaterials.OUROBOROS, props);
		}

		@Override
		public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
			OuroborosRegalia.tooltip(tooltip, "crown");
			super.appendHoverText(stack, context, display, tooltip, flags);
		}
	}

	public static class Mantle extends GemChest {

		public Mantle(Properties props) {
			super(PEArmorMaterials.OUROBOROS, props);
		}

		@Override
		public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
			OuroborosRegalia.tooltip(tooltip, "mantle");
			super.appendHoverText(stack, context, display, tooltip, flags);
		}
	}

	public static class Greaves extends GemLegs {

		public Greaves(Properties props) {
			super(PEArmorMaterials.OUROBOROS, props);
		}

		@Override
		public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
			OuroborosRegalia.tooltip(tooltip, "greaves");
			super.appendHoverText(stack, context, display, tooltip, flags);
		}
	}

	public static class Treads extends GemFeet {

		public Treads(Properties props) {
			super(PEArmorMaterials.OUROBOROS, props);
		}

		@Override
		public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flags) {
			OuroborosRegalia.tooltip(tooltip, "treads");
			super.appendHoverText(stack, context, display, tooltip, flags);
		}
	}
}
