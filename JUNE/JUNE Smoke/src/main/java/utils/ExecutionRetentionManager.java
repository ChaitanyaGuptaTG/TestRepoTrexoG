package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExecutionRetentionManager {

	public static void applyRetentionPolicy(String currentRunId) {
		Log.info("Execution Retention Started");

		int retentionLimit = 5;
		try {
			Properties config = ConfigReader.loadConfig();
			String limitProp = config.getProperty("workflow.retentionLimit");
			if (limitProp != null && !limitProp.isBlank()) {
				retentionLimit = Integer.parseInt(limitProp.trim());
			}
		} catch (Exception e) {
			Log.warn("Failed to load workflow.retentionLimit from config, defaulting to 5. Error: " + e.getMessage());
		}

		Path resultsDir = Paths.get("ExecutionResults").toAbsolutePath().normalize();
		if (!Files.exists(resultsDir) || !Files.isDirectory(resultsDir)) {
			Log.info("Total Runs Found: 0");
			Log.info("Retention Limit: " + retentionLimit);
			Log.info("Runs Retained: 0");
			Log.info("Runs Deleted: 0");
			Log.info("Execution Retention Completed Successfully");
			return;
		}

		List<Path> runDirs = new ArrayList<>();
		try (Stream<Path> stream = Files.list(resultsDir)) {
			runDirs = stream
					.filter(Files::isDirectory)
					.filter(path -> {
						String name = path.getFileName().toString();
						// Regex check to ensure it's a framework-generated run folder e.g., Run_20260605_111735
						return name.matches("Run_\\d{8}_\\d{6}");
					})
					.collect(Collectors.toList());
		} catch (IOException e) {
			Log.error("Error listing execution result folders in " + resultsDir + ": " + e.getMessage());
			return;
		}

		// Sort lexicographically in descending order (latest first)
		runDirs.sort((p1, p2) -> p2.getFileName().toString().compareTo(p1.getFileName().toString()));

		int totalRuns = runDirs.size();
		int deletedCount = 0;
		int retainedCount = 0;

		List<Path> toDelete = new ArrayList<>();
		List<Path> toRetain = new ArrayList<>();

		for (Path runDir : runDirs) {
			String runId = runDir.getFileName().toString();
			if (runId.equals(currentRunId)) {
				// Current run is always retained
				toRetain.add(runDir);
				retainedCount++;
			} else if (retainedCount < retentionLimit) {
				toRetain.add(runDir);
				retainedCount++;
			} else {
				toDelete.add(runDir);
			}
		}

		Log.info("Total Runs Found: " + totalRuns);
		Log.info("Retention Limit: " + retentionLimit);
		Log.info("Runs Retained: " + retainedCount);

		for (Path runDir : toDelete) {
			try {
				// Defensive safeguard: Verify parent is indeed ExecutionResults, name is correct, and it is a directory
				Path parent = runDir.getParent();
				if (parent != null && parent.equals(resultsDir) && runDir.getFileName().toString().matches("Run_\\d{8}_\\d{6}")) {
					deleteDirectoryRecursively(runDir);
					deletedCount++;
				} else {
					Log.warn("Defensive check failed: skipped deleting " + runDir + " because it is not a direct child of ExecutionResults or name format is incorrect.");
				}
			} catch (Exception e) {
				Log.error("Error during execution retention cleanup of folder " + runDir + ": " + e.getMessage());
			}
		}

		Log.info("Runs Deleted: " + deletedCount);
		Log.info("Execution Retention Completed Successfully");
	}

	private static void deleteDirectoryRecursively(Path dir) throws IOException {
		try (Stream<Path> walk = Files.walk(dir)) {
			List<Path> paths = walk.sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList());
			for (Path p : paths) {
				Files.deleteIfExists(p);
			}
		}
	}
}
