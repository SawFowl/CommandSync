package sawfowl.commandsyncclient.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandSynchronize implements CommandExecutor {

	private CSC plugin;
	
	public CommandSynchronize(CSC plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("sync.use")) {
			sender.sendMessage(plugin.getLocale().getString("NoPerm"));
			return true;
		}
		if(args.length >= 0) {
			if(args.length <= 2) {
				sender.sendMessage(plugin.getLocale().getString("HelpAuthors"));
				if(args.length >= 1) {
					if(args[0].equalsIgnoreCase("console")){
						sender.sendMessage(plugin.getLocale().getString("HelpCommands9", "sync"));
						sender.sendMessage(plugin.getLocale().getString("HelpCommands8", "sync"));
						sender.sendMessage(plugin.getLocale().getString("HelpCommands7", "sync"));
						sender.sendMessage(plugin.getLocale().getString("HelpCommands6", "sync"));
					} else if(args[0].equalsIgnoreCase("player")) {
						sender.sendMessage(plugin.getLocale().getString("HelpCommands5", "sync"));
						sender.sendMessage(plugin.getLocale().getString("HelpCommands4", "sync"));
					} else {
						sender.sendMessage(plugin.getLocale().getString("HelpCommands10", "sync"));
					}
				} else {
					sender.sendMessage(plugin.getLocale().getString("HelpCommands3", "sync"));
					sender.sendMessage(plugin.getLocale().getString("HelpCommands2", "sync"));
					sender.sendMessage(plugin.getLocale().getString("HelpCommands1"));
				}
				sender.sendMessage(plugin.getLocale().getString("HelpLink"));
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
					if(args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("bungee") || args[1].equalsIgnoreCase("proxy")) {
						makeData(newArgs, false, sender);
					} else {
					    makeData(newArgs, true, sender);
					}
				} else {
					sender.sendMessage(plugin.getLocale().getString("HelpCommands9"));
				}
			}
		}
		return true;
	}
	
	private void makeData(String[] args, Boolean single, CommandSender sender) {
		String data;
		String message = "";
		if(args[0].equalsIgnoreCase("console")) {
			if(args[1].equalsIgnoreCase("player")) {
				String playerName = args[2].split("\\+")[0];
				message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " ").replaceFirst(playerName + " ", ""),  plugin.getLocale().getString("SyncConsolePlayer", playerName));
			} else if(args[1].equalsIgnoreCase("all")) {
				message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsoleAll"));
			} else {
				message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsole", args[1]));
			}
		} else if(args[0].equalsIgnoreCase("bungee") || args[0].equalsIgnoreCase("proxy")) {
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
