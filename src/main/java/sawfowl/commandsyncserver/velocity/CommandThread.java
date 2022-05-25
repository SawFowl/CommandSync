package sawfowl.commandsyncserver.velocity;

import java.util.List;

import com.velocitypowered.api.proxy.Player;

public class CommandThread extends Thread {

	private CSS plugin;
	private Player player;
	private String name;
	private List<String> commands;

	public CommandThread(CSS plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		this.name = player.getUsername();
		this.commands = plugin.pq.get(name);
	}

	public void run() {
		while(true) {
			try {
				for(String command : commands) {
					player.spoofChatInput(command);
					plugin.getLoger().info(plugin.getLocale().getString("BungeeRanPlayerSingle", true, command, name));
				}
				plugin.pq.remove(name);
				return;
			} catch(IllegalStateException e1) {
				try {
					sleep(1000);
				} catch(InterruptedException e2) {
					plugin.getLoger().error(e2.getLocalizedMessage());
				}
			}
		}
	}
}
