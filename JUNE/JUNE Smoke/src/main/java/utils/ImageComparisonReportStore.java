package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ImageComparisonReportStore {

	private static final List<ImageScenarioResult> RESULTS = new ArrayList<>();

	private ImageComparisonReportStore() {
	}

	public static synchronized void clear() {
		RESULTS.clear();
	}

	public static synchronized void record(ImageScenarioResult result) {
		RESULTS.add(result);
	}

	public static synchronized List<ImageScenarioResult> getResults() {
		return new ArrayList<>(RESULTS);
	}

	public static synchronized boolean hasFailures() {
		return RESULTS.stream().anyMatch(r -> "FAIL".equalsIgnoreCase(r.getStatus()));
	}

	public static synchronized List<File> getFailedScenarioAttachments() {
		List<File> files = new ArrayList<>();
		for (ImageScenarioResult r : RESULTS) {
			if ("FAIL".equalsIgnoreCase(r.getStatus())) {
				if (r.getExpectedPath() != null) {
					File f = r.getExpectedPath().toFile();
					if (f.exists() && !files.contains(f)) {
						files.add(f);
					}
				}
				if (r.getActualPath() != null) {
					File f = r.getActualPath().toFile();
					if (f.exists() && !files.contains(f)) {
						files.add(f);
					}
				}
				if (r.getDiffPath() != null) {
					File f = r.getDiffPath().toFile();
					if (f.exists() && !files.contains(f)) {
						files.add(f);
					}
				}
			}
		}
		return files;
	}

	public static synchronized String buildEmailSummary() {
		if (RESULTS.isEmpty()) {
			return "";
		}

		StringBuilder summary = new StringBuilder();
		summary.append("========================================\n");
		summary.append("🖼️ VISUAL IMAGE COMPARISON SUMMARY\n");
		summary.append("========================================\n\n");
		summary.append("Intent/Workflow: US TM Image Download (ustmimagedownload)\n");

		boolean allMatch = !hasFailures();
		summary.append("  • Status: ").append(allMatch ? "✅ PASSED" : "❌ FAILED").append("\n");
		summary.append("  • Total Scenarios Compared: ").append(RESULTS.size()).append("\n");

		long passed = RESULTS.stream().filter(r -> "PASS".equalsIgnoreCase(r.getStatus())).count();
		long failed = RESULTS.size() - passed;
		summary.append("  • Successful Matches: ").append(passed).append("\n");
		summary.append("  • Mismatch Failures: ").append(failed).append("\n\n");

		summary.append("Detailed Image Comparisons:\n");
		for (ImageScenarioResult r : RESULTS) {
			summary.append("  - ").append(r.getScenarioName()).append(": ")
					.append("PASS".equalsIgnoreCase(r.getStatus()) ? "✅ PASS" : "❌ FAIL")
					.append(" | SSIM = ").append(String.format("%.2f%%", r.getSsimScore() * 100))
					.append(" | Pixel Diff = ").append(String.format("%.2f%%", r.getPixelDiffPercentage()))
					.append("\n");
			if (!r.getFailureReason().isEmpty()) {
				summary.append("    Reason: ").append(r.getFailureReason()).append("\n");
			}
		}

		if (failed > 0) {
			summary.append("\nNote: Expected, Actual, and Difference images for failed scenarios are attached to this email.\n");
		}
		summary.append("\n");
		return summary.toString();
	}

	public static synchronized void addImageValidationSection(ExtentReports extent) {
		if (RESULTS.isEmpty()) {
			return;
		}

		ExtentTest section = extent.createTest("Centralized Visual Image Comparison Results");
		boolean allPass = !hasFailures();
		String overallStatus = allPass
				? "<span style='background-color:#d4edda; color:#155724; padding:6px 12px; border-radius:20px; font-weight:bold; font-size:12px; border:1px solid #c3e6cb;'>✅ PASSED</span>"
				: "<span style='background-color:#f8d7da; color:#721c24; padding:6px 12px; border-radius:20px; font-weight:bold; font-size:12px; border:1px solid #f5c6cb;'>❌ FAILED</span>";

		ExtentTest node = section.createNode("US TM Image Download Validation Summary");

		StringBuilder html = new StringBuilder();
		html.append("<div style='font-family:\"Segoe UI\",Arial,sans-serif; margin:10px 0;'>");
		html.append("<table style='width:100%; border-collapse:collapse; margin-bottom:15px; font-size:13px; box-shadow:0 1px 3px rgba(0,0,0,0.1);'>");
		html.append("<tr style='background-color:#f8f9fa; border-bottom:1px solid #dee2e6;'>");
		html.append("<th style='padding:8px 12px; text-align:left; width:250px; color:#495057;'>Property</th>");
		html.append("<th style='padding:8px 12px; text-align:left; color:#495057;'>Details</th>");
		html.append("</tr>");

		html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
		html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Overall Image Validation Status</td>");
		html.append("<td style='padding:8px 12px;'>").append(overallStatus).append("</td>");
		html.append("</tr>");

		html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
		html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Total Scenarios Compared</td>");
		html.append("<td style='padding:8px 12px; color:#212529;'>").append(RESULTS.size()).append("</td>");
		html.append("</tr>");

		long failedCount = RESULTS.stream().filter(r -> "FAIL".equalsIgnoreCase(r.getStatus())).count();
		html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
		html.append("<td style='padding:8px 12px; font-weight:bold; color:#6c757d;'>Total Mismatched Scenarios</td>");
		html.append("<td style='padding:8px 12px; color:#c10000; font-weight:bold;'>").append(failedCount).append("</td>");
		html.append("</tr>");

		html.append("</table>");

		html.append("<h5 style='margin-top:15px; color:#333;'>Scenario Summary:</h5>");
		html.append("<table style='width:100%; border-collapse:collapse; font-size:12px;'>");
		html.append("<tr style='background-color:#e9ecef; border-bottom:1px solid #dee2e6; font-weight:bold;'>");
		html.append("<td style='padding:6px; width:25%;'>Scenario</td>");
		html.append("<td style='padding:6px; width:15%;'>Status</td>");
		html.append("<td style='padding:6px; width:15%;'>SSIM Score</td>");
		html.append("<td style='padding:6px; width:15%;'>Pixel Diff %</td>");
		html.append("<td style='padding:6px; width:30%;'>Details / Failure Reason</td>");
		html.append("</tr>");

		for (ImageScenarioResult r : RESULTS) {
			String statusBadge = "PASS".equalsIgnoreCase(r.getStatus())
					? "<span style='color:green;font-weight:bold;'>PASS</span>"
					: "<span style='color:red;font-weight:bold;'>FAIL</span>";
			html.append("<tr style='border-bottom:1px solid #dee2e6;'>");
			html.append("<td style='padding:6px;'>").append(r.getScenarioName()).append("</td>");
			html.append("<td style='padding:6px;'>").append(statusBadge).append("</td>");
			html.append("<td style='padding:6px;'>").append(String.format("%.2f%%", r.getSsimScore() * 100)).append("</td>");
			html.append("<td style='padding:6px;'>").append(String.format("%.2f%%", r.getPixelDiffPercentage())).append("</td>");
			html.append("<td style='padding:6px; color:#666;'>").append(r.getFailureReason().isEmpty() ? "Visual match" : r.getFailureReason()).append("</td>");
			html.append("</tr>");
		}
		html.append("</table>");
		html.append("</div>");

		if (allPass) {
			node.pass(html.toString());
		} else {
			node.fail(html.toString());
		}

		// Add detailed nodes for each scenario to display images
		for (ImageScenarioResult r : RESULTS) {
			ExtentTest scNode = section.createNode(r.getScenarioName() + " Visual Details");
			StringBuilder scHtml = new StringBuilder();
			scHtml.append("<div style='font-family:\"Segoe UI\",Arial,sans-serif; margin:10px 0;'>");

			String statusBadge = "PASS".equalsIgnoreCase(r.getStatus())
					? "<span style='background-color:#d4edda; color:#155724; padding:2px 8px; border-radius:10px; font-weight:bold; font-size:11px;'>PASS</span>"
					: "<span style='background-color:#f8d7da; color:#721c24; padding:2px 8px; border-radius:10px; font-weight:bold; font-size:11px;'>FAIL</span>";

			scHtml.append("<table style='width:100%; border-collapse:collapse; margin-bottom:10px; font-size:12px;'>");
			scHtml.append("<tr><td style='padding:4px; font-weight:bold; width:150px; color:#6c757d;'>Status</td><td style='padding:4px;'>").append(statusBadge).append("</td></tr>");
			scHtml.append("<tr><td style='padding:4px; font-weight:bold; color:#6c757d;'>Expected Image</td><td style='padding:4px; font-family:monospace;'>").append(r.getExpectedPath()).append("</td></tr>");
			scHtml.append("<tr><td style='padding:4px; font-weight:bold; color:#6c757d;'>Actual Image</td><td style='padding:4px; font-family:monospace;'>").append(r.getActualPath()).append("</td></tr>");
			scHtml.append("<tr><td style='padding:4px; font-weight:bold; color:#6c757d;'>Dimensions Info</td><td style='padding:4px;'>").append(r.getDimensionsInfo()).append("</td></tr>");
			scHtml.append("<tr><td style='padding:4px; font-weight:bold; color:#6c757d;'>SSIM Score</td><td style='padding:4px;'>").append(String.format("%.4f", r.getSsimScore())).append("</td></tr>");
			scHtml.append("<tr><td style='padding:4px; font-weight:bold; color:#6c757d;'>Pixel Difference %</td><td style='padding:4px;'>").append(String.format("%.4f%%", r.getPixelDiffPercentage())).append("</td></tr>");
			if (!r.getFailureReason().isEmpty()) {
				scHtml.append("<tr><td style='padding:4px; font-weight:bold; color:#c10000;'>Failure Reason</td><td style='padding:4px; color:#c10000; font-weight:bold;'>").append(r.getFailureReason()).append("</td></tr>");
			}
			scHtml.append("</table>");

			// Add HTML visual previews if the paths are relative-friendly
			String relExpected = getRelativePathForHtml(r.getExpectedPath());
			String relActual = getRelativePathForHtml(r.getActualPath());
			String relDiff = r.getDiffPath() != null ? getRelativePathForHtml(r.getDiffPath()) : "";

			scHtml.append("<div style='margin-top:15px; display:flex; gap:20px; flex-wrap:wrap;'>");
			if (!relExpected.isEmpty()) {
				scHtml.append("<div style='border:1px solid #dee2e6; padding:10px; border-radius:4px; text-align:center;'>")
						.append("<b>Expected Image</b><br/>")
						.append("<img src='../").append(relExpected).append("' style='max-width:300px; max-height:300px; margin-top:5px; border:1px dashed #ccc;'/>")
						.append("</div>");
			}
			if (!relActual.isEmpty()) {
				scHtml.append("<div style='border:1px solid #dee2e6; padding:10px; border-radius:4px; text-align:center;'>")
						.append("<b>Actual Image</b><br/>")
						.append("<img src='../").append(relActual).append("' style='max-width:300px; max-height:300px; margin-top:5px; border:1px dashed #ccc;'/>")
						.append("</div>");
			}
			if (!relDiff.isEmpty()) {
				scHtml.append("<div style='border:1px solid #dee2e6; padding:10px; border-radius:4px; text-align:center; background-color:#fff5f5;'>")
						.append("<b style='color:#c10000;'>Difference Image</b><br/>")
						.append("<img src='../").append(relDiff).append("' style='max-width:300px; max-height:300px; margin-top:5px; border:1px solid #c10000;'/>")
						.append("</div>");
			}
			scHtml.append("</div>");
			scHtml.append("</div>");

			if ("PASS".equalsIgnoreCase(r.getStatus())) {
				scNode.pass(scHtml.toString());
			} else {
				scNode.fail(scHtml.toString());
				if (r.getDiffPath() != null) {
					scNode.addScreenCaptureFromPath("../" + relDiff, "Mismatched Regions Highlight");
				}
			}
		}
	}

	private static String getRelativePathForHtml(Path path) {
		if (path == null) {
			return "";
		}
		try {
			Path reportDir = Paths.get("test-output").toAbsolutePath().normalize();
			Path targetPath = path.toAbsolutePath().normalize();
			Path relative = reportDir.relativize(targetPath);
			return relative.toString().replace('\\', '/');
		} catch (Exception e) {
			return path.toAbsolutePath().toString();
		}
	}

	public static class ImageScenarioResult {
		private final String scenarioName;
		private final Path expectedPath;
		private final Path actualPath;
		private final Path diffPath;
		private final double ssimScore;
		private final double pixelDiffPercentage;
		private final String dimensionsInfo;
		private final String status;
		private final String failureReason;

		public ImageScenarioResult(String scenarioName, Path expectedPath, Path actualPath, Path diffPath,
				double ssimScore, double pixelDiffPercentage, String dimensionsInfo, String status,
				String failureReason) {
			this.scenarioName = scenarioName;
			this.expectedPath = expectedPath;
			this.actualPath = actualPath;
			this.diffPath = diffPath;
			this.ssimScore = ssimScore;
			this.pixelDiffPercentage = pixelDiffPercentage;
			this.dimensionsInfo = dimensionsInfo;
			this.status = status;
			this.failureReason = failureReason;
		}

		public String getScenarioName() {
			return scenarioName;
		}

		public Path getExpectedPath() {
			return expectedPath;
		}

		public Path getActualPath() {
			return actualPath;
		}

		public Path getDiffPath() {
			return diffPath;
		}

		public double getSsimScore() {
			return ssimScore;
		}

		public double getPixelDiffPercentage() {
			return pixelDiffPercentage;
		}

		public String getDimensionsInfo() {
			return dimensionsInfo;
		}

		public String getStatus() {
			return status;
		}

		public String getFailureReason() {
			return failureReason;
		}
	}
}
