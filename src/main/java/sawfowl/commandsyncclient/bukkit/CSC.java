package sawfowl.commandsyncclient.bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CSC extends JavaPlugin {

	public ClientThread client;
	public List<String> oq = Collections.synchronizedList(new ArrayList<String>());
	public Integer qc = 0;
	public String spacer = "@#@";
	public Debugger debugger;
	private Locale loc;
	private boolean remove;
	
	public void onEnable() {
		String[] data = loadConfig();
		if(data[3].equals("UNSET") || data[4].equals("UNSET")) {
			Bukkit.getConsoleSender().sendMessage(loc.getString("UnsetValues"));
			return;
		}
		try {
			client = new ClientThread(this, InetAddress.getByName(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), data[3], data[4]);
			client.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			workData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		getCommand("Sync").setExecutor(new CommandSynchronize(this));
		
	}
	
	public void onDisable() {
		saveData();
        debugger.close();
		loc = null;
	}
	
	public Locale getLocale() {
		return loc;
	}
	
	private void workData() throws IOException {
		File folder = getDataFolder();
		File data = new File(folder + File.separator + "data.txt");
        boolean remove = this.remove;
        if (remove == true) {
        	if(data.delete()){
        		Bukkit.getConsoleSender().sendMessage(loc.getString("DataRemoved"));
			} else Bukkit.getConsoleSender().sendMessage(loc.getString("DataRemoveNotFound"));
        } else {
    		loadData();
        }
	}

	public String[] loadConfig() {
		String[] defaults = new String[] {
			"ip=localhost", "port=39999", "heartbeat=1000", "name=UNSET", "pass=UNSET", "debug=false", "removedata=false", "lang=en_US"
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
            debugger = new Debugger(this, Boolean.valueOf(data[5]));
            remove = Boolean.valueOf(data[6]);
    		loc = new Locale(this, String.valueOf(data[7]));
    		loc.init();
    		Bukkit.getConsoleSender().sendMessage(loc.getString("ConfigLoaded"));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return data;
	}
	
	private void saveData() {
		try{
			OutputStream os = new FileOutputStream(new File(getDataFolder(), "data.txt"));
			PrintStream ps = new PrintStream(os);
			for(String s : oq) {
				ps.println("oq:" + s);
			}
			ps.println("qc:" + String.valueOf(qc));
			ps.close();
			Bukkit.getConsoleSender().sendMessage(loc.getString("DataSaved"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void loadData() {
		try{
			File file = new File(getDataFolder(), "data.txt");
			if(file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				try {
					String l = br.readLine();
					while(l != null) {
						if(l.startsWith("oq:")) {
							oq.add(new String(l.substring(3)));
						} else if(l.startsWith("qc:")) {
							qc = Integer.parseInt(new String(l.substring(3)));
						}
						l = br.readLine();
					}
					Bukkit.getConsoleSender().sendMessage(loc.getString("DataLoaded"));
				} finally {
					br.close();
				}
			} else {
			    Bukkit.getConsoleSender().sendMessage(loc.getString("DataNotfound"));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
