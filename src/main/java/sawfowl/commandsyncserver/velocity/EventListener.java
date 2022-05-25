package sawfowl.commandsyncserver.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;

public class EventListener {

	private CSS plugin;
	
	public EventListener(CSS plugin) {
		this.plugin = plugin;
	}
	
	@Subscribe(order = PostOrder.EARLY)
	public void onServerConnected(LoginEvent event) {
		Player player = event.getPlayer();
		if(plugin.pq.containsKey(player.getUsername())) {
			new CommandThread(plugin, player).start();
		}
	}
}
