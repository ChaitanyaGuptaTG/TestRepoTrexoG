package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

	// this class is written for logging purpose when test cases run through the
	// CI/CD.
	// Author : Yash Shrivastava

	private static final Logger logger = LogManager.getLogger(Log.class);

	public static void info(String message) {
		logger.info(message);
	}

	public static void warn(String message) {
		logger.warn(message);
	}

	public static void error(String message) {
		logger.error(message);
	}

	public static void debug(String message) {
		logger.debug(message);
	}
}