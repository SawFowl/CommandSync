package sawfowl.commandsyncclient.sponge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

@Plugin("commandsync")
public class CSC {

	public ClientThread client;
	public List<String> oq = Collections.synchronizedList(new ArrayList<String>());
	public Integer qc = 0;
	public String spacer = "@#@";
	public Debugger debugger;
	private static CSC instance;
	private Locale loc;
	private boolean remove;
	private Logger logger;
	private PluginContainer container;
    public File configDir;

	@Inject
	public CSC(PluginContainer container, @ConfigDir(sharedRoot = false) Path configDir) {
		logger = LogManager.getLogger("CommandSync");
		this.container = container;
		this.configDir = configDir.toFile();
	}

	public Locale getLocale() {
		return loc;
	}

	public Logger getLogger() {
		return logger;
	}

	public static CSC getInstance() {
        return instance;
    }

	public PluginContainer getContainer() {
		return container;
	}
    
	@Listener
	public void onServerStart(ConstructPluginEvent event) {
		String[] data = loadConfig();
		if(data[3].equals("UNSET") || data[4].equals("UNSET")) {
			logger.warn(loc.getString("UnsetValues"));
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
	}

	private String[] loadConfig() {
		String[] defaults = new String[] {
			"ip=localhost", "port=39999", "heartbeat=1000", "name=UNSET", "pass=UNSET", "debug=false", "removedata=false", "lang=en_US"
		};
		String[] data = new String[defaults.length];
        try {
            File folder = configDir;
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
			logger.info(loc.getString("ConfigLoaded"));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return data;
	}
	
	@Listener
	public void onServerStop(StoppedGameEvent event) {
		saveData();
        debugger.close();
		loc = null;
	}
	
	private void workData() throws IOException {
		File folder = configDir;
		File data = new File(folder + File.separator + "data.txt");
        boolean remove = this.remove;
        if (remove == true) {
        	if(data.delete()){
    			logger.info(loc.getString("DataRemoved"));
			} else logger.warn(loc.getString("DataRemoveNotFound"));
        } else {
    		loadData();
        }
	}
	
	public void saveData() {
		try{
			OutputStream os = new FileOutputStream(new File(configDir, "data.txt"));
			PrintStream ps = new PrintStream(os);
			for(String s : oq) {
				ps.println("oq:" + s);
			}
			ps.println("qc:" + String.valueOf(qc));
			ps.close();
			logger.info(loc.getString("DataSaved"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void loadData() {
		try{
			File file = new File(configDir, "data.txt");
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
					logger.info(loc.getString("DataLoaded"));
				} finally {
					br.close();
				}
			} else {
				logger.warn(loc.getString("DataNotfound"));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
