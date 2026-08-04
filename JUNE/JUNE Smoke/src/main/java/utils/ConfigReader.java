package utils;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
	// Reading the config properties.
	// Author : Yash Shrivastava
	private static Properties properties;

	public static synchronized Properties loadConfig() {
		if (properties != null) {
			return properties;
		}

		Properties props = new Properties();
		try {
			String env = System.getProperty("env", "qa");

			// SEC-04: Prevent Path Traversal by sanitizing the 'env' variable
			if (env == null || !env.matches("^[a-zA-Z0-9_-]+$")) {
				throw new IllegalArgumentException("Invalid environment name: " + env);
			}

			// 1. Load the environment specific properties file
			String envConfigPath = "src/test/resources/config-" + env + ".properties";
			try (FileInputStream fis = new FileInputStream(envConfigPath)) {
				props.load(fis);
			} catch (java.io.FileNotFoundException e) {
				Log.warn("Environment config file not found: " + envConfigPath + ". Proceeding with local/system config.");
			}

			// 2. Load local properties file (git-ignored) if it exists
			java.io.File localFile = new java.io.File("src/test/resources/config-local.properties");
			if (localFile.exists()) {
				try (FileInputStream fis = new FileInputStream(localFile)) {
					props.load(fis);
				} catch (Exception e) {
					Log.error("Error loading local config: " + e.getMessage());
				}
			}

			// 3. Resolve/override with environment variables or JVM system properties
			resolveOverrides(props);

			properties = props;

		} catch (Exception e) {
			Log.error("Failed to load configuration: " + e.getMessage());
		}
		return properties;
	}

	private static void resolveOverrides(Properties props) {
		for (String key : props.stringPropertyNames()) {
			// Check JVM system properties (highest priority)
			String sysPropVal = System.getProperty(key);
			if (sysPropVal != null && !sysPropVal.isBlank()) {
				props.setProperty(key, sysPropVal);
				continue;
			}

			// Check environment variable (middle priority)
			// Map key dot-notation to uppercase env var name (e.g. workflow.downloadWaitSeconds -> WORKFLOW_DOWNLOADWAITSECONDS)
			String envVarName = key.replace(".", "_").toUpperCase();
			if (!envVarName.equals("USERNAME") && !envVarName.equals("USER")) {
				String envVarVal = System.getenv(envVarName);
				if (envVarVal != null && !envVarVal.isBlank()) {
					props.setProperty(key, envVarVal);
				}
			}
		}
	}


}
