package moze_intel.projecte.gameObjs;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Which items of the mod a player has held at some point. The Alchemist's Codex keeps the entries about anything else locked,
 * so the book fills in as the player gets further. Kept on the server and sent to the player it belongs to.
 */
public final class CodexDiscoveries {

	private static final int SCAN_INTERVAL = 40;

	private CodexDiscoveries() {
	}

	public record Discovered(Set<Identifier> items) {

		public static final Discovered NONE = new Discovered(Set.of());
		public static final Codec<Discovered> CODEC = Identifier.CODEC.listOf().xmap(list -> new Discovered(Set.copyOf(list)), discovered -> discovered.items().stream().toList());
		public static final StreamCodec<RegistryFriendlyByteBuf, Discovered> STREAM_CODEC = Identifier.STREAM_CODEC.apply(ByteBufCodecs.<io.netty.buffer.ByteBuf, Identifier>list())
				.<Discovered>map(list -> new Discovered(Set.copyOf(list)), discovered -> discovered.items().stream().toList()).cast();

		public boolean contains(Identifier item) {
			return items.contains(item);
		}
	}

	/**
	 * Every two seconds, adds the items of the mod in the player's inventory to what they have discovered.
	 */
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SCAN_INTERVAL != 0) {
			return;
		}
		Discovered discovered = player.getData(PEAttachmentTypes.CODEX_DISCOVERED);
		Set<Identifier> found = null;
		for (ItemStack stack : player.getInventory()) {
			if (stack.isEmpty()) {
				continue;
			}
			Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
			if (id.getNamespace().equals(PECore.MODID) && !discovered.contains(id)) {
				if (found == null) {
					found = new HashSet<>(discovered.items());
				}
				found.add(id);
			}
		}
		if (found != null) {
			//Setting the data sends it to the player
			player.setData(PEAttachmentTypes.CODEX_DISCOVERED, new Discovered(Set.copyOf(found)));
		}
	}
}
