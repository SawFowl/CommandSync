package sawfowl.commandsyncclient.sponge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CommandSynchronize implements Command.Raw {

	private final CSC plugin;
	public CommandSynchronize(CSC plugin) {
		this.plugin = plugin;
		completionStrings.add("console");
		completionStrings.add("player");
		completions.addAll(completionStrings.stream().map(CommandCompletion::of).collect(Collectors.toList()));
	}

	List<CommandCompletion> empty = new ArrayList<CommandCompletion>();
	List<String> completionStrings = new ArrayList<String>();
	List<CommandCompletion> completions = new ArrayList<CommandCompletion>();

	@Override
	public CommandResult process(CommandCause cause, Mutable arguments) throws CommandException {
		String[] args = Stream.of(arguments.input().split(" ")).filter(string -> (!string.equals(""))).toArray(String[]::new);
		if(args.length >= 0) {
			if(args.length <= 2) {
				cause.audience().sendMessage(plugin.getLocale().getString("HelpAuthors"));
				if(args.length >= 1) {
					if(args[0].equalsIgnoreCase("console")){
						cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands9", "sync"));
						cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands8", "sync"));
						cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands7", "sync"));
						cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands6", "sync"));
					} else if(args[0].equalsIgnoreCase("player")) {
						cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands5", "sync"));
						cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands4", "sync"));
					} else {
						cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands10", "sync"));
					}
				} else {
					cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands3", "sync"));
					cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands2", "sync"));
					cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands1"));
				}
				cause.audience().sendMessage(plugin.getLocale().getString("HelpLink"));
			} else if(args.length >= 3) {
				if(args[0].equalsIgnoreCase("console") || args[0].equalsIgnoreCase("player")) {
				    String[] newArgs = new String[3];
				    newArgs[0] = args[0];
				    newArgs[1] = args[1];
				    StringBuilder sb = new StringBuilder();
				    for(int i = 2; i < args.length; i++) {
				        sb.append(args[i]);
				        if(i < args.length - 1) {
				            sb.append("+");
				        }
				    }
				    newArgs[2] = sb.toString();
					if(args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("bungee")) {
						makeData(newArgs, false, cause);
					} else {
					    makeData(newArgs, true, cause);
					}
				} else {
					cause.audience().sendMessage(plugin.getLocale().getString("HelpCommands9"));
				}
			}
		}
		return CommandResult.success();
	}

	@Override
	public List<CommandCompletion> complete(CommandCause cause, Mutable arguments) throws CommandException {
		String plainArgs = arguments.input();
		List<String> args = Stream.of(plainArgs.split(" ")).filter(string -> (!string.equals(""))).collect(Collectors.toList());
		if(args.size() == 0) return completions;
		if(args.size() == 1) {
			if(completionStrings.contains(args.get(0))) {
				return empty;
			} else return completionStrings.stream().filter(string -> (string.startsWith(args.get(0)))).map(CommandCompletion::of).collect(Collectors.toList());
		}
		return empty;
	}

	@Override
	public boolean canExecute(CommandCause cause) {
		return cause.hasPermission("sync.use");
	}

	@Override
	public Optional<Component> shortDescription(CommandCause cause) {
		return Optional.of(Component.text("Sync command."));
	}

	@Override
	public Optional<Component> extendedDescription(CommandCause cause) {
		return Optional.of(Component.text("Sync command."));
	}

	@Override
	public Component usage(CommandCause cause) {
		return Component.text("/sync <console/player> <args...>");
	}

	private void makeData(String[] args, Boolean single, CommandCause cause) {
		String data;
		Component message;
		if(args[0].equalsIgnoreCase("console")) {
			if(args[1].equalsIgnoreCase("all")) {
				message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "), serialize(plugin.getLocale().getString("SyncConsoleAll")));
			} else {
				message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "), serialize(plugin.getLocale().getString("SyncConsole", args[1])));
			}
		} else if(args[0].equalsIgnoreCase("bungee")) {
			message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  serialize(plugin.getLocale().getString("SyncConsole", args[1])));
		} else {
			if(args[1].equalsIgnoreCase("all")) {
				message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "), serialize(plugin.getLocale().getString("SyncPlayerAll")));
			} else {
				message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "), serialize(plugin.getLocale().getString("SyncPlayer", args[1])));
			}
		}
		if(single) {
		    data = args[0].toLowerCase() + plugin.spacer + "single" + plugin.spacer + args[2] + plugin.spacer + args[1];
		} else {
		    data = args[0].toLowerCase() + plugin.spacer + args[1].toLowerCase() + plugin.spacer + args[2];
		}
		plugin.oq.add(data);
		cause.audience().sendMessage(message);
	}

	private String serialize(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component);
	}

}
