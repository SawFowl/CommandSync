package sawfowl.commandsyncclient.sponge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;

public class ClientThread extends Thread {

	private CSC plugin;
	private InetAddress ip;
	private Integer port;
	private Boolean connected = false;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private Integer heartbeat = 0;
	private String name;
	private String pass;
	private String version = "2.5";
	public Integer countR;
		
	public ClientThread(CSC plugin) {
		this.plugin = plugin;
	}
	
	public ClientThread(CSC plugin, InetAddress ip, Integer port, Integer heartbeat, String name, String pass) {
		this.plugin = plugin;
		this.ip = ip;
		this.port = port;
		this.heartbeat = heartbeat;
		this.name = name;
		this.pass = pass;
		connect(false);
	}
	
	public void run() {
		while(true) {
			if(connected) {
				out.println("heartbeat");
				if(out.checkError()) {
					connected = false;
					plugin.getLogger().warn(plugin.getLocale().getString("ConnectLost"));
				} else {
					try {
						Integer size = plugin.oq.size();
						Integer count = plugin.qc;
						if(size > count) {
							for(int i = count; i < size; i++) {
								count++;
								String output = plugin.oq.get(i);
								out.println(output);
								plugin.getLogger().info(plugin.getLocale().getString("SentOutput", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), output));
							}
							plugin.qc = count;
						}
						while(in.ready()) {
							String input = in.readLine();
							if(!input.equals("heartbeat")) {
								plugin.getLogger().info(plugin.getLocale().getString("ReceivedInput", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), input));
								String[] data = input.split(plugin.spacer);
								if(data[0].equals("console")) {
									String command = data[2].replaceAll("\\+", " ");
									plugin.getLogger().info(plugin.getLocale().getString("RanCommand", command));
									Sponge.server().scheduler().executor(plugin.getContainer()).execute(() -> {
										try {
											Sponge.server().commandManager().process(Sponge.systemSubject(), command);
										} catch (CommandException e) {
											plugin.getLogger().error(e.getLocalizedMessage());
										}
									});
								}
							}
						}
					} catch(IOException e) {
						e.printStackTrace();
					}					
				}
			} else {
				connect(true);
			}
			try {
				sleep(heartbeat);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void connect(Boolean sleep) {
		if(sleep) {
			try {
				sleep(10000);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			socket = new Socket(ip, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(name);
			if(in.readLine().equals("n")) {
				plugin.getLogger().error(plugin.getLocale().getString("NameError", name));
			    socket.close();
			    return;
			}
			out.println(pass);
			if(in.readLine().equals("n")) {
				plugin.getLogger().error(plugin.getLocale().getString("InvalidPassword"));
			    socket.close();
				return;
			}
            out.println(version);
            if(in.readLine().equals("n")) {
				plugin.getLogger().error(plugin.getLocale().getString("VersionError", version, in.readLine()));
                socket.close();
                return;
            }
			connected = true;
			plugin.getLogger().error(plugin.getLocale().getString("ConnectInfo", ip.getHostName(), String.valueOf(port), name));
		} catch(IOException e) {
			plugin.getLogger().error(plugin.getLocale().getString("NoConnect"));
		}
	}
}
