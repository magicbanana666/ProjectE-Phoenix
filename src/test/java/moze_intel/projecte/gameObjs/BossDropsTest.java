package moze_intel.projecte.gameObjs;

import moze_intel.projecte.PECore;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
class BossDropsTest {

	@Test
	@DisplayName("The loot modifiers that add the boss drops and the journal pages load")
	void testLootModifiersLoad(MinecraftServer server) {
		LootModifierManager manager = server.getServerResources().managers().getListener(NeoForgeReloadListeners.LOOT_MODIFIERS_KEY);
		for (String drop : new String[]{"warden_heart", "blighted_soul", "elder_eye", "journal_ancient_city", "journal_stronghold", "journal_end_city", "journal_trial_chambers"}) {
			Assertions.assertNotNull(manager.getModifier(PECore.rl(drop)), () -> "Missing loot modifier " + drop);
		}
	}
}
