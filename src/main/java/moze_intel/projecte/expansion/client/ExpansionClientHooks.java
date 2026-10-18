package moze_intel.projecte.expansion.client;

import java.util.List;
import moze_intel.projecte.expansion.capability.CapabilityAlchemicalBookLocations.TeleportLocation;
import moze_intel.projecte.expansion.gui.GUIAlchemicalBook;
import moze_intel.projecte.expansion.util.ClientSideHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Client side installation of the hooks that the (common) expansion code needs.
 * <p>
 * This is the only place that is allowed to touch client only game classes on behalf of the addon's common code: the
 * addon screens need a local player and a {@link Screen}, and referencing them from a class that a dedicated server
 * loads makes the server fail to start.
 */
public final class ExpansionClientHooks {

	private ExpansionClientHooks() {
	}

	/**
	 * Installs the client implementations of the addon's client hooks. Called from ProjectE's client setup.
	 */
	public static void install() {
		HitDirectionSource.setSource(ExpansionClientHooks::lookDirection);
		ClientSideHandler.setScreen(new ExpansionClientHooks.ExpansionAlchemicalBookScreen());
	}

	@Nullable
	private static Direction lookDirection() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.hitResult instanceof BlockHitResult blockHit) {
			return blockHit.getDirection();
		}
		return null;
	}

	private static final class ExpansionAlchemicalBookScreen implements ClientSideHandler.AlchemicalBookScreen {

		@Override
		public void open(InteractionHand hand, List<TeleportLocation> locations, boolean canEdit) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) {
				return;
			}
			//26.3: the open screen lives on Gui, and it is a screen() accessor rather than a field
			Minecraft.getInstance().gui.setScreen(new GUIAlchemicalBook(player, hand, locations, canEdit));
		}

		@Override
		public void sync(List<TeleportLocation> locations, boolean canEdit) {
			Screen current = Minecraft.getInstance().gui.screen();
			if (current instanceof GUIAlchemicalBook gui) {
				gui.setLocations(locations, canEdit);
			}
		}
	}
}
