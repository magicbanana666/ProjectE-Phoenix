package moze_intel.projecte.expansion.commands;

import moze_intel.projecte.PECore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionType;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Permission nodes for the {@code /px} command tree.
 * <p>
 * Kept separate from {@code moze_intel.projecte.PEPermissions}, and every node is registered as
 * {@code projecte:expansion.command.*} so that it cannot collide with ProjectE's own
 * {@code projecte:command.*} nodes. Nodes are self registering through {@link EventBusSubscriber}, so nothing
 * outside this package has to call {@link #registerPermissionNodes(PermissionGatherEvent.Nodes)}.
 * <p>
 * 26.3 note: {@code EventBusSubscriber} no longer has a {@code bus} attribute. FML routes each
 * {@code @SubscribeEvent} method to the mod bus or the game bus based on whether its event implements
 * {@code IModBusEvent}, and {@code PermissionGatherEvent} is a plain game bus event, so nothing has to be declared.
 */
@EventBusSubscriber(modid = PECore.MODID)
public class Permissions {
	/**
	 * Prepended to every node name registered here, to keep it disjoint from ProjectE's nodes.
	 */
	public static final String NODE_PREFIX = "expansion.command.";

	private static final List<PermissionNode<?>> NODES = new ArrayList<>();
	//26.1: permission levels are now PermissionCheck objects tested against a PermissionSet
	private static final PermissionNode.PermissionResolver<Boolean> PLAYER_IS_ALL = (player, uuid, context) -> player != null && Commands.LEVEL_ALL.check(player.permissions());
	private static final PermissionNode.PermissionResolver<Boolean> PLAYER_IS_OP = (player, uuid, context) -> player != null && Commands.LEVEL_GAMEMASTERS.check(player.permissions());

	public static final CommandPermissionNode EMC = nodeOpCommand("emc");
	public static final CommandPermissionNode EMC_ADD = nodeOpCommand("emc.add");
	public static final CommandPermissionNode EMC_GET = nodeOpCommand("emc.get");
	public static final CommandPermissionNode EMC_REMOVE = nodeOpCommand("emc.remove");
	public static final CommandPermissionNode EMC_SET = nodeOpCommand("emc.set");
	public static final CommandPermissionNode EMC_TEST = nodeOpCommand("emc.test");
	public static final CommandPermissionNode KNOWLEDGE = nodeOpCommand("knowledge");
	public static final CommandPermissionNode KNOWLEDGE_CLEAR = nodeOpCommand("knowledge.clear");
	public static final CommandPermissionNode KNOWLEDGE_LEARN = nodeOpCommand("knowledge.learn");
	public static final CommandPermissionNode KNOWLEDGE_TEST = nodeOpCommand("knowledge.test");
	public static final CommandPermissionNode KNOWLEDGE_UNLEARN = nodeOpCommand("knowledge.unlearn");
	public static final CommandPermissionNode BOOK_ADD_HAND = nodeOpCommand("book.add.hand");
	public static final CommandPermissionNode BOOK_ADD_PLAYER = nodeOpCommand("book.add.player");
	public static final CommandPermissionNode BOOK_CLEAR_HAND = nodeOpCommand("book.clear.hand");
	public static final CommandPermissionNode BOOK_CLEAR_PLAYER = nodeOpCommand("book.clear.player");
	public static final CommandPermissionNode BOOK_DUMP_HAND = nodeOpCommand("book.dump.hand");
	public static final CommandPermissionNode BOOK_DUMP_PLAYER = nodeOpCommand("book.dump.player");
	public static final CommandPermissionNode BOOK_LIST_HAND = nodeOpCommand("book.list.hand");
	public static final CommandPermissionNode BOOK_LIST_PLAYER = nodeOpCommand("book.list.player");
	public static final CommandPermissionNode BOOK_REINDEX_HAND = nodeOpCommand("book.reindex.hand");
	public static final CommandPermissionNode BOOK_REINDEX_PLAYER = nodeOpCommand("book.reindex.player");
	public static final CommandPermissionNode BOOK_REMOVE_HAND = nodeOpCommand("book.remove.hand");
	public static final CommandPermissionNode BOOK_REMOVE_PLAYER = nodeOpCommand("book.remove.player");
	public static final CommandPermissionNode DUMP_FUEL_MAP = nodeAllCommand("dump_fuel_map");
	public static final CommandPermissionNode SET_OWNER = nodeOpCommand("set_owner");
	public static final CommandPermissionNode RELOAD_EMC = nodeOpCommand("reload_emc");

	private Permissions() {}

	private static CommandPermissionNode nodeAllCommand(String nodeName) {
		PermissionNode<Boolean> node = node(nodeName, PermissionTypes.BOOLEAN, PLAYER_IS_ALL);
		return new CommandPermissionNode(node, Commands.LEVEL_ALL);
	}

	private static CommandPermissionNode nodeOpCommand(String nodeName) {
		PermissionNode<Boolean> node = node(nodeName, PermissionTypes.BOOLEAN, PLAYER_IS_OP);
		return new CommandPermissionNode(node, Commands.LEVEL_GAMEMASTERS);
	}

	@SuppressWarnings("SameParameterValue")
	@SafeVarargs
	private static <T> PermissionNode<T> node(String nodeName, PermissionType<T> type, PermissionNode.PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<T>... dynamics) {
		PermissionNode<T> node = new PermissionNode<>(PECore.MODID, NODE_PREFIX + nodeName, type, defaultResolver, dynamics);
		NODES.add(node);
		return node;
	}

	public record CommandPermissionNode(PermissionNode<Boolean> node, PermissionCheck fallbackLevel) implements Predicate<CommandSourceStack> {

		@Override
		public boolean test(CommandSourceStack source) {
			//26.1: CommandSourceStack#hasPermission(int) was replaced by testing the PermissionCheck against the source's PermissionSet
			return fallbackLevel.check(source.permissions()) || source.source instanceof ServerPlayer player && PermissionAPI.getPermission(player, node);
		}
	}

	@SubscribeEvent
	public static void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
		event.addNodes(NODES);
	}
}
