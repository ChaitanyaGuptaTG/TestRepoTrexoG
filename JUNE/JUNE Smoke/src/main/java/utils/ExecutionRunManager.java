package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExecutionRunManager {
	private static final String RUN_ID;
	private static final Path RUN_ROOT;
	private static final Path DOWNLOADS_DIR;
	private static final Path COMPARISON_REPORTS_DIR;
	private static final Path RETRY_ARTIFACTS_DIR;
	private static final Path LOGS_DIR;
	private static boolean runSpecificEnabled = true;

	static {
		try {
			// Optional: read from system property to allow runtime disabling
			String prop = System.getProperty("workflow.runSpecificEnabled");
			if (prop != null) {
				runSpecificEnabled = Boolean.parseBoolean(prop);
			}
		} catch (Exception ignored) {}

		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		RUN_ID = "Run_" + timestamp;
		RUN_ROOT = Paths.get("ExecutionResults", RUN_ID).toAbsolutePath().normalize();
		DOWNLOADS_DIR = RUN_ROOT.resolve("Downloads");
		COMPARISON_REPORTS_DIR = RUN_ROOT.resolve("ComparisonReports");
		RETRY_ARTIFACTS_DIR = RUN_ROOT.resolve("RetryArtifacts");
		LOGS_DIR = RUN_ROOT.resolve("Logs");

		if (runSpecificEnabled) {
			try {
				Files.createDirectories(RUN_ROOT);
				Files.createDirectories(DOWNLOADS_DIR);
				Files.createDirectories(DOWNLOADS_DIR.resolve("incoming"));
				Files.createDirectories(DOWNLOADS_DIR.resolve("generated"));
				Files.createDirectories(COMPARISON_REPORTS_DIR);
				Files.createDirectories(RETRY_ARTIFACTS_DIR);
				Files.createDirectories(LOGS_DIR);

				// Apply execution retention policy
				ExecutionRetentionManager.applyRetentionPolicy(RUN_ID);
			} catch (IOException e) {
				System.err.println("Failed to initialize ExecutionRunManager directories: " + e.getMessage());
			}
		}
	}

	public static boolean isRunSpecificEnabled() {
		return runSpecificEnabled;
	}

	public static String getRunId() {
		return RUN_ID;
	}

	public static Path getRunRoot() {
		return RUN_ROOT;
	}

	public static Path getDownloadsDir() {
		return DOWNLOADS_DIR;
	}

	public static Path getComparisonReportsDir() {
		return COMPARISON_REPORTS_DIR;
	}

	public static Path getRetryArtifactsDir() {
		return RETRY_ARTIFACTS_DIR;
	}

	public static Path getLogsDir() {
		return LOGS_DIR;
	}
}
