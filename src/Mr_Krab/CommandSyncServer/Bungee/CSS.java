package Mr_Krab.CommandSyncServer.Bungee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Mr_Krab.CommandSyncServer.Bungee.Metrics.Graph;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;

public class CSS extends Plugin {

	public ServerSocket server;
	public Set<String> c = Collections.synchronizedSet(new HashSet<String>());
	public List<String> oq = Collections.synchronizedList(new ArrayList<String>());
	public Map<String, List<String>> pq = Collections.synchronizedMap(new HashMap<String, List<String>>());
	public Map<String, Integer> qc = Collections.synchronizedMap(new HashMap<String, Integer>());
	public String spacer = "@#@";
	public Debugger debugger;
	public Locale loc;
	private boolean remove;
	
	public void onEnable() {
		String[] data = loadConfig();
		if(data[3].equals("UNSET")) {
			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("UnsetValues")));
			return;
		}
		try {
			server = new ServerSocket(Integer.parseInt(data[1]), 50, InetAddress.getByName(data[0]));
			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("OpenOn") + data[0] + ":" + data[1] + "."));
			new ClientListener(this, Integer.parseInt(data[2]), data[3]).start();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			workData();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
		    Metrics metrics = new Metrics(this);
		    Graph graph1 = metrics.createGraph("Total queries sent");
		    graph1.addPlotter(new Metrics.Plotter() {
				public int getValue() {
					return oq.size();
				}
				public String getColumnName() {
					return "Total queries sent";
				}
			});
		    Graph graph2 = metrics.createGraph("Total servers linked");
		    graph2.addPlotter(new Metrics.Plotter() {
				public int getValue() {
					return qc.keySet().size();
				}
				public String getColumnName() {
					return "Total servers linked";
				}
			});
		    metrics.start();
		    getProxy().getPluginManager().registerListener(this, new EventListener(this));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void workData() throws IOException {
		File folder = getDataFolder();
		File data = new File(folder + File.separator + "data.txt");
        boolean remove = this.remove;
        if (remove == true) {
        	if(data.delete()){
    			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("DataRemoved")));
			} else ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("DataRemoveNotFound"))); 
        } else {
    		loadData();
        }
	}
	
	public void onDisable() {
		saveData();
		debugger.close();
	}
	
	private String[] loadConfig() {
		String[] defaults = new String[] {
			"ip=localhost", "port=9190", "heartbeat=1000", "pass=UNSET", "debug=false", "removedata=false", "lang=en_US"
		};
		String[] data = new String[defaults.length];
		try {
		    File folder = getDataFolder();
		    if(!folder.exists()) {
		        folder.mkdir();
		    }
			File file = new File(folder, "config.txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			BufferedReader br = new BufferedReader(new FileReader(file));
			for(int i = 0; i < defaults.length; i++) {
			    String l = br.readLine();
			    if(l == null || l.isEmpty()) {
			        data[i] = defaults[i].split("=")[1];
			    } else {
			        data[i] = l.split("=")[1];
			        defaults[i] = l;
			    }
			}
	        br.close();
	        file.delete();
	        file.createNewFile();
			PrintStream ps = new PrintStream(new FileOutputStream(file));
			for(int i = 0; i < defaults.length; i++) {
				ps.println(defaults[i]);
			}
			ps.close();
			debugger = new Debugger(this, Boolean.valueOf(data[4]));
            remove = Boolean.valueOf(data[5]);
    		loc = new Locale(this, String.valueOf(data[6]));
    		loc.init();
			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("ConfigLoaded")));
		} catch(IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	private void saveData() {
		try {
			OutputStream os = new FileOutputStream(new File(getDataFolder(), "data.txt"));
			PrintStream ps = new PrintStream(os);
			for(String s : oq) {
				ps.println("oq:" + s);
			}
			for(Entry<String, List<String>> e : pq.entrySet()) {
				String name = e.getKey();
				for(String command : e.getValue()) {
					ps.println("pq:" + name + spacer + command);
				}
			}
			for(Entry<String, Integer> e : qc.entrySet()) {
				ps.println("qc:" + e.getKey() + spacer + String.valueOf(e.getValue()));
			}
			ps.close();
			ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("DataSaved")));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void loadData() {
		try {
			File file = new File(getDataFolder(), "data.txt");
			if(file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				try {
					String l = br.readLine();
					while(l != null) {
						if(l.startsWith("oq:")) {
							oq.add(new String(l.substring(3)));
						} else if(l.startsWith("pq:")) {
							String[] parts = new String(l.substring(3)).split(spacer);
							if(pq.containsKey(parts[0])) {
								List<String> commands = pq.get(parts[0]);
								commands.add(parts[1]);
								pq.put(parts[0], commands);
							} else {
								List<String> commands = new ArrayList<String>(Arrays.asList(parts[1]));
								pq.put(parts[0], commands);
							}
						} else if(l.startsWith("qc:")) {
							String[] parts = new String(l.substring(3)).split(spacer);
							qc.put(parts[0], Integer.parseInt(parts[1]));
						}
						l = br.readLine();
					}
					ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("DataLoaded")));
				} finally {
					br.close();
				}
			} else {
				ConsoleCommandSender.getInstance().sendMessage(ChatColor.translateAlternateColorCodes('&', loc.getString("DataNotfound")));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
