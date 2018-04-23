package Mr_Krab.CommandSyncClient.Bukkit;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandSynchronize implements CommandExecutor {

	private CSC plugin;
	
	public CommandSynchronize(CSC plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender.hasPermission("sync.use")) {
			if(args.length >= 0) {
				if(args.length <= 2) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpAuthors")));
					if(args.length >= 1) {
						if(args[0].equalsIgnoreCase("console")){
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands8")));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands7")));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands6")));
						} else if(args[0].equalsIgnoreCase("player")) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands5")));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands4")));
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands9")));
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands3")));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands2")));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands1")));
					}
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpLink")));
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
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("HelpCommands9")));
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("NoPerm")));
		}
		return true;
	}
	
	private void makeData(String[] args, Boolean single, CommandSender sender) {
		String data;
		String message = ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("SyncingCommand")) + args[2].replaceAll("\\+", " ") + ChatColor.translateAlternateColorCodes('&',  plugin.loc.getString("To")) + args[0];
		if(single) {
		    data = args[0].toLowerCase() + plugin.spacer + "single" + plugin.spacer + args[2] + plugin.spacer + args[1];
			message = message + " [" + args[1] + "]...";
		} else {
		    data = args[0].toLowerCase() + plugin.spacer + args[1].toLowerCase() + plugin.spacer + args[2];
			message = message + " [" + WordUtils.capitalizeFully(args[1]) + "]...";
		}
		plugin.oq.add(data);
		sender.sendMessage(message);
	}
}
