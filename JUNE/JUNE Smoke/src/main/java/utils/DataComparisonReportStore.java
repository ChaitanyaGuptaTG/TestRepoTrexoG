package utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class DataComparisonReportStore {

	private static final List<ScenarioComparisonReport> RESULTS = new ArrayList<>();
	private static final List<Path> COMPARISON_REPORTS = new ArrayList<>();

	private DataComparisonReportStore() {
	}

	public static synchronized void clear() {
		RESULTS.clear();
		COMPARISON_REPORTS.clear();
	}

	public static synchronized void record(ScenarioComparisonReport result) {
		RESULTS.add(result);
	}

	public static synchronized void recordComparisonReport(Path reportPath) {
		if (reportPath != null && !COMPARISON_REPORTS.contains(reportPath)) {
			COMPARISON_REPORTS.add(reportPath);
		}
	}

	public static synchronized List<Path> getComparisonReports() {
		return new ArrayList<>(COMPARISON_REPORTS);
	}

	public static synchronized List<ScenarioComparisonReport> getResults() {
		return new ArrayList<>(RESULTS);
	}

	public static synchronized boolean hasFailures() {
		return RESULTS.stream().anyMatch(result -> !result.isMatch());
	}

	public static synchronized String buildEmailSummary() {
		if (RESULTS.isEmpty()) {
			return "Data Comparison Results: No generated output files were validated in this run.";
		}

		StringBuilder summary = new StringBuilder();
		summary.append("========================================\n");
		summary.append("📊 DATA VALIDATION & COMPARISON SUMMARY\n");
		summary.append("========================================\n\n");

		// Group by intent/workflow
		Map<String, List<ScenarioComparisonReport>> byWorkflow = new LinkedHashMap<>();
		for (ScenarioComparisonReport report : RESULTS) {
			byWorkflow.computeIfAbsent(report.getWorkflowKey(), k -> new ArrayList<>()).add(report);
		}

		for (Map.Entry<String, List<ScenarioComparisonReport>> entry : byWorkflow.entrySet()) {
			String wf = entry.getKey();
			List<ScenarioComparisonReport> reports = entry.getValue();
			String intent = reports.get(0).getIntentName();
			boolean allMatch = reports.stream().allMatch(ScenarioComparisonReport::isMatch);
			int totalMatched = reports.stream().mapToInt(ScenarioComparisonReport::getTotalMatched).sum();
			int totalMismatches = reports.stream().mapToInt(ScenarioComparisonReport::getTotalMismatches).sum();
			Path reportFile = reports.stream()
					.map(ScenarioComparisonReport::getReportPath)
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);

			summary.append("Intent/Workflow: ").append(intent).append(" (").append(wf).append(")\n");
			summary.append("  • Status: ").append(allMatch ? "✅ PASSED" : "❌ FAILED").append("\n");
			summary.append("  • Total Matched Records/Cells: ").append(totalMatched).append("\n");
			summary.append("  • Total Mismatches: ").append(totalMismatches).append("\n");
			if (reportFile != null) {
				summary.append("  • Attached Report: ").append(reportFile.getFileName().toString()).append("\n");
			}
			summary.append("\n");
		}

		summary.append("Note: The detailed discrepancy highlighting can be reviewed directly in the attached Excel sheet comparison reports.\n");
		return summary.toString();
	}

	private static String valueOrDash(String value) {
		return value == null || value.isBlank() ? "-" : value;
	}

	public static class ScenarioComparisonReport {
		private final String workflowKey;
		private final String intentName;
		private final String scenarioKey;
		private final String scenarioNumber;
		private final String requestId;
		private final Path expectedFile;
		private final Path actualFile;
		private final double matchPercentage;
		private final List<String> discrepancies;
		private final boolean match;
		private int totalMatched;
		private int totalMismatches;
		private Path reportPath;

		private ScenarioComparisonReport(String workflowKey, String intentName, String scenarioKey,
				String scenarioNumber, String requestId, Path expectedFile, Path actualFile, double matchPercentage,
				List<String> discrepancies, boolean match) {
			this.workflowKey = workflowKey;
			this.intentName = intentName;
			this.scenarioKey = scenarioKey;
			this.scenarioNumber = scenarioNumber;
			this.requestId = requestId;
			this.expectedFile = expectedFile;
			this.actualFile = actualFile;
			this.matchPercentage = matchPercentage;
			this.discrepancies = discrepancies == null ? Collections.emptyList() : new ArrayList<>(discrepancies);
			this.match = match;
		}

		public static ScenarioComparisonReport pass(String workflowKey, String intentName, String scenarioKey,
				String scenarioNumber, String requestId, Path expectedFile, Path actualFile, double matchPercentage) {
			return new ScenarioComparisonReport(workflowKey, intentName, scenarioKey, scenarioNumber, requestId,
					expectedFile, actualFile, matchPercentage, Collections.emptyList(), true);
		}

		public static ScenarioComparisonReport fail(String workflowKey, String intentName, String scenarioKey,
				String scenarioNumber, String requestId, Path expectedFile, Path actualFile, double matchPercentage,
				List<String> discrepancies) {
			return new ScenarioComparisonReport(workflowKey, intentName, scenarioKey, scenarioNumber, requestId,
					expectedFile, actualFile, matchPercentage, discrepancies, false);
		}

		public static ScenarioComparisonReport custom(String workflowKey, String intentName, String scenarioKey,
				String scenarioNumber, String requestId, Path expectedFile, Path actualFile, double matchPercentage,
				List<String> discrepancies, boolean match, int totalMatched, int totalMismatches, Path reportPath) {
			ScenarioComparisonReport report = new ScenarioComparisonReport(workflowKey, intentName, scenarioKey,
					scenarioNumber, requestId, expectedFile, actualFile, matchPercentage, discrepancies, match);
			report.totalMatched = totalMatched;
			report.totalMismatches = totalMismatches;
			report.reportPath = reportPath;
			return report;
		}

		public String getWorkflowKey() {
			return workflowKey;
		}

		public String getIntentName() {
			return intentName;
		}

		public String getScenarioKey() {
			return scenarioKey;
		}

		public String getScenarioNumber() {
			return scenarioNumber;
		}

		public String getRequestId() {
			return requestId;
		}

		public Path getExpectedFile() {
			return expectedFile;
		}

		public Path getActualFile() {
			return actualFile;
		}

		public double getMatchPercentage() {
			return matchPercentage;
		}

		public List<String> getDiscrepancies() {
			return new ArrayList<>(discrepancies);
		}

		public List<String> getLimitedDiscrepancies(int max) {
			if (discrepancies.size() <= max) {
				return getDiscrepancies();
			}

			List<String> limited = new ArrayList<>(discrepancies.subList(0, max));
			limited.add("Additional discrepancies omitted from email: " + (discrepancies.size() - max));
			return limited;
		}

		public boolean isMatch() {
			return match;
		}

		public int getTotalMatched() {
			return totalMatched;
		}

		public void setTotalMatched(int totalMatched) {
			this.totalMatched = totalMatched;
		}

		public int getTotalMismatches() {
			return totalMismatches;
		}

		public void setTotalMismatches(int totalMismatches) {
			this.totalMismatches = totalMismatches;
		}

		public Path getReportPath() {
			return reportPath;
		}

		public void setReportPath(Path reportPath) {
			this.reportPath = reportPath;
		}

		public String getDisplayName() {
			return intentName + " Scenario " + scenarioNumber + " (" + scenarioKey + ")";
		}
	}
}
