package moze_intel.projecte.expansion.integrations.curios;

import java.util.Optional;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import moze_intel.projecte.integration.curios.TransmutationTableCurios;
import net.minecraft.world.entity.player.Player;


/**
 * The "transmutation_tablet" curios slot already exists in ProjectE as
 * {@link TransmutationTableCurios}, so instead of duplicating the Curios API access we
 * simply forward to it. Behaviour is identical to the original ProjectExpansion class.
 */
public class CuriosIntegration {
	public static final String MOD_ID = TransmutationTableCurios.MOD_ID;
	public static final String TABLET_SLOT_ID = TransmutationTableCurios.TABLET_SLOT_ID;

	public static boolean modLoaded() {
		return TransmutationTableCurios.modLoaded();
	}

	public static Optional<IItemHandlerModifiable> getCuriosInventory(Player player) {
		return TransmutationTableCurios.getCuriosInventory(player);
	}
}
