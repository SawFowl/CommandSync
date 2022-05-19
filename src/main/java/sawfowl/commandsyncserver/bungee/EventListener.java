package sawfowl.commandsyncserver.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListener implements Listener {

	private CSS plugin;
	
	public EventListener(CSS plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onServerConnected(ServerConnectedEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if(plugin.pq.containsKey(player.getName())) {
			new CommandThread(plugin, player).start();
		}
	}
}
