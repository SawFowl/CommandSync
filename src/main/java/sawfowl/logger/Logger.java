package sawfowl.logger;

import java.io.IOException;

import net.kyori.adventure.text.Component;

public interface Logger {

	static Logger getLogger(String name) {
		try {
			Class.forName("org.apache.logging.log4j.Logger");
			return new ApacheLogger(name);
		} catch (Exception e) {
			return new JavaLogger(name);
		}
	}

	void info(String string);

	void info(Component component);

	void warn(String string);

	void warn(Component component);

	void error(String string);

	void error(Component component);

	void warn(String string, IOException ex);

	void warn(String string, Object... args);

	void error(String string, Object... args);

}
