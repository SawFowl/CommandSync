package Mr_Krab.CommandSyncClient.Sponge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;

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
					plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(plugin.loc.getString("ConnectLost")));
				} else {
					try {
						Integer size = plugin.oq.size();
						Integer count = plugin.qc;
						if(size > count) {
							for(int i = count; i < size; i++) {
								count++;
								String output = plugin.oq.get(i);
								out.println(output);
								plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize("[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] " + plugin.loc.getString("SentOutput") + output));
							}
							plugin.qc = count;
						}
						while(in.ready()) {
							String input = in.readLine();
							if(!input.equals("heartbeat")) {
								plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize("[" + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "] " + plugin.loc.getString("ReceivedInput") + input));
								String[] data = input.split(plugin.spacer);
								if(data[0].equals("console")) {
									String command = data[2].replaceAll("\\+", " ");
									plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(plugin.loc.getString("RanCommand") + command + "."));
									Sponge.getServer().equals(Task.builder().execute(() -> Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command))
										    .delay(100, TimeUnit.MILLISECONDS)
										    .submit(plugin));
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
				plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(plugin.loc.getString("NameError").replace("%name%", name)));
			    socket.close();
			    return;
			}
			out.println(pass);
			if(in.readLine().equals("n")) {
				plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(plugin.loc.getString("InvalidPassword")));
			    socket.close();
				return;
			}
            out.println(version);
            if(in.readLine().equals("n")) {
				plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(plugin.loc.getString("VersionError").replace("%client%", version).replace("%server%", in.readLine())));
                socket.close();
                return;
            }
			connected = true;
			plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(plugin.loc.getString("ConnectInfo").replace("%host%", ip.getHostName()).replace("%port%", String.valueOf(port)).replace("%name%", name)));
		} catch(IOException e) {
			plugin.consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(plugin.loc.getString("NoConnect")));
		}
	}
}
