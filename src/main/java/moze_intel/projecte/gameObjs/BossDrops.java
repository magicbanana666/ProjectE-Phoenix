package moze_intel.projecte.gameObjs;

import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * The Ender Dragon has no loot table to add its heart to, so it is dropped here. The other boss drops come from the loot
 * modifiers in {@code data/projecte/loot_modifiers}.
 */
public final class BossDrops {

	private BossDrops() {
	}

	public static void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof EnderDragon dragon && dragon.level() instanceof ServerLevel level) {
			//The dragon dies over the void as often as not, so the heart lands on top of the exit portal
			BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.ZERO).above(2);
			ItemEntity heart = new ItemEntity(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, new ItemStack(PEItems.DRAGON_HEART.get()), 0, 0.1, 0);
			heart.setUnlimitedLifetime();
			level.addFreshEntity(heart);
		}
	}
}
