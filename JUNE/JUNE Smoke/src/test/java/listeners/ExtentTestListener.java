package listeners;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import base.BaseTest;

import org.openqa.selenium.WebDriver;
import org.testng.*;
import utils.ExtentManager;
import utils.Log;
import utils.ScreenshotUtil;
import utils.PlatformRetryReportStore;
import utils.EmailUtil;
import utils.ExecutionRunManager;
import utils.logging.TestLogBuffer;

import java.util.List;

public class ExtentTestListener implements ITestListener {

	ExtentReports extent = ExtentManager.getInstance();
	ThreadLocal<ExtentTest> test = new ThreadLocal<>();

	@Override
	public void onTestStart(ITestResult result) {
		// Fresh slate so logs from a previous test method on this (possibly reused)
		// thread don't bleed into this one.
		TestLogBuffer.clear();
		ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName());
		test.set(extentTest);
		Log.setTest(extentTest);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		test.get().pass("Test Passed");
		attachExecutionLogs(test.get());
		Log.clear();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		Object testClass = result.getInstance();
		WebDriver driver = ((BaseTest) testClass).getDriver();

		String screenshotPath = ScreenshotUtil.captureScreenshot(driver, result.getMethod().getMethodName());
		test.get().fail(result.getThrowable());
		test.get().addScreenCaptureFromPath(screenshotPath);
		attachExecutionLogs(test.get());
		Log.clear();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		test.get().skip("Test Skipped");
		attachExecutionLogs(test.get());
		Log.clear();
	}

	/**
	 * Drains this thread's captured log lines (console + file logging is untouched -
	 * see ExtentTestAppender) and attaches them under a collapsible "Execution Logs"
	 * node on the test, rendered as a monospace code block with timestamps intact.
	 */
	private void attachExecutionLogs(ExtentTest extentTest) {
		List<String> lines = TestLogBuffer.drainAndClear();
		if (lines.isEmpty()) {
			return;
		}
		String combined = String.join("", lines);
		extentTest.createNode("Execution Logs").info(MarkupHelper.createCodeBlock(combined));
	}

	@Override
	public void onFinish(ITestContext context) {
		boolean hasRetries = PlatformRetryReportStore.hasAppliedRetries();
		String retryZipPath = null;
		if (hasRetries) {
			String srcDir = ExecutionRunManager.isRunSpecificEnabled()
					? ExecutionRunManager.getRetryArtifactsDir().toAbsolutePath().toString()
					: "test-output/RetryArtifacts";
			String destZip = ExecutionRunManager.isRunSpecificEnabled()
					? ExecutionRunManager.getRunRoot().resolve("RetryArtifacts.zip").toAbsolutePath().toString()
					: "test-output/RetryArtifacts.zip";
			retryZipPath = utils.ZipUtil.zipDirectory(srcDir, destZip);
		}

		addPlatformRetrySection();
		addDataValidationSection();
		utils.ImageComparisonReportStore.addImageValidationSection(extent);

		extent.flush();

		// Preserve logs in the run-specific Logs directory
		if (ExecutionRunManager.isRunSpecificEnabled()) {
			try {
				java.nio.file.Path sourceLog = java.nio.file.Paths.get("logs", "application.log");
				if (java.nio.file.Files.exists(sourceLog)) {
					java.nio.file.Path destLog = ExecutionRunManager.getLogsDir().resolve("application.log");
					java.nio.file.Files.copy(sourceLog, destLog, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
					System.out.println("✅ Copied application.log to run-specific Logs directory: " + destLog);
				}
			} catch (Exception e) {
				System.err.println("Failed to copy application.log to run-specific Logs directory: " + e.getMessage());
			}
		}

		boolean isBuildFailed = context.getFailedTests().size() > 0;

		EmailUtil.sendReportEmail(isBuildFailed, retryZipPath);
		// Commented out to prevent breaking screenshot references in the Extent Report HTML.
		// Screenshots are cleaned up at the start of each suite execution in BaseTest.beforeSuite().
		// ScreenshotUtil.deleteScreenshots(System.getProperty("user.dir") + "/test-output/screenshots");
	}

	private void addPlatformRetrySection() {
		ExtentTest section = extent.createTest("Platform Retry Results");
		java.util.List<PlatformRetryReportStore.RetryEvent> events = PlatformRetryReportStore.getEvents();

		if (events.isEmpty()) {
			section.info("No transient platform error retries were triggered.");
			return;
		}

		boolean hasApplied = false;
		for (PlatformRetryReportStore.RetryEvent event : events) {
			if (!event.isRetryApplied()) {
				continue;
			}
			hasApplied = true;
			ExtentTest node = section.createNode(event.getIntentName() + " - " + event.getScenarioName());

			StringBuilder html = new StringBuilder();
			html.append("<div style='font-family:\"Segoe UI\",Arial,sans-serif; margin:10px 0;'>");
			
			String statusBadge;
			if (event.getRetryOutcome().equalsIgnoreCase("Success") || event.getRetryOutcome().contains("Recovered")) {
				statusBadge = "<span style='background-color:#fff3cd; color:#856404; padding:6px 12px; border-radius:20px; font-weight:bold; font-size:12px; border:1px solid #ffeeba;'>⚠ RECOVERED</span>";
			} else {
				statusBadge = "<span style='background-color:#f8d7da; color:#721c24; padding:6px 12px; border-radius:20px; font-weight:bold; font-size:12px; border:1px solid #f5c6cb;'>FAIL</span>";
			}

			html.append("<table style='width:100%; border-collapse:collapse; margin-bottom:15px; font-size:13px; box-shadow:0 1px 3px rgba(0,0,0,0.1);'>");
			html.append("<tr style='background-color:#f8f9fa; border-bottom:1px solid #dee2e6;'>");
			html.append("<th style='padding:8px 12px; text-align:left; width:200px; color:#495057;'>Property</th>");
			html.append("<th style='padding:8px 12px; text-align:left; color:#495057;'>Details</th>");
			html.append("</tr>");
			
			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Retry Applied</td>");
			html.append("<td style='padding:8px 12px; color:#212529;'>Yes</td>");
			html.append("</tr>");

			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Scenario Name</td>");
			html.append("<td style='padding:8px 12px; color:#212529;'>").append(event.getScenarioName()).append("</td>");
			html.append("</tr>");

			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Retry Reason</td>");
			html.append("<td style='padding:8px 12px; color:#212529;'>").append(event.getRetryTriggerReason()).append("</td>");
			html.append("</tr>");

			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Attempts</td>");
			html.append("<td style='padding:8px 12px; color:#212529;'>").append(event.getRetryCount()).append("</td>");
			html.append("</tr>");

			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Outcome</td>");
			html.append("<td style='padding:8px 12px;'>").append(statusBadge).append("</td>");
			html.append("</tr>");

			String artifactsPath = "test-output/RetryArtifacts/" + event.getIntentName() + "/" + event.getScenarioName();
			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Artifacts Path</td>");
			html.append("<td style='padding:8px 12px; font-family:monospace; color:#0056b3;'>").append(artifactsPath).append("</td>");
			html.append("</tr>");

			html.append("</table>");
			html.append("</div>");

			if (event.getRetryOutcome().equalsIgnoreCase("Success") || event.getRetryOutcome().contains("Recovered")) {
				node.warning(html.toString());
			} else {
				node.fail(html.toString());
			}
		}

		if (!hasApplied) {
			section.info("No transient platform error retries were applied during this run.");
		} else {
			section.info("Retry Recovery Screenshots & Logs ZIP is available at: <b>test-output/RetryArtifacts.zip</b>");
		}
	}

	private void addDataValidationSection() {
		ExtentTest section = extent.createTest("Centralized Data Validation & Comparison Results");
		java.util.List<utils.DataComparisonReportStore.ScenarioComparisonReport> reports = utils.DataComparisonReportStore.getResults();

		if (reports.isEmpty()) {
			section.info("No data validation or comparison occurred in this execution run.");
			return;
		}

		// Group by intent/workflow
		java.util.Map<String, java.util.List<utils.DataComparisonReportStore.ScenarioComparisonReport>> byWorkflow = new java.util.LinkedHashMap<>();
		for (utils.DataComparisonReportStore.ScenarioComparisonReport r : reports) {
			byWorkflow.computeIfAbsent(r.getWorkflowKey(), k -> new java.util.ArrayList<>()).add(r);
		}

		for (java.util.Map.Entry<String, java.util.List<utils.DataComparisonReportStore.ScenarioComparisonReport>> entry : byWorkflow.entrySet()) {
			String wf = entry.getKey();
			java.util.List<utils.DataComparisonReportStore.ScenarioComparisonReport> wfReports = entry.getValue();
			String intent = wfReports.get(0).getIntentName();
			boolean allMatch = wfReports.stream().allMatch(utils.DataComparisonReportStore.ScenarioComparisonReport::isMatch);
			int totalMatched = wfReports.stream().mapToInt(utils.DataComparisonReportStore.ScenarioComparisonReport::getTotalMatched).sum();
			int totalMismatches = wfReports.stream().mapToInt(utils.DataComparisonReportStore.ScenarioComparisonReport::getTotalMismatches).sum();
			java.nio.file.Path reportFile = wfReports.stream()
					.map(utils.DataComparisonReportStore.ScenarioComparisonReport::getReportPath)
					.filter(java.util.Objects::nonNull)
					.findFirst()
					.orElse(null);

			ExtentTest node = section.createNode("Intent/Workflow: " + intent + " (" + wf + ")");
			
			StringBuilder html = new StringBuilder();
			html.append("<div style='font-family:\"Segoe UI\",Arial,sans-serif; margin:10px 0;'>");
			
			String statusBadge;
			if (allMatch) {
				statusBadge = "<span style='background-color:#d4edda; color:#155724; padding:6px 12px; border-radius:20px; font-weight:bold; font-size:12px; border:1px solid #c3e6cb;'>✅ PASSED</span>";
			} else {
				statusBadge = "<span style='background-color:#f8d7da; color:#721c24; padding:6px 12px; border-radius:20px; font-weight:bold; font-size:12px; border:1px solid #f5c6cb;'>❌ FAILED</span>";
			}

			html.append("<table style='width:100%; border-collapse:collapse; margin-bottom:15px; font-size:13px; box-shadow:0 1px 3px rgba(0,0,0,0.1);'>");
			html.append("<tr style='background-color:#f8f9fa; border-bottom:1px solid #dee2e6;'>");
			html.append("<th style='padding:8px 12px; text-align:left; width:250px; color:#495057;'>Property</th>");
			html.append("<th style='padding:8px 12px; text-align:left; color:#495057;'>Details</th>");
			html.append("</tr>");
			
			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Overall Validation Status</td>");
			html.append("<td style='padding:8px 12px;'>").append(statusBadge).append("</td>");
			html.append("</tr>");

			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Total Matched Cells/Records</td>");
			html.append("<td style='padding:8px 12px; color:#212529;'>").append(totalMatched).append("</td>");
			html.append("</tr>");

			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Total Mismatches</td>");
			html.append("<td style='padding:8px 12px; color:#c10000; font-weight:bold;'>").append(totalMismatches).append("</td>");
			html.append("</tr>");

			if (reportFile != null) {
				String relReportPath;
				try {
					java.nio.file.Path projectRoot = java.nio.file.Paths.get("").toAbsolutePath().normalize();
					java.nio.file.Path relFromRoot = projectRoot.relativize(reportFile.toAbsolutePath().normalize());
					relReportPath = relFromRoot.toString().replace('\\', '/');
				} catch (Exception e) {
					relReportPath = "output/comparison/" + reportFile.getFileName().toString();
				}
				html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
				html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Excel Comparison Report File</td>");
				html.append("<td style='padding:8px 12px;'><a href='../").append(relReportPath)
					.append("' style='font-family:monospace; color:#0056b3; text-decoration:underline; font-weight:bold;' target='_blank'>")
					.append(reportFile.getFileName().toString()).append("</a></td>");
				html.append("</tr>");
				
				html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
				html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Absolute Location</td>");
				html.append("<td style='padding:8px 12px; font-family:monospace; font-size:11px; color:#495057;'>").append(reportFile.toAbsolutePath().toString()).append("</td>");
				html.append("</tr>");
			}

			html.append("</table>");
			
			// Show short list of scenario outcomes inside this workflow
			html.append("<h5 style='margin-top:15px; color:#333;'>Scenario Summary:</h5>");
			html.append("<table style='width:100%; border-collapse:collapse; font-size:12px;'>");
			html.append("<tr style='background-color:#e9ecef; border-bottom:1px solid #dee2e6; font-weight:bold;'>");
			html.append("<td style='padding:6px; width:20%;'>Scenario</td>");
			html.append("<td style='padding:6px; width:15%;'>Request ID</td>");
			html.append("<td style='padding:6px; width:15%;'>Status</td>");
			html.append("<td style='padding:6px; width:15%;'>Match Pct</td>");
			html.append("<td style='padding:6px; width:35%;'>Details/Discrepancies</td>");
			html.append("</tr>");
			
			for (utils.DataComparisonReportStore.ScenarioComparisonReport r : wfReports) {
				String scStatus = r.isMatch() ? "<span style='color:green;font-weight:bold;'>MATCH</span>" : "<span style='color:red;font-weight:bold;'>MISMATCH</span>";
				String limitDesc = String.join("<br/>", r.getLimitedDiscrepancies(5));
				html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
				html.append("<td style='padding:6px;'>").append(r.getScenarioKey()).append("</td>");
				html.append("<td style='padding:6px; font-family:monospace;'>").append(r.getRequestId() != null ? r.getRequestId() : "-").append("</td>");
				html.append("<td style='padding:6px;'>").append(scStatus).append("</td>");
				html.append("<td style='padding:6px;'>").append(String.format("%.1f%%", r.getMatchPercentage())).append("</td>");
				html.append("<td style='padding:6px; color:#666;'>").append(limitDesc.isEmpty() ? "No discrepancies" : limitDesc).append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			html.append("</div>");

			if (allMatch) {
				node.pass(html.toString());
			} else {
				node.fail(html.toString());
			}
		}
	}
}
