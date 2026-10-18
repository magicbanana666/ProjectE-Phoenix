package moze_intel.projecte.expansion.util;

import java.util.List;
import moze_intel.projecte.expansion.capability.CapabilityAlchemicalBookLocations.TeleportLocation;
import moze_intel.projecte.expansion.net.packets.to_client.PacketOpenAlchemicalBookGUI;
import moze_intel.projecte.expansion.net.packets.to_client.PacketSyncAlchemicalBookLocations;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

/**
 * The client side half of the alchemical book screens, called from the (common) packets that open and update them.
 * <p>
 * This class deliberately only speaks in common types. The screens themselves need client only classes (the screen, the
 * local player), and a common class referencing those would fail to load on a dedicated server, because the packet that
 * calls it is registered on both sides. The client therefore installs an {@link AlchemicalBookScreen} implementation.
 */
public class ClientSideHandler {

	@Nullable
	private static volatile AlchemicalBookScreen screen;
	@Nullable
	private static volatile Runnable codexScreen;

	private ClientSideHandler() {
	}

	/**
	 * Installs the real screen implementation. Only ever called from client code.
	 */
	public static void setScreen(@Nullable AlchemicalBookScreen newScreen) {
		screen = newScreen;
	}

	/**
	 * Installs what opens the Alchemist's Codex. Only ever called from client code.
	 */
	public static void setCodexScreen(@Nullable Runnable newCodexScreen) {
		codexScreen = newCodexScreen;
	}

	public static void openCodex() {
		Runnable current = codexScreen;
		if (current != null) {
			current.run();
		}
	}

	public static void handleAlchemicalBookOpen(PacketOpenAlchemicalBookGUI packet) {
		AlchemicalBookScreen current = screen;
		if (current != null) {
			current.open(packet.hand(), packet.locations(), packet.canEdit());
		}
	}

	public static void handleSyncAlchemicalBookLocations(PacketSyncAlchemicalBookLocations packet) {
		AlchemicalBookScreen current = screen;
		if (current != null) {
			current.sync(packet.locations(), packet.canEdit());
		}
	}

	@FunctionalInterface
	public interface AlchemicalBookScreen {
		void open(InteractionHand hand, List<TeleportLocation> locations, boolean canEdit);

		/**
		 * Updates the locations of the book screen, if it is currently open
		 */
		default void sync(List<TeleportLocation> locations, boolean canEdit) {
		}
	}
}
