package sawfowl.commandsyncserver.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import net.kyori.adventure.text.Component;

public class SyncCommand implements SimpleCommand {

    private final CSS plugin;
    public SyncCommand(CSS css) {
        plugin = css;
    }

	@Override
	public void execute(Invocation invocation) {
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();
        if(args.length >= 2 && args[0].equalsIgnoreCase("console") && args[1].equalsIgnoreCase("bungee")) {
        	source.sendMessage(Component.text(plugin.getLocale().getString("CantUseArgProxyServer")));
            return;
        }
        if(args.length >= 0) {
            if(args.length <= 2) {
                source.sendMessage(Component.text(plugin.getLocale().getString("HelpAuthors")));
                if(args.length >= 1) {
                    if(args[0].equalsIgnoreCase("console")){
                        source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands9", "proxysync")));
                        source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands8", "proxysync")));
                        source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands7", "proxysync")));
                        source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands6", "proxysync")));
                    } else if(args[0].equalsIgnoreCase("player")) {
                        source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands5", "proxysync")));
                        source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands4", "proxysync")));
                    } else {
                        source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands10", "proxysync")));
                    }
                } else {
                    source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands3", "proxysync")));
                    source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands2", "proxysync")));
                    source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands1")));
                }
                source.sendMessage(Component.text(plugin.getLocale().getString("HelpLink")));
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
                        makeData(newArgs, false, source);
                    } else {
                        makeData(newArgs, true, source);
                    }
                } else {
                    source.sendMessage(Component.text(plugin.getLocale().getString("HelpCommands9")));
                }
            }
        }
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
		return invocation.source().hasPermission("sync.use");    
	}

    private void makeData(String[] args, Boolean single, CommandSource source) {
        String data;
        String message;
        if(args[0].equalsIgnoreCase("console")) {
            if(args[1].equalsIgnoreCase("all")) {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsoleAll"));
            } else {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsole", args[1]));
            }
        } else if(args[0].equalsIgnoreCase("bungee")) {
            message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsole", args[1]));
        } else {
            if(args[1].equalsIgnoreCase("all")) {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncPlayerAll"));
            } else {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncPlayer", args[1]));
            }
        }
        if(single) {
            data = args[0].toLowerCase() + plugin.spacer + "single" + plugin.spacer + args[2] + plugin.spacer + args[1];
        } else {
            data = args[0].toLowerCase() + plugin.spacer + args[1].toLowerCase() + plugin.spacer + args[2];
        }
        plugin.oq.add(data);
        source.sendMessage(Component.text(message));
    }

}
