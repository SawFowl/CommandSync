package sawfowl.logger;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ApacheLogger implements Logger {

	private org.apache.logging.log4j.Logger logger;
	public ApacheLogger(String name) {
		logger = LogManager.getLogger(name);
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
		logger.warn(string);
	}

	@Override
	public void warn(Component component) {
		warn(toPlain(component));
	}

	@Override
	public void error(String string) {
		logger.error(string);
	}

	@Override
	public void error(Component component) {
		error(toPlain(component));
	}

	@Override
	public void warn(String string, IOException ex) {
		logger.warn(string, ex);
	}

	@Override
	public void warn(String string, Object... args) {
		logger.warn(string, args);
	}

	@Override
	public void error(String string, Object... args) {
		logger.error(string, args);
	}

	private String toPlain(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

}
