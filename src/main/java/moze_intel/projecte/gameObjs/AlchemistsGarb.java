package moze_intel.projecte.gameObjs;

import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * The Alchemist's Garb barely stops a blow, but the full set serves the dark side of the work: an extra soul from every kill
 * inside a circle, chalk that lasts twice as long, and soul fire that does not burn.
 */
public final class AlchemistsGarb {

	private AlchemistsGarb() {
	}

	public static boolean isWearingFullSet(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is(PEItems.ALCHEMISTS_HOOD)
			   && entity.getItemBySlot(EquipmentSlot.CHEST).is(PEItems.ALCHEMISTS_ROBE)
			   && entity.getItemBySlot(EquipmentSlot.LEGS).is(PEItems.ALCHEMISTS_TROUSERS)
			   && entity.getItemBySlot(EquipmentSlot.FEET).is(PEItems.ALCHEMISTS_BOOTS);
	}

	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		LivingEntity entity = event.getEntity();
		if (event.getSource().is(DamageTypeTags.IS_FIRE) && isWearingFullSet(entity) && inSoulFire(entity)) {
			event.setCanceled(true);
			entity.clearFire();
		}
	}

	private static boolean inSoulFire(LivingEntity entity) {
		BlockPos pos = entity.blockPosition();
		for (BlockPos check : new BlockPos[]{pos, pos.below()}) {
			BlockState state = entity.level().getBlockState(check);
			if (state.is(Blocks.SOUL_FIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
				return true;
			}
		}
		return false;
	}
}
