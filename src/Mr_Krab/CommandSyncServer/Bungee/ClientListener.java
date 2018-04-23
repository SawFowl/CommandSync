package Mr_Krab.CommandSyncServer.Bungee;

import java.io.IOException;

public class ClientListener extends Thread {

	private CSS plugin;
	private Integer heartbeat;
	private String pass;

	public ClientListener(CSS plugin, Integer heartbeat, String pass) {
		this.plugin = plugin;
		this.heartbeat = heartbeat;
		this.pass = pass;
	}

	public void run() {
		while(true) {
			try {
				new ClientHandler(plugin, plugin.server.accept(), heartbeat, pass).start();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
