package Mr_Krab.CommandSyncClient.Sponge;

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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.inject.Inject;

import Mr_Krab.CommandSyncClient.Sponge.Locale;

@Plugin(id = "commandsync",
		name = "Command Sync",
		version = "2.5",
		authors = "Original - fuzzoland \n Sponge version - Mr_Krab",
		description = "Synchronize commands across servers")
public class CSC{

	public ClientThread client;
	public List<String> oq = Collections.synchronizedList(new ArrayList<String>());
	public Integer qc = 0;
	public String spacer = "@#@";
	public Debugger debugger;
	private static CSC instance;
	public Locale loc;
	private boolean remove;
		
	@Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;
	
	public MessageChannel consoleMessage(){
		return MessageChannel.TO_CONSOLE;
	}
		
    public void registerCommands() {
    	CommandSpec toAllPlayers = CommandSpec.builder()
    	        .permission("sync.use")
    	        .arguments(GenericArguments.remainingRawJoinedStrings(TextSerializers.FORMATTING_CODE.deserialize("command")))
    	        .executor((src, args) -> {
    	            String command = args.<String>getOne(TextSerializers.FORMATTING_CODE.deserialize("command")).get();
    	    		src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("SyncingCommand") + command + loc.getString("To") + "player [ALL]")); 
    	            oq.add("player" + spacer + "all" + spacer + command);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec playerSpec = CommandSpec.builder()
    	        .permission("sync.use")
    	        .child(toAllPlayers, "all")
    	        .arguments(GenericArguments.string(TextSerializers.FORMATTING_CODE.deserialize("player")),
    	                GenericArguments.remainingRawJoinedStrings(TextSerializers.FORMATTING_CODE.deserialize("command")))
    	        .executor((src, args) -> {
    	            String player = args.<String>getOne(TextSerializers.FORMATTING_CODE.deserialize("player")).get();
    	            String command = args.<String>getOne(TextSerializers.FORMATTING_CODE.deserialize("command")).get();
    	    		src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("SyncingCommand") + command + loc.getString("To") + "[" + player + "]")); 
    	            oq.add("player" + spacer + "single" + spacer + command + spacer + player);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec toAllServers = CommandSpec.builder()
    	        .permission("sync.use")
    	        .arguments(GenericArguments.remainingRawJoinedStrings(TextSerializers.FORMATTING_CODE.deserialize("command")))
    	        .executor((src, args) -> {
    	            String command = args.<String>getOne(TextSerializers.FORMATTING_CODE.deserialize("command")).get();
    	    		src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("SyncingCommand") + command + loc.getString("To") + "console [ALL]")); 
    	            oq.add("console" + spacer + "all" + spacer + command);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec toBungee = CommandSpec.builder()
    	        .permission("sync.use")
    	        .arguments(GenericArguments.remainingRawJoinedStrings(TextSerializers.FORMATTING_CODE.deserialize("command")))
    	        .executor((src, args) -> {
    	            String command = args.<String>getOne(TextSerializers.FORMATTING_CODE.deserialize("command")).get();
    	    		src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("SyncingCommand") + command + loc.getString("To") + "console [Bungee]")); 
	            	oq.add("console" + spacer + "bungee" + spacer + command);
    	            return CommandResult.success();
    	        })
    	        .build();

    	CommandSpec consoleSpec = CommandSpec.builder()
    	        .permission("sync.use")
    	        .child(toAllServers, "all")
    	        .child(toBungee, "bungee")
    	        .arguments(GenericArguments.string(TextSerializers.FORMATTING_CODE.deserialize("server")),
    	                GenericArguments.remainingRawJoinedStrings(TextSerializers.FORMATTING_CODE.deserialize("command")))
    	        .executor((src, args) -> {
    	            String server = args.<String>getOne(TextSerializers.FORMATTING_CODE.deserialize("server")).get();
    	            String command = args.<String>getOne(TextSerializers.FORMATTING_CODE.deserialize("command")).get();
    	    		src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("SyncingCommand") + command + loc.getString("To") + "console [" + server + "]")); 
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
		String[] data = loadConfig();
		if(data[3].equals("UNSET") || data[4].equals("UNSET")) {
			consoleMessage().send((TextSerializers.FORMATTING_CODE.deserialize(loc.getString("UnsetValues"))));
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
			"ip=localhost", "port=9190", "heartbeat=1000", "name=UNSET", "pass=UNSET", "debug=false", "removedata=false", "lang=en_US"
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
    		consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("ConfigLoaded")));
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
    			consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("DataRemoved")));
			} else consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("DataRemoveNotFound")));
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
			consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("DataSaved")));
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
					consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("DataLoaded")));
				} finally {
					br.close();
				}
			} else {
				consoleMessage().send(TextSerializers.FORMATTING_CODE.deserialize(loc.getString("DataNotfound")));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
		
	public static CSC getInstance() {
        return instance;
    }
}
