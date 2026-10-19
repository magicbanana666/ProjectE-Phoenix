package moze_intel.projecte.gameObjs;

import java.util.UUID;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalCauldronBlockEntity;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Souls of what dies inside the outer circle of an Alchemical Cauldron go into the cauldron.
 * <p>
 * Villagers and wandering traders give the most, and they are missed: every villager nearby remembers who keeps the
 * cauldron, which raises their prices and, after a few, turns the iron golems against them.
 */
public final class CauldronSouls {

	private static final int HUMAN_SOULS = 10;
	private static final int BOSS_SOULS = 50;
	private static final int HOSTILE_SOULS = 2;
	private static final int OTHER_SOULS = 1;
	/**
	 * How much a villager's death in the circle is held against the keeper of the cauldron, by every villager who hears of it.
	 */
	private static final int GOSSIP = 10;
	private static final int GOSSIP_RANGE = 48;

	private CauldronSouls() {
	}

	public static void onLivingDeath(LivingDeathEvent event) {
		LivingEntity victim = event.getEntity();
		if (!(victim.level() instanceof ServerLevel level) || victim instanceof Player) {
			return;
		}
		AlchemicalCauldronBlockEntity cauldron = findCauldron(level, victim);
		if (cauldron == null) {
			return;
		}
		boolean human = victim instanceof AbstractVillager;
		int amount;
		if (human) {
			amount = HUMAN_SOULS;
		} else if (victim.is(Tags.EntityTypes.BOSSES)) {
			amount = BOSS_SOULS;
		} else if (victim instanceof Enemy) {
			amount = HOSTILE_SOULS;
		} else {
			amount = OTHER_SOULS;
		}
		if (!cauldron.addSouls(amount)) {
			return;
		}
		BlockPos pos = cauldron.getBlockPos();
		level.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 6 + amount, 0.3, 0.3, 0.3, 0.02);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 4 + amount, 0.2, 0.1, 0.2, 0.01);
		level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0F, human ? 0.6F : 1.0F);
		UUID owner = cauldron.getOwner();
		if (human && owner != null) {
			for (Villager witness : level.getEntitiesOfClass(Villager.class, new AABB(victim.blockPosition()).inflate(GOSSIP_RANGE))) {
				witness.getGossips().add(owner, GossipType.MAJOR_NEGATIVE, GOSSIP);
			}
		}
	}

	/**
	 * @return the cauldron whose outer circle the entity died in
	 */
	@Nullable
	private static AlchemicalCauldronBlockEntity findCauldron(ServerLevel level, LivingEntity victim) {
		BlockPos at = victim.blockPosition();
		int reach = CauldronRecipe.MAX_CIRCLES;
		for (BlockPos pos : BlockPos.betweenClosed(at.offset(-reach, -2, -reach), at.offset(reach, 1, reach))) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof AlchemicalCauldronBlockEntity cauldron && cauldron.hasCircle(CauldronRecipe.MAX_CIRCLES)) {
				return cauldron;
			}
		}
		return null;
	}
}
