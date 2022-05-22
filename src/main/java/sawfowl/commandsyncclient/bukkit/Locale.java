package sawfowl.commandsyncclient.bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.bukkit.ChatColor;

/**
 * Most likely, this is the final version of the class..  <br>
 * Okay, what is this? This is an assistant class that will help you understand your language packs. <br>
 * Those. Instead of using yaml/hocon files that are dependent on the read encoding of the file, 
 * you can safely write your locales in the .properties file without 
 * worrying about encoding the file at all. <br>
 * http://java-properties-editor.com/ - program for editing ;) <br>
 * <br>
 * Class rewritten for use in plugins written on Sponge API,
 * here, the methods from the above api are used. But it is not worth 
 * it to rewrite / delete a couple of points for other applications.
 *
 * @author Dereku
 * @update SawFowl
 */
public class Locale {

    private final HashMap<String, MessageFormat> messageCache = new HashMap<>();
    private final Properties locale = new Properties();
    private File localeFile;
    private String loc;

    CSC instance;
    public Locale(CSC plugin, String string) {
    	instance = plugin;
    	loc = string;
    }

    /**
     * Initializing the class. Must be called first. <br>
     * Otherwise, you will receive Key "key" does not exists!
     */
    public void init() {
        this.locale.clear();
        String loc = this.loc;
        this.localeFile = new File(instance.getDataFolder(), loc + ".properties");
        
        if (this.saveLocale(loc)) {
            try (FileReader fr = new FileReader(this.localeFile)) {
                this.locale.load(fr);
            } catch (Exception ex) {
            	instance.getLogger().log(Level.WARNING, "Failed to load " + loc + " locale!", ex);
            }
        } else {
            try {
                this.locale.load(instance.getResource("en_US.properties"));
            } catch (IOException ex) {
            	instance.getLogger().log(Level.WARNING, "Failed to load en_US locale!", ex);
            }
        }
    }

    /**
     * Receiving a message from the configuration <br>
     * Example message: "There is so many players." <br>
     * Example call: getString("key");
     *
     * @param key - message key
     * @return message, otherwise null
     */
    public String getString(final String key) {
        return this.getString(key, false, "");
    }

    /**
     * Receiving a message with arguments from the configuration <br>
     * Example message: "There is {0} players: {1}." <br>
     * Example call: getString("key", "2", "You, Me");
     *
     * @param key - message key
     * @param args - arguments of the message. Only String format.
     * @return message, otherwise null
     */
    public String getString(final String key, final String... args) {
        return this.getString(key, false, args);
    }

    /**
     * Receiving a message from a configuration with the ability to filter color <br>
     * Example message: "\u00a76There is so many players." <br>
     * Example call: getString("key", false);
     *
     * @param key - message key
     * @param removeColors if true, then the colors will be removed
     * @return message, otherwise null
     */
    public String getString(final String key, final boolean removeColors) {
        return this.getString(key, removeColors, "");
    }

    /**
     * Receiving a message with arguments from a configuration with the ability to filter the color <br>
     * Example message: "\u00a76There is \u00a7c{0} \u00a76players:\u00a7c {1}." <br>
     * Example call: getString("key", false, "2", "You, Me"); <br>
     *
     * @param key - message key
     * @param removeColors - if true, then the colors will be removed
     * @param args - arguments of the message. Only String format.
     * @return message, otherwise null
     */
    public String getString(final String key, final boolean removeColors, final String... args) {
        String out = this.locale.getProperty(key);
        if (out == null) {
            return ChatColor.RED + "Key \"" + key + "\" not found!" + ChatColor.RESET;
        }

        MessageFormat mf = this.messageCache.get(out);
        if (mf == null) {
            mf = new MessageFormat(out);
            this.messageCache.put(out, mf);
        }
        
        out = mf.format(args);
        
        out = ChatColor.translateAlternateColorCodes('&', out);
        
        if (removeColors) {
            out = ChatColor.stripColor(out);
        }
        
        return out;
    }
    
    /**
     * Saving the localization file. <br>
     * The choice of a specific localization is determined by
     * the parameter in the main configuration file. <br>
     * If the selected localization does not exist, 
     * the default file will be saved(en_US.properties). <br>
     * The path to the localization file can be changed. <br>
     * 
     * @path - The path to the localization file in the *.jar file. <br>
     * @name - The name of the localization file specified in the main configuration file. <br>
     * @is - Localization file name + .properties
     */
	private boolean saveLocale(final String name) {
        if (this.localeFile.exists()) {
            return true;
        }
        File enFile = new File(instance.getDataFolder() + File.separator + "en_US.properties");
        String is = "/" + name + ".properties";
        if (getClass().getResource(is) == null) {
        	instance.getLogger().log(Level.WARNING, "Failed to save \"" + name + ".properties \"");
            if (!enFile.exists()){
            try {
                URI u = getClass().getResource("/en_US.properties").toURI();
                FileSystem jarFS = FileSystems.newFileSystem(URI.create(u.toString().split("!")[0]), new HashMap<>());
    			Files.copy(jarFS.getPath(u.toString().split("!")[1]), new File(instance.getDataFolder() + File.separator + "en_US.properties").toPath());
    	        jarFS.close();
    		} catch (IOException ex) {
            	instance.getLogger().log(Level.WARNING, "Failed to save \"" + name + ".properties \"", ex);
    		} catch (URISyntaxException e) {
            	instance.getLogger().log(Level.WARNING, "Locale \"{0}\" does not exists!\"", name);
    		}
            return false;
            }
            return false;
        } else {    
        try {
            URI u = getClass().getResource(is).toURI();
            FileSystem jarFS = FileSystems.newFileSystem(URI.create(u.toString().split("!")[0]), new HashMap<>());
			Files.copy(jarFS.getPath(u.toString().split("!")[1]), new File(instance.getDataFolder() + File.separator + name + ".properties").toPath());
	        jarFS.close();
		} catch (IOException ex) {
        	instance.getLogger().log(Level.WARNING, "Failed to save \"" + name + ".properties \"", ex);
		} catch (URISyntaxException e) {
        	instance.getLogger().log(Level.WARNING, "Locale \"{0}\" does not exists!\"", name);
		}	
        }
        return true;
    }
}