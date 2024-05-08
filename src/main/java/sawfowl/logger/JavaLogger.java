package sawfowl.logger;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class JavaLogger implements sawfowl.logger.Logger {

	private Logger logger;
	public JavaLogger(String name) {
		logger = Logger.getLogger(name);
	}

	@Override
	public void info(String string) {
		logger.info(string);
	}

	@Override
	public void info(Component component) {
		info(toPlain(component));
	}

	@Override
	public void warn(String string) {
		logger.log(Level.WARNING, string);
	}

	@Override
	public void warn(Component component) {
		warn(toPlain(component));
	}

	@Override
	public void error(String string) {
		logger.log(Level.SEVERE, string);
	}

	@Override
	public void error(Component component) {
		error(toPlain(component));
	}

	@Override
	public void warn(String string, IOException ex) {
		logger.log(Level.SEVERE, string, ex);
	}

	@Override
	public void warn(String string, Object... args) {
		logger.log(Level.SEVERE, string, args);
	}

	@Override
	public void error(String string, Object... args) {
		logger.log(Level.SEVERE, string, args);;
	}

	private String toPlain(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

}
