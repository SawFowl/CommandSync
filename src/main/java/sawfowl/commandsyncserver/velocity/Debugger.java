package sawfowl.commandsyncserver.velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Debugger {

    private PrintStream ps;
    private Boolean log;
    
    public Debugger(CSS plugin, Boolean log) {
        this.log = log;
        if(log) {
            try {
                File file = new File(plugin.getDataFolder(), "log.txt");
                if(!file.exists()) {
                    file.createNewFile();
                }
                ps = new PrintStream(new FileOutputStream(file));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void debug(String debug) {
        System.out.println("[CommandSync] " + debug);
        if(log) {
            ps.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + ": " + debug);
        }
    }
    
    public void close() {
        if(log) {
            ps.close();
        }
    }
}
