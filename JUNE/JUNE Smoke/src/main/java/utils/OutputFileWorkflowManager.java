package utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OutputFileWorkflowManager {

	private static final String DEFAULT_OUTPUT_ROOT = "output";
	private static final String DEFAULT_SOURCE_ROOT = "source";
	private static final Pattern SCENARIO_NUMBER_PATTERN = Pattern.compile("(\\d+)");

	private final Properties config;
	private final String workflowKey;
	private final String intentName;
	private final Path incomingDir;
	private final Path generatedOutputDir;
	private final Path workflowSourceDir;
	private final Map<String, ScenarioVerification> scenarios = new LinkedHashMap<>();

	public OutputFileWorkflowManager(Properties config, String workflowKey) throws IOException {
		this.config = config;
		this.workflowKey = sanitizePathSegment(workflowKey);
		this.intentName = resolveIntentName(config, this.workflowKey);
		this.incomingDir = resolveIncomingDownloadDirectory(config);
		this.generatedOutputDir = resolveGeneratedOutputDirectory(config);
		String configuredSourceDir = config.getProperty(this.workflowKey + ".sourceDir");
		this.workflowSourceDir = configuredSourceDir == null || configuredSourceDir.isBlank()
				? resolveSourceRoot(config)
				: Paths.get(configuredSourceDir).toAbsolutePath().normalize();

		cleanupPreviousRunFiles();

		if ("ustmimagedownload".equals(this.workflowKey)) {
			ImageComparisonReportStore.clear();
		}

		Files.createDirectories(incomingDir);
		Files.createDirectories(generatedOutputDir);
		Files.createDirectories(workflowSourceDir);
	}

	public static void resetWorkspace(Properties config) throws IOException {
		deleteDirectoryIfExists(resolveIncomingDownloadDirectory(config));
		deleteDirectoryIfExists(resolveGeneratedOutputDirectory(config));
		Files.createDirectories(resolveIncomingDownloadDirectory(config));
		Files.createDirectories(resolveGeneratedOutputDirectory(config));
		Files.createDirectories(resolveSourceRoot(config));
	}

	public static Path resolveIncomingDownloadDirectory(Properties config) {
		if (ExecutionRunManager.isRunSpecificEnabled()) {
			return ExecutionRunManager.getDownloadsDir().resolve("incoming").toAbsolutePath().normalize();
		}
		return resolveOutputRoot(config).resolve("incoming").toAbsolutePath().normalize();
	}

	public static Path resolveGeneratedOutputDirectory(Properties config) {
		if (ExecutionRunManager.isRunSpecificEnabled()) {
			return ExecutionRunManager.getDownloadsDir().resolve("generated").toAbsolutePath().normalize();
		}
		return resolveOutputRoot(config).resolve("generated").toAbsolutePath().normalize();
	}

	public static Path resolveOutputRoot(Properties config) {
		String legacyDownloadRoot = config.getProperty("workflow.download.root");
		String configuredOutputRoot = config.getProperty("workflow.output.root", legacyDownloadRoot);
		return Paths.get(configuredOutputRoot == null || configuredOutputRoot.isBlank() ? DEFAULT_OUTPUT_ROOT
				: configuredOutputRoot).toAbsolutePath().normalize();
	}

	public static Path resolveDownloadRoot(Properties config) {
		return resolveOutputRoot(config);
	}

	public static Path resolveSourceRoot(Properties config) {
		return Paths.get(config.getProperty("workflow.source.root", DEFAULT_SOURCE_ROOT)).toAbsolutePath().normalize();
	}

	public DownloadSnapshot snapshotDownloads() throws IOException {
		return new DownloadSnapshot(listCurrentIncomingFiles());
	}

	public Path captureDownloadedFile(String scenarioKey, DownloadSnapshot snapshot, String filenameContains,
			int timeoutSeconds) throws IOException, InterruptedException {
		return captureDownloadedFile(scenarioKey, null, snapshot, filenameContains, timeoutSeconds);
	}

	public Path captureDownloadedFile(String scenarioKey, String requestId, DownloadSnapshot snapshot,
			String filenameContains, int timeoutSeconds) throws IOException, InterruptedException {
		String normalizedScenarioKey = sanitizePathSegment(scenarioKey);
		ScenarioVerification scenario = scenarios.computeIfAbsent(normalizedScenarioKey, key -> new ScenarioVerification(
				key, VerificationOptions.fromConfig(config, workflowKey, workflowSourceDir, key, intentName)));
		scenario.setRequestId(requestId);

		// New file download initiated log
		Log.info("[OutputWorkflow] New file download initiated for workflow: " + workflowKey 
				+ " | Scenario: " + scenarioKey + " | Request ID: " + valueOrUnknown(requestId));

		Path downloadedFile = waitForNewIncomingFile(snapshot.getKnownFiles(), filenameContains, requestId,
				timeoutSeconds);
		if (downloadedFile == null) {
			String errMsg = "Fresh file download failed! No downloaded file found in " + incomingDir 
					+ " for scenario " + scenarioKey + " and Request ID " + valueOrUnknown(requestId) 
					+ " within " + timeoutSeconds + " seconds.";
			Log.error("[OutputWorkflow] " + errMsg);
			try {
				org.openqa.selenium.WebDriver activeDriver = getActiveDriver();
				if (activeDriver != null) {
					String ssPath = ScreenshotUtil.captureScreenshot(activeDriver, "DownloadFailed_" + scenarioKey);
					Log.error("[OutputWorkflow] Captured download failure screenshot: " + ssPath);
				}
			} catch (Exception se) {
				Log.warn("[OutputWorkflow] Failed to capture screenshot for download failure: " + se.getMessage());
			}
			throw new IllegalStateException(errMsg);
		}

		// New file download completed log
		Log.info("[OutputWorkflow] New file download completed. File found: " + downloadedFile.getFileName());

		// Fresh File Validation: Verify request ID if present
		if (requestId != null && !requestId.isBlank()) {
			String filename = downloadedFile.getFileName().toString();
			if (!filename.contains(requestId)) {
				String errMsg = "Fresh file validation failed! The downloaded file '" + filename
						+ "' does not match the current Request ID: " + requestId;
				Log.error("[OutputWorkflow] " + errMsg);
				try {
					org.openqa.selenium.WebDriver activeDriver = getActiveDriver();
					if (activeDriver != null) {
						String ssPath = ScreenshotUtil.captureScreenshot(activeDriver, "ValidationFailed_" + scenarioKey);
						Log.error("[OutputWorkflow] Captured validation failure screenshot: " + ssPath);
					}
				} catch (Exception se) {
					Log.warn("[OutputWorkflow] Failed to capture screenshot for validation failure: " + se.getMessage());
				}
				throw new IllegalStateException(errMsg);
			}
		}

		// Fresh File Validation: Verify file is not stale
		long lastModified = downloadedFile.toFile().lastModified();
		long currentMillis = System.currentTimeMillis();
		long maxAgeMillis = (timeoutSeconds + 60) * 1000L;
		if (currentMillis - lastModified > maxAgeMillis) {
			String errMsg = "Fresh file validation failed! The downloaded file '" 
					+ downloadedFile.getFileName().toString() + "' is stale (last modified: " 
					+ new java.util.Date(lastModified) + "), suggesting it is a residual file from an earlier run.";
			Log.error("[OutputWorkflow] " + errMsg);
			try {
				org.openqa.selenium.WebDriver activeDriver = getActiveDriver();
				if (activeDriver != null) {
					String ssPath = ScreenshotUtil.captureScreenshot(activeDriver, "StaleFileDetected_" + scenarioKey);
					Log.error("[OutputWorkflow] Captured stale file screenshot: " + ssPath);
				}
			} catch (Exception se) {
				Log.warn("[OutputWorkflow] Failed to capture screenshot for stale file: " + se.getMessage());
			}
			throw new IllegalStateException(errMsg);
		}

		// Safe File Replacement Logic
		Path target = generatedOutputDir.resolve(downloadedFile.getFileName().toString());
		if (Files.exists(target)) {
			Log.info("[OutputWorkflow] Safe File Replacement: Old file with same name already exists at target: " + target);
			try {
				Files.delete(target);
				Log.info("[OutputWorkflow] Safe File Replacement: Successfully deleted old file: " + target);
			} catch (IOException e) {
				Log.warn("[OutputWorkflow] Safe File Replacement: Failed to delete old file directly, archiving instead. Error: " + e.getMessage());
				Path archiveDir = generatedOutputDir.resolve("archive");
				Files.createDirectories(archiveDir);
				Path archiveTarget = resolveUniqueTarget(archiveDir, target.getFileName().toString());
				Files.move(target, archiveTarget, StandardCopyOption.REPLACE_EXISTING);
				Log.info("[OutputWorkflow] Safe File Replacement: Old file archived to: " + archiveTarget);
			}
		}

		// Retry loop to handle transient Windows file locks (antivirus, Chrome download handle)
		int moveRetries = 5;
		for (int attempt = 1; attempt <= moveRetries; attempt++) {
			try {
				Files.move(downloadedFile, target, StandardCopyOption.REPLACE_EXISTING);
				break;
			} catch (IOException e) {
				if (attempt == moveRetries) {
					Log.error("[OutputWorkflow] File move failed after " + moveRetries + " attempts: " + e.getMessage());
					throw e;
				}
				Log.warn("[OutputWorkflow] File locked, retrying move (" + attempt + "/" + moveRetries + "): " + e.getMessage());
				Thread.sleep(2000);
			}
		}
		
		// Ensure comparison engine ONLY uses the latest authoritative file
		scenario.getDownloadedFiles().clear();
		scenario.getDownloadedFiles().add(target);

		// File verification successful log
		Log.info("[OutputWorkflow] File verification successful for: " + target.getFileName());
		Log.info("[OutputWorkflow] Captured generated output for " + workflowKey + "/" + scenarioKey
				+ " | Request ID: " + valueOrUnknown(requestId) + " | File: " + target);
		return target;
	}

	public void verifyAll() throws IOException {
		if (scenarios.isEmpty()) {
			Log.info("[OutputWorkflow] No downloaded files were captured for workflow: " + workflowKey);
			return;
		}

		if ("ustmimagedownload".equals(workflowKey)) {
			verifyImages();
			return;
		}

		Log.info("[OutputWorkflow] Phase 2: Starting validation and data comparison for workflow: " + workflowKey);
		Log.info("[OutputWorkflow] Centralized Comparison process initiated for workflow: " + workflowKey);
		List<ExcelComparisonEngine.ScenarioComparisonTask> tasks = new ArrayList<>();
		
		for (ScenarioVerification scenario : scenarios.values()) {
			Path expectedFile = null;
			try {
				expectedFile = findExpectedSourceFile(scenario.getOptions());
			} catch (Exception e) {
				Log.warn("[OutputWorkflow] Could not find expected source file for scenario " + scenario.getScenarioKey() + ": " + e.getMessage());
			}

			Path actualFile = null;
			try {
				actualFile = findActualOutputFile(scenario);
			} catch (Exception e) {
				Log.warn("[OutputWorkflow] Could not find actual output file for scenario " + scenario.getScenarioKey() + ": " + e.getMessage());
			}

			tasks.add(new ExcelComparisonEngine.ScenarioComparisonTask(
				intentName + " Scenario " + scenario.getOptions().getScenarioNumber(),
				scenario.getScenarioKey(),
				expectedFile,
				actualFile,
				scenario.getOptions().getSheetIndex(),
				scenario.getOptions().getHeaderRowIndex(),
				scenario.getOptions().isStrictHeaderOrder(),
				scenario.getOptions().isCaseInsensitiveHeaders(),
				scenario.getOptions().isCaseInsensitiveData(),
				scenario.getOptions().isIgnoreRowOrder(),
				scenario.getOptions().getPrimaryKeyColumn()
			));
		}

		Path comparisonOutputDir = ExecutionRunManager.isRunSpecificEnabled()
				? ExecutionRunManager.getComparisonReportsDir()
				: Paths.get("output", "comparison").toAbsolutePath().normalize();
		ExcelComparisonEngine.ComparisonSummary summary = ExcelComparisonEngine.compareAndGenerateReport(
				intentName, workflowKey, tasks, comparisonOutputDir);

		DataComparisonReportStore.recordComparisonReport(summary.getReportPath());

		List<String> failures = new ArrayList<>();
		for (ExcelComparisonEngine.ScenarioResult res : summary.getScenarioResults()) {
			double matchPct = res.getMatchedCount() + res.getMismatchedCount() == 0 
					? (res.isMatch() ? 100.0 : 0.0)
					: (res.getMatchedCount() * 100.0) / (res.getMatchedCount() + res.getMismatchedCount());

			String requestId = null;
			ScenarioVerification scenario = scenarios.get(res.getScenarioKey());
			if (scenario != null) {
				requestId = scenario.getRequestId();
			}

			DataComparisonReportStore.ScenarioComparisonReport report = DataComparisonReportStore.ScenarioComparisonReport.custom(
					workflowKey,
					intentName,
					res.getScenarioKey(),
					resolveScenarioNumber(res.getScenarioKey()),
					requestId,
					res.getExpectedFile(),
					res.getActualFile(),
					matchPct,
					res.getDiscrepancies(),
					res.isMatch(),
					res.getMatchedCount(),
					res.getMismatchedCount(),
					summary.getReportPath()
			);

			DataComparisonReportStore.record(report);

			if (!res.isMatch()) {
				failures.add("Scenario " + res.getScenarioKey() + " | Request ID: " + valueOrUnknown(requestId) 
						+ " | Discrepancies: " + String.join(", ", res.getDiscrepancies()));
			}
		}

		if (!failures.isEmpty()) {
			String report = String.join(System.lineSeparator() + System.lineSeparator(), failures);
			throw new AssertionError("Output file verification failed for workflow " + workflowKey + ":"
					+ System.lineSeparator() + report);
		}

		Log.info("[OutputWorkflow] Output file verification passed for workflow: " + workflowKey);
	}

	private void verifyImages() throws IOException {
		Log.info("[OutputWorkflow] Phase 2: Starting visual image comparison for workflow: " + workflowKey);
		
		double ssimThreshold = 0.99;
		String thresholdProp = config.getProperty("ustmimagedownload.ssimThreshold");
		if (thresholdProp != null && !thresholdProp.isBlank()) {
			try {
				ssimThreshold = Double.parseDouble(thresholdProp.trim());
				if (ssimThreshold > 1.0) {
					ssimThreshold /= 100.0;
				}
			} catch (NumberFormatException e) {
				Log.warn("[OutputWorkflow] Invalid SSIM threshold in config: " + thresholdProp + ". Using default 0.99");
			}
		}

		Path comparisonOutputDir = ExecutionRunManager.isRunSpecificEnabled()
				? ExecutionRunManager.getComparisonReportsDir()
				: Paths.get("output", "comparison").toAbsolutePath().normalize();
		Files.createDirectories(comparisonOutputDir);

		for (ScenarioVerification scenario : scenarios.values()) {
			String scenarioKey = scenario.getScenarioKey();
			String scenarioNum = resolveScenarioNumber(scenarioKey);
			String format = scenarioKey.toLowerCase().contains("png") ? "PNG" : "JPG";

			Path actualZip = scenario.getDownloadedFiles().stream().filter(Files::exists)
					.max(Comparator.comparingLong(path -> path.toFile().lastModified()))
					.orElse(null);

			if (actualZip == null) {
				String errMsg = "Missing downloaded ZIP file for scenario: " + scenarioKey;
				Log.error("[OutputWorkflow] " + errMsg);
				ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
						scenarioKey, null, null, null, 0.0, 100.0, "N/A", "FAIL", errMsg
				));
				continue;
			}

			Path extractTempDir = generatedOutputDir.resolve("extracted").resolve(scenarioKey + "_temp");
			try {
				deleteDirectoryIfExists(extractTempDir);
				Files.createDirectories(extractTempDir);
			} catch (IOException e) {
				String errMsg = "Failed to create temp directory for extraction: " + e.getMessage();
				Log.error("[OutputWorkflow] " + errMsg);
				ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
						scenarioKey, null, actualZip, null, 0.0, 100.0, "N/A", "FAIL", errMsg
				));
				continue;
			}

			List<Path> extractedFiles;
			try {
				extractedFiles = extractZip(actualZip, extractTempDir);
			} catch (Exception e) {
				String errMsg = "Corrupted or invalid ZIP file: " + e.getMessage();
				Log.error("[OutputWorkflow] " + errMsg);
				ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
						scenarioKey, null, actualZip, null, 0.0, 100.0, "N/A", "FAIL", errMsg
				));
				continue;
			}

			if (extractedFiles.isEmpty()) {
				String errMsg = "Empty ZIP file (no files extracted) for scenario: " + scenarioKey;
				Log.error("[OutputWorkflow] " + errMsg);
				ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
						scenarioKey, null, actualZip, null, 0.0, 100.0, "N/A", "FAIL", errMsg
				));
				continue;
			}

			List<Path> actualImages = new ArrayList<>();
			for (Path file : extractedFiles) {
				String filename = file.getFileName().toString().toLowerCase();
				if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
					actualImages.add(file);
				}
			}

			if (actualImages.isEmpty()) {
				String errMsg = "No supported image files found in ZIP for scenario: " + scenarioKey;
				Log.error("[OutputWorkflow] " + errMsg);
				ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
						scenarioKey, null, actualZip, null, 0.0, 100.0, "N/A", "FAIL", errMsg
				));
				continue;
			}

			String expectedBasePattern = ("USTMImageDownload_Scenario_" + scenarioNum + "_" + format).toLowerCase();
			List<Path> expectedSourceFiles = new ArrayList<>();
			if (Files.isDirectory(workflowSourceDir)) {
				try (Stream<Path> s = Files.list(workflowSourceDir)) {
					s.filter(Files::isRegularFile).forEach(path -> {
						String fn = stripExtension(path.getFileName().toString()).toLowerCase();
						if (fn.equals(expectedBasePattern) || fn.startsWith(expectedBasePattern + "_")) {
							expectedSourceFiles.add(path);
						}
					});
				}
			}

			if (expectedSourceFiles.isEmpty()) {
				String errMsg = "Missing expected source image matching base pattern '" + expectedBasePattern + "' in source directory: " + workflowSourceDir;
				Log.error("[OutputWorkflow] " + errMsg);
				ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
						scenarioKey, null, actualImages.get(0), null, 0.0, 100.0, "N/A", "FAIL", errMsg
				));
				continue;
			}

			// Extract expected images: if source file is a ZIP, unpack its image contents
			List<Path> expectedImages = new ArrayList<>();
			for (Path srcFile : expectedSourceFiles) {
				String srcName = srcFile.getFileName().toString().toLowerCase();
				if (srcName.endsWith(".zip")) {
					Path expectedExtractDir = generatedOutputDir.resolve("extracted").resolve(scenarioKey + "_expected_temp");
					try {
						deleteDirectoryIfExists(expectedExtractDir);
						Files.createDirectories(expectedExtractDir);
						List<Path> extractedExpected = extractZip(srcFile, expectedExtractDir);
						for (Path ef : extractedExpected) {
							String efName = ef.getFileName().toString().toLowerCase();
							if (efName.endsWith(".png") || efName.endsWith(".jpg") || efName.endsWith(".jpeg")) {
								expectedImages.add(ef);
							}
						}
						Log.info("[OutputWorkflow] Extracted " + expectedImages.size() + " expected image(s) from source ZIP: " + srcFile.getFileName());
					} catch (Exception e) {
						String errMsg = "Failed to extract expected source ZIP: " + srcFile.getFileName() + " - " + e.getMessage();
						Log.error("[OutputWorkflow] " + errMsg);
						ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
								scenarioKey, srcFile, actualImages.get(0), null, 0.0, 100.0, "N/A", "FAIL", errMsg
						));
					}
				} else if (srcName.endsWith(".png") || srcName.endsWith(".jpg") || srcName.endsWith(".jpeg")) {
					expectedImages.add(srcFile);
				}
			}

			if (expectedImages.isEmpty()) {
				String errMsg = "No supported image files found after processing expected source for scenario: " + scenarioKey;
				Log.error("[OutputWorkflow] " + errMsg);
				ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
						scenarioKey, null, actualImages.get(0), null, 0.0, 100.0, "N/A", "FAIL", errMsg
				));
				continue;
			}

			actualImages.sort(Comparator.comparing(path -> path.getFileName().toString()));
			expectedImages.sort(Comparator.comparing(path -> path.getFileName().toString()));

			for (int i = 0; i < Math.max(actualImages.size(), expectedImages.size()); i++) {
				String pairScenarioName = actualImages.size() > 1
						? scenarioKey + " (Image " + (i + 1) + ")"
						: scenarioKey;

				if (i >= expectedImages.size()) {
					String errMsg = "Extra actual image extracted from ZIP: " + actualImages.get(i).getFileName();
					Log.error("[OutputWorkflow] " + errMsg);
					ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
							pairScenarioName, null, actualImages.get(i), null, 0.0, 100.0, "N/A", "FAIL", errMsg
					));
					continue;
				}

				if (i >= actualImages.size()) {
					String errMsg = "Missing actual image for expected: " + expectedImages.get(i).getFileName();
					Log.error("[OutputWorkflow] " + errMsg);
					ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
							pairScenarioName, expectedImages.get(i), null, null, 0.0, 100.0, "N/A", "FAIL", errMsg
					));
					continue;
				}

				Path expPath = expectedImages.get(i);
				Path actPath = actualImages.get(i);

				Log.info("[OutputWorkflow] Comparing actual image: " + actPath.getFileName() + " with expected: " + expPath.getFileName());

				ImageComparisonEngine.ComparisonResult compResult = ImageComparisonEngine.compareImages(
						expPath.toFile(), actPath.toFile(), ssimThreshold
				);

				Path diffPath = null;
				if (!compResult.isMatch()) {
					String diffFilename = scenarioKey + "_diff_" + (i + 1) + "." + format.toLowerCase();
					diffPath = comparisonOutputDir.resolve(diffFilename);
					if (compResult.getDiffImage() != null) {
						try {
							javax.imageio.ImageIO.write(compResult.getDiffImage(), format, diffPath.toFile());
							Log.info("[OutputWorkflow] Visual difference highlighted image saved to: " + diffPath);
						} catch (IOException e) {
							Log.error("[OutputWorkflow] Failed to write difference image: " + e.getMessage());
						}
					}
				}

				ImageComparisonReportStore.ImageScenarioResult reportResult = new ImageComparisonReportStore.ImageScenarioResult(
						pairScenarioName,
						expPath,
						actPath,
						diffPath,
						compResult.getSsimScore(),
						compResult.getPixelDifferencePercentage(),
						compResult.getDimensionsInfo(),
						compResult.isMatch() ? "PASS" : "FAIL",
						compResult.getFailureReason()
				);

				ImageComparisonReportStore.record(reportResult);
			}
		}

		if (ImageComparisonReportStore.hasFailures()) {
			throw new AssertionError("Visual image comparison failed for workflow " + workflowKey + ". See report for details.");
		}
		Log.info("[OutputWorkflow] Visual image comparison passed for workflow: " + workflowKey);
	}

	private List<Path> extractZip(Path zipFile, Path destDir) throws IOException {
		List<Path> extractedFiles = new ArrayList<>();
		try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(Files.newInputStream(zipFile))) {
			java.util.zip.ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					Files.createDirectories(destDir.resolve(entry.getName()));
				} else {
					Path filePath = destDir.resolve(entry.getName());
					Files.createDirectories(filePath.getParent());
					try (java.io.OutputStream os = Files.newOutputStream(filePath)) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zis.read(buffer)) > 0) {
							os.write(buffer, 0, len);
						}
					}
					extractedFiles.add(filePath);
				}
				zis.closeEntry();
			}
		}
		return extractedFiles;
	}

	private Path findExpectedSourceFile(VerificationOptions options) throws IOException {
		if (!Files.isDirectory(options.getExpectedDirectory())) {
			throw new IOException("Expected source folder not found at " + options.getExpectedDirectory());
		}

		List<String> candidateBaseNames = Arrays.asList(
				options.getIntentName() + "_Scenario_" + options.getScenarioNumber(),
				options.getIntentName() + "_scenario_" + options.getScenarioNumber(),
				options.getIntentName() + "_" + options.getScenarioNumber());

		for (String candidateBaseName : candidateBaseNames) {
			try (Stream<Path> fileStream = Files.list(options.getExpectedDirectory())) {
				Path expectedFile = fileStream.filter(Files::isRegularFile)
						.filter(OutputFileWorkflowManager::isExcelFile)
						.filter(path -> candidateBaseName
								.equalsIgnoreCase(stripExtension(path.getFileName().toString())))
						.findFirst().orElse(null);

				if (expectedFile != null) {
					return expectedFile;
				}
			}
		}

		throw new IOException("Expected source Excel file not found. Add "
				+ options.getExpectedDirectory().resolve(options.getIntentName() + "_Scenario_"
						+ options.getScenarioNumber() + ".xlsx")
				+ " for this scenario.");
	}

	private Path findActualOutputFile(ScenarioVerification scenario) {
		String requestId = scenario.getRequestId();
		return scenario.getDownloadedFiles().stream().filter(Files::exists)
				.filter(path -> requestId == null || requestId.isBlank()
						|| path.getFileName().toString().contains(requestId))
				.max(Comparator.comparingLong(path -> path.toFile().lastModified()))
				.orElseThrow(() -> new IllegalStateException("Generated output file not found for Request ID "
						+ valueOrUnknown(requestId) + " in " + generatedOutputDir));
	}

	private void validateGeneratedFileName(Path actualFile, ScenarioVerification scenario, List<String> discrepancies) {
		String fileName = actualFile.getFileName().toString();
		String baseName = stripExtension(fileName);
		String expectedPrefix = valueOrUnknown(scenario.getRequestId()) + "_BibData_" + intentName + "_";

		if (scenario.getRequestId() != null && !scenario.getRequestId().isBlank()
				&& !baseName.startsWith(expectedPrefix)) {
			discrepancies.add("Output filename mismatch. Expected format: <Request_ID>_BibData_<Intent_Name>_<Execution_Date>.xlsx"
					+ " | Expected prefix: " + expectedPrefix + " | Actual: " + fileName);
		}
	}

	private List<Path> listCurrentIncomingFiles() throws IOException {
		List<Path> files = new ArrayList<>();
		if (!Files.isDirectory(incomingDir)) {
			return files;
		}

		try (Stream<Path> fileStream = Files.list(incomingDir)) {
			fileStream.filter(Files::isRegularFile).forEach(files::add);
		}
		return files;
	}

	private Path waitForNewIncomingFile(List<Path> knownFiles, String filenameContains, String requestId,
			int timeoutSeconds) throws IOException, InterruptedException {
		Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
		while (Instant.now().isBefore(deadline)) {
			try (Stream<Path> fileStream = Files.list(incomingDir)) {
				List<Path> candidates = fileStream.filter(Files::isRegularFile)
						.filter(path -> knownFiles.stream().noneMatch(existing -> existing.equals(path)))
						.filter(path -> !isPartialDownload(path))
						.sorted(Comparator.comparingLong(path -> path.toFile().lastModified())).collect(java.util.stream.Collectors.toList());

				Path preferred = candidates.stream().filter(path -> matchesHint(path, filenameContains, requestId))
						.reduce((first, second) -> second).orElse(null);
				if (preferred != null) {
					return preferred;
				}
				if (!candidates.isEmpty()) {
					return candidates.get(candidates.size() - 1);
				}
			}
			Thread.sleep(1000);
		}
		return null;
	}

	private static boolean matchesHint(Path path, String filenameContains, String requestId) {
		String fileName = path.getFileName().toString();
		boolean matchesConfiguredHint = filenameContains == null || filenameContains.isBlank()
				|| fileName.contains(filenameContains);
		boolean matchesRequestId = requestId == null || requestId.isBlank() || fileName.contains(requestId);
		return matchesConfiguredHint && matchesRequestId;
	}

	private static String resolveIntentName(Properties config, String workflowKey) {
		String configuredIntentName = config.getProperty(workflowKey + ".intentName");
		if (configuredIntentName != null && !configuredIntentName.isBlank()) {
			return configuredIntentName.trim();
		}

		switch (workflowKey) {
		case "uspatent":
			return "US_Patent";
		case "ustrademark":
			return "US_Trademark";
		case "aupatent":
			return "AU_Patent";
		case "autrademark":
			return "AU_Trademark";
		case "eppatent":
			return "EP_Patent";
		case "globalpatent":
			return "Global_Patent";
		default:
			return workflowKey;
		}
	}

	private static boolean isPartialDownload(Path path) {
		String fileName = path.getFileName().toString().toLowerCase();
		return fileName.endsWith(".crdownload") 
				|| fileName.endsWith(".tmp") 
				|| fileName.endsWith(".part")
				|| fileName.startsWith(".") 
				|| fileName.startsWith("~$")
				|| fileName.contains("chrome") 
				|| fileName.contains("google")
				|| fileName.contains("edge")
				|| fileName.contains("chromium");
	}

	private static Path resolveUniqueTarget(Path directory, String fileName) {
		Path target = directory.resolve(fileName);
		if (!Files.exists(target)) {
			return target;
		}

		String baseName = stripExtension(fileName);
		String extension = getExtension(fileName);
		int counter = 1;
		Path candidate = target;
		while (Files.exists(candidate)) {
			candidate = directory.resolve(baseName + "-" + counter + extension);
			counter++;
		}
		return candidate;
	}

	private static boolean isExcelFile(Path path) {
		String fileName = path.getFileName().toString().toLowerCase();
		return fileName.endsWith(".xlsx") || fileName.endsWith(".xls") || fileName.endsWith(".xlsm");
	}

	private static String stripExtension(String fileName) {
		int extensionIndex = fileName.lastIndexOf('.');
		return extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
	}

	private static String getExtension(String fileName) {
		int extensionIndex = fileName.lastIndexOf('.');
		return extensionIndex >= 0 ? fileName.substring(extensionIndex) : "";
	}

	private static String sanitizePathSegment(String value) {
		return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
	}

	private static String resolveScenarioNumber(String scenarioKey) {
		Matcher matcher = SCENARIO_NUMBER_PATTERN.matcher(scenarioKey);
		return matcher.find() ? matcher.group(1) : sanitizePathSegment(scenarioKey);
	}

	private static String valueOrUnknown(String value) {
		return value == null || value.isBlank() ? "UNKNOWN" : value;
	}

	private static void deleteDirectoryIfExists(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return;
		}

		try (Stream<Path> walk = Files.walk(directory)) {
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					throw new RuntimeException("Failed to delete path: " + path, e);
				}
			});
		} catch (RuntimeException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw e;
		}
	}

	public static class DownloadSnapshot {
		private final List<Path> knownFiles;

		public DownloadSnapshot(List<Path> knownFiles) {
			this.knownFiles = knownFiles;
		}

		public List<Path> getKnownFiles() {
			return knownFiles;
		}
	}

	public static class VerificationOptions {
		private final Path expectedDirectory;
		private final String intentName;
		private final String scenarioNumber;
		private final int sheetIndex;
		private final int headerRowIndex;
		private final boolean strictHeaderOrder;
		private final boolean caseInsensitiveHeaders;
		private final boolean caseInsensitiveData;
		private final boolean ignoreRowOrder;
		private final String primaryKeyColumn;

		public VerificationOptions(Path expectedDirectory, String intentName, String scenarioNumber, int sheetIndex,
				int headerRowIndex, boolean strictHeaderOrder, boolean caseInsensitiveHeaders,
				boolean caseInsensitiveData, boolean ignoreRowOrder, String primaryKeyColumn) {
			this.expectedDirectory = expectedDirectory;
			this.intentName = intentName;
			this.scenarioNumber = scenarioNumber;
			this.sheetIndex = sheetIndex;
			this.headerRowIndex = headerRowIndex;
			this.strictHeaderOrder = strictHeaderOrder;
			this.caseInsensitiveHeaders = caseInsensitiveHeaders;
			this.caseInsensitiveData = caseInsensitiveData;
			this.ignoreRowOrder = ignoreRowOrder;
			this.primaryKeyColumn = primaryKeyColumn;
		}

		public static VerificationOptions fromConfig(Properties config, String workflowKey, Path workflowSourceDir,
				String scenarioKey, String intentName) {
			int sheetIndex = getInt(config, workflowKey + ".sheetIndex", getInt(config, "workflow.sheetIndex", 0));
			int headerRowIndex = getInt(config, workflowKey + ".headerRowIndex",
					getInt(config, "workflow.headerRowIndex", 0));
			boolean strictHeaderOrder = getBoolean(config, workflowKey + ".strictHeaderOrder",
					getBoolean(config, "workflow.strictHeaderOrder", false));
			boolean caseInsensitiveHeaders = getBoolean(config, workflowKey + ".caseInsensitiveHeaders",
					getBoolean(config, "workflow.caseInsensitiveHeaders", true));
			boolean caseInsensitiveData = getBoolean(config, workflowKey + ".caseInsensitiveData",
					getBoolean(config, "workflow.caseInsensitiveData", false));
			boolean ignoreRowOrder = getBoolean(config, workflowKey + ".ignoreRowOrder",
					getBoolean(config, "workflow.ignoreRowOrder", false));
			String primaryKeyColumn = getString(config, workflowKey + ".primaryKeyColumn",
					getString(config, "workflow.primaryKeyColumn", "1"));

			return new VerificationOptions(workflowSourceDir, intentName, resolveScenarioNumber(scenarioKey), sheetIndex,
					headerRowIndex, strictHeaderOrder, caseInsensitiveHeaders, caseInsensitiveData, ignoreRowOrder,
					primaryKeyColumn);
		}

		public Path getExpectedDirectory() {
			return expectedDirectory;
		}

		public String getIntentName() {
			return intentName;
		}

		public String getScenarioNumber() {
			return scenarioNumber;
		}

		public int getSheetIndex() {
			return sheetIndex;
		}

		public int getHeaderRowIndex() {
			return headerRowIndex;
		}

		public boolean isStrictHeaderOrder() {
			return strictHeaderOrder;
		}

		public boolean isCaseInsensitiveHeaders() {
			return caseInsensitiveHeaders;
		}

		public boolean isCaseInsensitiveData() {
			return caseInsensitiveData;
		}

		public boolean isIgnoreRowOrder() {
			return ignoreRowOrder;
		}

		public String getPrimaryKeyColumn() {
			return primaryKeyColumn;
		}

		private static int getInt(Properties config, String key, int defaultValue) {
			String value = config.getProperty(key);
			if (value == null || value.isBlank()) {
				return defaultValue;
			}
			return Integer.parseInt(value.trim());
		}

		private static boolean getBoolean(Properties config, String key, boolean defaultValue) {
			String value = config.getProperty(key);
			if (value == null || value.isBlank()) {
				return defaultValue;
			}
			return Boolean.parseBoolean(value.trim());
		}

		private static String getString(Properties config, String key, String defaultValue) {
			String value = config.getProperty(key);
			if (value == null || value.isBlank()) {
				return defaultValue;
			}
			return value.trim();
		}
	}

	private static class ScenarioVerification {
		private final String scenarioKey;
		private final VerificationOptions options;
		private final List<Path> downloadedFiles = new ArrayList<>();
		private String requestId;

		private ScenarioVerification(String scenarioKey, VerificationOptions options) {
			this.scenarioKey = scenarioKey;
			this.options = options;
		}

		public String getScenarioKey() {
			return scenarioKey;
		}

		public VerificationOptions getOptions() {
			return options;
		}

		public List<Path> getDownloadedFiles() {
			return downloadedFiles;
		}

		public String getRequestId() {
			return requestId;
		}

		public void setRequestId(String requestId) {
			if (requestId != null && !requestId.isBlank()) {
				this.requestId = requestId;
			}
		}
	}

	private void cleanupPreviousRunFiles() {
		Log.info("[OutputWorkflow] Output folder cleanup started for intent: " + intentName);
		try {
			List<Path> toDelete = new ArrayList<>();
			
			if (Files.exists(incomingDir)) {
				try (Stream<Path> stream = Files.list(incomingDir)) {
					stream.filter(Files::isRegularFile)
					      .filter(p -> isFileRelatedToIntent(p, intentName, workflowKey))
					      .forEach(toDelete::add);
				}
			}
			
			if (Files.exists(generatedOutputDir)) {
				try (Stream<Path> stream = Files.list(generatedOutputDir)) {
					stream.filter(Files::isRegularFile)
					      .filter(p -> isFileRelatedToIntent(p, intentName, workflowKey))
					      .forEach(toDelete::add);
				}
			}
			
			if (toDelete.isEmpty()) {
				Log.info("[OutputWorkflow] No old output files from previous runs identified for deletion.");
			} else {
				Log.info("[OutputWorkflow] Files identified for deletion: " + toDelete);
				for (Path file : toDelete) {
					Files.deleteIfExists(file);
					Log.info("[OutputWorkflow] File successfully deleted: " + file);
				}
			}
			Log.info("[OutputWorkflow] Output folder cleanup completed. Directory is in a clean state.");
		} catch (Exception e) {
			Log.error("[OutputWorkflow] Pre-Execution Cleanup failed for intent: " + intentName + " | Error: " + e.getMessage());
			try {
				org.openqa.selenium.WebDriver activeDriver = getActiveDriver();
				if (activeDriver != null) {
					String ssPath = ScreenshotUtil.captureScreenshot(activeDriver, "CleanupFailed_" + workflowKey);
					Log.error("[OutputWorkflow] Captured cleanup failure screenshot: " + ssPath);
				}
			} catch (Exception se) {
				Log.warn("[OutputWorkflow] Failed to capture screenshot for cleanup failure: " + se.getMessage());
			}
			throw new RuntimeException("Pre-Execution Cleanup failed for intent: " + intentName + ". Aborting to prevent stale files from being used.", e);
		}
	}

	private boolean isFileRelatedToIntent(Path path, String intentName, String workflowKey) {
		String filename = path.getFileName().toString().toLowerCase();
		String lowerIntent = intentName.toLowerCase();
		String lowerWf = workflowKey.toLowerCase();
		
		String normalizedIntent1 = lowerIntent.replace("_", "");
		String normalizedIntent2 = lowerIntent.replace("_", " ");
		String normalizedIntent3 = lowerIntent.replace("_", "-");
		
		return filename.contains(lowerIntent) ||
		       filename.contains(lowerWf) ||
		       filename.contains(normalizedIntent1) ||
		       filename.contains(normalizedIntent2) ||
		       filename.contains(normalizedIntent3);
	}

	private org.openqa.selenium.WebDriver getActiveDriver() {
		try {
			Class<?> baseTestClass = Class.forName("base.BaseTest");
			java.lang.reflect.Method method = baseTestClass.getMethod("getActiveDriver");
			return (org.openqa.selenium.WebDriver) method.invoke(null);
		} catch (Exception e) {
			return null;
		}
	}
}
