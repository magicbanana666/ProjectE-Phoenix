package moze_intel.projecte.expansion.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import moze_intel.projecte.PECore;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * The {@code /px} command tree, plus the {@code /projectexpansion} and {@code /pex} aliases.
 * <p>
 * Deliberately a separate root literal from ProjectE's {@code /projecte} so the two trees never merge. The subcommand
 * names ({@code book}, {@code emc}, {@code knowledge}, {@code dumpfuelmap}, {@code devtest},
 * {@code setowner}, {@code reloademc}) do not overlap with ProjectE's ({@code remove_emc}, {@code reset_emc},
 * {@code set_emc}, {@code show_bag}, {@code emc}, {@code knowledge}) at the same level either.
 * <p>
 * Self registering through {@link EventBusSubscriber}, the main loop does not have to call anything.
 * <p>
 * 26.3 note: {@code EventBusSubscriber} no longer has a {@code bus} attribute, FML picks the mod or the game bus
 * from the event type itself. {@link RegisterCommandsEvent} is a game bus event, so a plain
 * {@code @EventBusSubscriber(modid = PECore.MODID)} is all that is needed.
 */
@EventBusSubscriber(modid = PECore.MODID)
public class CommandRegistry {
	public static final String COMMAND_BASE = "px";
	public static final String ALIAS_LONG = "phoenix";
	public static final String ALIAS_SHORT = "pex";

	private CommandRegistry() {}

	@SubscribeEvent
	public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		CommandBuildContext buildContext = event.getBuildContext();

		LiteralCommandNode<CommandSourceStack> baseNode = dispatcher.register(Commands.literal(COMMAND_BASE)
				.then(CommandBook.getArguments())
				.then(CommandEMC.getArguments())
				.then(CommandKnowledge.getArguments(buildContext))
				.then(CommandDumpFuelMap.getArguments())
				.then(CommandDevTest.getArguments())
				.then(CommandSetOwner.getArguments())
				.then(CommandReloadEMC.getArguments())
		);
		dispatcher.register(Commands.literal(ALIAS_LONG).redirect(baseNode));
		dispatcher.register(Commands.literal(ALIAS_SHORT).redirect(baseNode));
	}
}
