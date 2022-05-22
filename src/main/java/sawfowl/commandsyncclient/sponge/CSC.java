package sawfowl.commandsyncclient.sponge;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import com.google.inject.Inject;

@Plugin(id = "commandsync",
		name = "Command Sync",
		version = "2.6.0",
		authors = "Original - fuzzoland\nSponge version - SawFowl",
		description = "Synchronize commands across servers")
public class CSC{

	public ClientThread client;
	public List<String> oq = Collections.synchronizedList(new ArrayList<String>());
	public Integer qc = 0;
	public String spacer = "@#@";
	public Debugger debugger;
	private static CSC instance;
	private Locale loc;
	private boolean remove;
	Logger logger;
		
	@Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;
	
	public MessageChannel consoleMessage(){
		return MessageChannel.TO_CONSOLE;
	}

	public Locale getLocale() {
		return loc;
	}
	
    public void registerCommands() {
    	CommandSpec toAllPlayers = CommandSpec.builder()
    	        .permission("sync.use")
    	        .arguments(GenericArguments.remainingRawJoinedStrings(Text.of("command")))
    	        .executor((src, args) -> {
    	            String command = args.<String>getOne(Text.of("command")).get();
    	            String p = loc.getLegacyString("SyncPlayerAll");
    	    		src.sendMessage(loc.getString("SyncingCommand", command, p)); 
    	            oq.add("player" + spacer + "all" + spacer + command);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec playerSpec = CommandSpec.builder()
    	        .permission("sync.use")
    	        .child(toAllPlayers, "all")
    	        .arguments(GenericArguments.string(Text.of("player")),
    	                GenericArguments.remainingRawJoinedStrings(Text.of("command")))
    	        .executor((src, args) -> {
    	            String player = args.<String>getOne(Text.of("player")).get();
    	            String command = args.<String>getOne(Text.of("command")).get();
    	            String p = loc.getLegacyString("SyncPlayer", player);
    	    		src.sendMessage(loc.getString("SyncingCommand", command, p)); 
    	            oq.add("player" + spacer + "single" + spacer + command + spacer + player);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec toAllServers = CommandSpec.builder()
    	        .permission("sync.use")
    	        .arguments(GenericArguments.remainingRawJoinedStrings(Text.of("command")))
    	        .executor((src, args) -> {
    	            String command = args.<String>getOne(Text.of("command")).get();
    	            String s = loc.getLegacyString("SyncConsoleAll");
    	    		src.sendMessage(loc.getString("SyncingCommand", command, s)); 
    	            oq.add("console" + spacer + "all" + spacer + command);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec toBungee = CommandSpec.builder()
    	        .permission("sync.use")
    	        .arguments(GenericArguments.remainingRawJoinedStrings(Text.of("command")))
    	        .executor((src, args) -> {
    	            String command = args.<String>getOne(Text.of("command")).get();
    	            String s = loc.getLegacyString("SyncConsole", "Bungee");
    	    		src.sendMessage(loc.getString("SyncingCommand", command, s)); 
	            	oq.add("console" + spacer + "bungee" + spacer + command);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec consoleSpec = CommandSpec.builder()
    	        .permission("sync.use")
    	        .child(toAllServers, "all")
    	        .child(toBungee, "bungee")
    	        .arguments(GenericArguments.string(Text.of("server")),
    	                GenericArguments.remainingRawJoinedStrings(Text.of("command")))
    	        .executor((src, args) -> {
    	            String server = args.<String>getOne(Text.of("server")).get();
    	            String command = args.<String>getOne(Text.of("command")).get();
    	            String s = loc.getLegacyString("SyncConsole", server);
    	    		src.sendMessage(loc.getString("SyncingCommand", command, s)); 
    	            oq.add("console" + spacer + "single" + spacer + command + spacer + server);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec spec = CommandSpec.builder()
    	        .permission("sync.use")
    	        .child(consoleSpec, "console")
    	        .child(playerSpec, "player")
    	        .build();

    	Sponge.getCommandManager().register(this, spec, "sync");

    }
    
	@Listener
	public void onServerStart(GamePreInitializationEvent event) {
		logger = (Logger)LoggerFactory.getLogger("CommandSync");
		String[] data = loadConfig();
		if(data[3].equals("UNSET") || data[4].equals("UNSET")) {
			consoleMessage().send(loc.getString("UnsetValues"));
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
		registerCommands();
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
    		consoleMessage().send(loc.getString("ConfigLoaded"));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return data;
	}
	
	@Listener
	public void onServerStop(GameStoppedEvent event) {
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
    			consoleMessage().send(loc.getString("DataRemoved"));
			} else consoleMessage().send(loc.getString("DataRemoveNotFound"));
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
			consoleMessage().send(loc.getString("DataSaved"));
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
					consoleMessage().send(loc.getString("DataLoaded"));
				} finally {
					br.close();
				}
			} else {
				consoleMessage().send(loc.getString("DataNotfound"));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
		
	public static CSC getInstance() {
        return instance;
    }
}
