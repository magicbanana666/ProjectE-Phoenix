package moze_intel.projecte.gameObjs;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.config.ServerConfig;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Comprehension, the first of the three steps: before a valuable item can be learned it has to be understood, by burning
 * several of it. Cheap items are learned on the first burn as always. How valuable is valuable, and how many burns it takes,
 * is up to the server config.
 * <p>
 * Only burning counts. Putting an item in the lock or input slot of a table teaches it only once it is understood.
 */
public final class Comprehension {

	private Comprehension() {
	}

	/**
	 * @return whether the item has to be studied before it can be learned
	 */
	public static boolean needsStudy(ItemInfo info) {
		ServerConfig.Comprehension settings = ProjectEConfig.server.comprehension;
		return settings.enabled.get() && settings.burnsNeeded.get() > 1 && IEMCProxy.INSTANCE.getValue(info) > settings.instantBelow.get();
	}

	/**
	 * @return whether the player understands the item well enough to learn it
	 */
	public static boolean isUnderstood(Player player, ItemInfo info) {
		return !needsStudy(info) || player.getData(PEAttachmentTypes.COMPREHENSION).get(info) >= ProjectEConfig.server.comprehension.burnsNeeded.get();
	}

	/**
	 * Counts burned items towards understanding them.
	 *
	 * @return whether the item is understood now, and so may be learned
	 */
	public static boolean study(ServerPlayer player, ItemInfo info, int burned) {
		if (!needsStudy(info)) {
			return true;
		}
		int needed = ProjectEConfig.server.comprehension.burnsNeeded.get();
		Progress progress = player.getData(PEAttachmentTypes.COMPREHENSION);
		int studied = progress.add(info, burned);
		if (studied >= needed) {
			return true;
		}
		player.sendOverlayMessage(PELang.COMPREHENSION_STUDYING.translateColored(ChatFormatting.GRAY, info.createStack().getHoverName(), studied, needed));
		return false;
	}

	/**
	 * How many of each item a player has burned without learning it yet. Items are counted by kind, whatever their data.
	 */
	public static class Progress {

		public static final Codec<Progress> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.INT).xmap(Progress::new, progress -> progress.burned);

		private final Map<Identifier, Integer> burned;

		public Progress() {
			this(Map.of());
		}

		private Progress(Map<Identifier, Integer> burned) {
			this.burned = new HashMap<>(burned);
		}

		public int get(ItemInfo info) {
			return burned.getOrDefault(BuiltInRegistries.ITEM.getKey(info.getItem().value()), 0);
		}

		private int add(ItemInfo info, int amount) {
			return burned.merge(BuiltInRegistries.ITEM.getKey(info.getItem().value()), amount, Integer::sum);
		}

		public Progress copy(net.neoforged.neoforge.attachment.IAttachmentHolder holder, net.minecraft.core.HolderLookup.Provider registries) {
			return new Progress(burned);
		}
	}
}
