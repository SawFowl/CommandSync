package Mr_Krab.CommandSyncClient.Bukkit;

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
 * Скорее всего, это финальная версия класса. 
 * Окей, что это? Это класс-помошник, который поможет вам разобраться
 * со своими языковыми пакетами. Т.е. вместо использования yaml файлов,
 * которые зависимы от кодировки чтения файла, вы можете спокойно писать
 * свои локали в .properties файл не заботясь об кодировке файла вообще.
 * http://java-properties-editor.com/ - прога для редактирования ;) 
 *
 * Т.к. писался этот класс для использования в плагинах под Bukkit API,
 * тут используются методы из вышеупомянутого api. Но ничего не стоит 
 * переписать/удалить пару моментов под другие приложения.
 *
 * @author Dereku
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
     * Инициализация класса. Должно вызываться в первую очередь.
     * В противном случае вы будете получать Key "key" does not exists!
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
     * Получение сообщения из конфигурации
     * Пример сообщения: "There is so many players."
     * Пример вызова: getString("key");
     *
     * @param key ключ сообщения
     * @return сообщение, иначе null
     */
    public String getString(final String key) {
        return this.getString(key, false, "");
    }

    /**
     * Получение сообщения с аргументами из конфигурации
     * Пример сообщения: "There is {0} players: {1}."
     * Пример вызова: getString("key", "2", "You, Me");
     *
     * @param key ключ сообщения
     * @param args аргументы сообщения
     * @return сообщение, иначе null
     */
    public String getString(final String key, final String... args) {
        return this.getString(key, false, args);
    }

    /**
     * Получение сообщения из конфигурации с возможностью фильтрации цвета
     * Пример сообщения: "\u00a76There is so many players."
     * Пример вызова: getString("key", false);
     *
     * @param key ключ сообщения
     * @param removeColors если true, то цвета будут убраны
     * @return сообщение, иначе null
     */
    public String getString(final String key, final boolean removeColors) {
        return this.getString(key, removeColors, "");
    }

    /**
     * Получение сообщения с аргументами из конфигурации с возможностью фильтрации цвета
     * Пример сообщения: "\u00a76There is \u00a7c{0} \u00a76players:\u00a7c {1}."
     * Пример вызова: getString("key", false, "2", "You, Me");
     *
     * @param key ключ сообщения
     * @param removeColors если true, то цвета будут убраны
     * @param args аргументы сообщения
     * @return сообщение, иначе null
     */
    public String getString(final String key, final boolean removeColors, final String... args) {
        String out = this.locale.getProperty(key);
        if (out == null) {
            return ChatColor.RED + "Key \"" + key + "\" not found!";
        }

        MessageFormat mf = this.messageCache.get(out);
        if (mf == null) {
            mf = new MessageFormat(out);
            this.messageCache.put(out, mf);
        }
        
        out = mf.format(args);

        if (removeColors) {
            out = ChatColor.stripColor(out);
        }
        
        return out;
    }

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