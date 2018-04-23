package Mr_Krab.CommandSyncServer.Bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.ConsoleCommandSender;

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
		ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeConnect")
				.replace("%host%", socket.getInetAddress().getHostName() + ":" + socket.getPort() + ".")));
		name = in.readLine();
		if(plugin.c.contains(name)) {
			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("NameErrorBungee")
					.replace("%name%", "[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] [" + name + "]")));
		    out.println("n");
		    socket.close();
		    return;
		}
		out.println("y");
		if(!in.readLine().equals(this.pass)) {
			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("PassError")
					.replace("%name%", "[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] [" + name + "]")));
			out.println("n");
			socket.close();
			return;
		}
		out.println("y");
		String version = in.readLine();
		if(!version.equals(this.version)) {
			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("VersionErrorBungee")
					.replace("%name%", "[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] [" + name + "]")
					.replace("%ClientVersion%", version).replace("%version%", this.version)));
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
		ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("ConnectFrom")
				.replace("%address%", socket.getInetAddress().getHostName() + ":" + socket.getPort()).replace("%name%", name)));
	}

	public void run() {
		while(true) {
			try {
				out.println("heartbeat");
				if(out.checkError()) {
					ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("Disconect")
							.replace("%address%", socket.getInetAddress().getHostName() + ":" + socket.getPort()).replace("%name%", name)));
					plugin.c.remove(name);
					return;
				}
				while(in.ready()) {
					String input = in.readLine();
					if(!input.equals("heartbeat")) {
						ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeInput")
								.replace("%host%", "[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] [" + name + "]")
								.replace("%input%", input)));
						String[] data = input.split(plugin.spacer);
						if(data[0].equals("player")) {
							String command = "/" + data[2].replaceAll("\\+", " ");
							if(data[1].equals("single")) {
								String name = data[3];
								Boolean found = false;
								for(ProxiedPlayer player : plugin.getProxy().getPlayers()) {
									if(name.equals(player.getName())){
										player.chat(command);
										ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeRanPlayerSingle")
												.replace("%command%", command).replace("%name%", name)));
										found = true;
										break;
									}
								}
								if(!found) {
									if(plugin.pq.containsKey(name)) {
										List<String> commands = plugin.pq.get(name);
										commands.add(command);
										plugin.pq.put(name, commands);
									} else {
										plugin.pq.put(name, new ArrayList<String>(Arrays.asList(command)));
									}
									ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeRanPlayerOffline")
											.replace("%name%", name).replace("%command%", command)));
								}
							} else if(data[1].equals("all")) {
								for(ProxiedPlayer player : plugin.getProxy().getPlayers()) {
									player.chat(command);
								}
								ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeRanAll")
										.replace("%command%", command)));
							}
						} else {
							if(data[1].equals("bungee")) {
								String command = data[2].replaceAll("\\+", " ");
								plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
								ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeRanServer")
										.replace("%command%", command)));
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
						if(data[1].equals("single")) {
							if(data[3].equals(name)) {
								out.println(output);
								ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeSentOutput")
										.replace("%host%", "[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] [" + name + "]")
										.replace("%output%", output)));
							}
						} else {
							out.println(output);
							ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.loc.getString("BungeeSentOutput")
									.replace("%host%", "[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] [" + name + "]")
									.replace("%output%", output)));
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
}
