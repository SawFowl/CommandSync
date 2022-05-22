package sawfowl.commandsyncserver.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class SyncCommand extends Command {

    private final CSS plugin;
    public SyncCommand(CSS css) {
        super("proxysync");
        plugin = css;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("sync.use")) {
            sender.sendMessage(new TextComponent(plugin.getLocale().getString("NoPerm")));
            return;
        }
        if(args.length >= 2 && args[0].equalsIgnoreCase("console") && args[1].equalsIgnoreCase("bungee")) {
            sender.sendMessage(new TextComponent(plugin.getLocale().getString("CantUseArgProxyServer")));
            return;
        }
        if(args.length >= 0) {
            if(args.length <= 2) {
                sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpAuthors")));
                if(args.length >= 1) {
                    if(args[0].equalsIgnoreCase("console")){
                        sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands9", "proxysync")));
                        sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands8", "proxysync")));
                        sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands7", "proxysync")));
                        sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands6", "proxysync")));
                    } else if(args[0].equalsIgnoreCase("player")) {
                        sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands5", "proxysync")));
                        sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands4", "proxysync")));
                    } else {
                        sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands10", "proxysync")));
                    }
                } else {
                    sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands3", "proxysync")));
                    sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands2", "proxysync")));
                    sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands1")));
                }
                sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpLink")));
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
                        makeData(newArgs, false, sender);
                    } else {
                        makeData(newArgs, true, sender);
                    }
                } else {
                    sender.sendMessage(new TextComponent(plugin.getLocale().getString("HelpCommands9")));
                }
            }
        }
    }

    private void makeData(String[] args, Boolean single, CommandSender sender) {
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
        sender.sendMessage(message);
    }

}
