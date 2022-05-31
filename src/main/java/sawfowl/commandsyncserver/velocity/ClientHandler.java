package sawfowl.commandsyncserver.velocity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.velocitypowered.api.proxy.Player;

public class ClientHandler extends Thread {

	private CSS plugin;
	private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Integer heartbeat = 0;
    private String name;
    private String pass;
    private String version = "2.5";

	public ClientHandler(CSS plugin, Socket socket, Integer heartbeat, String pass) throws IOException {
		this.plugin = plugin;
		this.socket = socket;
		this.heartbeat = heartbeat;
		this.pass = pass;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		plugin.getLoger().info(plugin.getLocale().getString("BungeeConnect", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort())));
		name = in.readLine();
		if(plugin.c.contains(name)) {
			plugin.getLoger().info(plugin.getLocale().getString("NameErrorBungee", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
		    out.println("n");
		    socket.close();
		    return;
		}
		out.println("y");
		if(!in.readLine().equals(this.pass)) {
			plugin.getLoger().info(plugin.getLocale().getString("PassError", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
			out.println("n");
			socket.close();
			return;
		}
		out.println("y");
		String version = in.readLine();
		if(!version.equals(this.version)) {
			plugin.getLoger().info(plugin.getLocale().getString("VersionErrorBungee", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name, version, this.version));
		    out.println("n");
		    out.println(this.version);
		    socket.close();
		    return;
		}
		out.println("y");
		if(!plugin.qc.containsKey(name)) {
		    plugin.qc.put(name, 0);
		}
		plugin.c.add(name);
		plugin.getLoger().info(plugin.getLocale().getString("ConnectFrom", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
	}

	public void run() {
		while(true) {
			try {
				out.println("heartbeat");
				if(out.checkError()) {
					plugin.getLoger().info(plugin.getLocale().getString("Disconect", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
					plugin.c.remove(name);
					return;
				}
				while(in.ready()) {
					String input = in.readLine();
					if(!input.equals("heartbeat")) {
						plugin.getLoger().info(plugin.getLocale().getString("BungeeInput", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name, input));
						String[] data = input.split(plugin.spacer);
						if(data[0].equals("player")) {
							String command = "/" + data[2].replaceAll("\\+", " ");
							if(data[1].equals("single")) {
								String name = data[3];
								Boolean found = false;
								Optional<Player> player = plugin.getProxyServer().getAllPlayers().stream().filter(p -> (name.equals(p.getUsername()))).findFirst();
								if(player.isPresent()) {
									player.get().spoofChatInput(command);
									plugin.getLoger().info(plugin.getLocale().getString("BungeeRanPlayerSingle", true, command, name));
									found = true;
								}
								if(!found) {
									if(plugin.pq.containsKey(name)) {
										List<String> commands = plugin.pq.get(name);
										commands.add(command);
										plugin.pq.put(name, commands);
									} else {
										plugin.pq.put(name, new ArrayList<String>(Arrays.asList(command)));
									}
									plugin.getLoger().info(plugin.getLocale().getString("BungeeRanPlayerOffline", true, name, command));
								}
							} else if(data[1].equals("all")) {
								for(Player player : plugin.getProxyServer().getAllPlayers()) {
									player.spoofChatInput(command);
								}
								plugin.getLoger().info(plugin.getLocale().getString("BungeeRanAll", true, command));
							}
						} else {
							if(data[1].equals("bungee") || data[1].equals("proxy")) {
								String command = data[2].replaceAll("\\+", " ");
								plugin.getProxyServer().getCommandManager().executeAsync(plugin.getProxyServer().getConsoleCommandSource(), command);
								plugin.getLoger().info(plugin.getLocale().getString("BungeeRanServer", true, command));
							} else {
								plugin.oq.add(input);
							}
						}
					}
				}
				Integer size = plugin.oq.size();
				Integer count = plugin.qc.get(name);
				if(size > count) {
					for(int i = count; i < size; i++) {
						count++;
						String output = plugin.oq.get(i);
						String[] data = output.split(plugin.spacer);
						boolean playerFilter = data.length > 3 && data[0].equals("console") && data[data.length - 1].equals("player") && output.contains("+");
						String playerName = playerFilter ? data[2].split("\\+")[0] : "";
						Optional<Player> player = playerFilter ? plugin.getProxyServer().getAllPlayers().stream().filter(p -> (playerName.equals(p.getUsername()))).findFirst() : Optional.empty();
						boolean singlePlayer = player.isPresent() && player.get().isActive() && player.get().getCurrentServer().isPresent() && player.get().getCurrentServer().get().getServerInfo().getName().equals(name);
						boolean single = data[1].equals("single") || singlePlayer;
						if(single) {
							if(data[3].equals(name)) {
								send(output);
							} else if(singlePlayer) {
								send(output.split(plugin.spacer + "player")[0].replaceFirst(playerName + "\\+", "") + plugin.spacer + name);
							}
						} else {
							send(output);
						}
					}
					plugin.qc.put(name, count);
				}
				sleep(heartbeat);
			} catch(Exception e) {
				plugin.c.remove(name);
				e.printStackTrace();
			}
		}
	}

	private void send(String output) {
		out.println(output);
		plugin.getLoger().info(plugin.getLocale().getString("BungeeSentOutput", true, socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name, output));
	}
}
