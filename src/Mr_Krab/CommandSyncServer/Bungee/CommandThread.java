package Mr_Krab.CommandSyncServer.Bungee;

import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.ConsoleCommandSender;

public class CommandThread extends Thread {

	private CSS plugin;
	private ProxiedPlayer player;
	private String name;
	private List<String> commands;
	
	public CommandThread(CSS plugin, ProxiedPlayer player) {
		this.plugin = plugin;
		this.player = player;
		this.name = player.getName();
		this.commands = plugin.pq.get(name);
	}
	
	public void run() {
		while(true) {
			try {
				for(String command : commands) {
					player.chat(command);
					ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeRanPlayerSingle")
							.replace("%command%", command).replace("%name%", name)));
				}
				plugin.pq.remove(name);
				return;
			} catch(IllegalStateException e1) {
				try {
					sleep(1000);
				} catch(InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
