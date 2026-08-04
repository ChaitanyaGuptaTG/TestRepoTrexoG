package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PlatformRetryReportStore {

	private static final List<RetryEvent> EVENTS = new ArrayList<>();
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private PlatformRetryReportStore() {
	}

	public static synchronized void clear() {
		EVENTS.clear();
	}

	public static synchronized void record(String intentName, String scenarioName, String expectedResponse,
			String actualResponse, String retryTriggerReason, int retryCount, String retryOutcome,
			boolean retryApplied) {
		EVENTS.removeIf(e -> e.getIntentName().equalsIgnoreCase(intentName) && e.getScenarioName().equalsIgnoreCase(scenarioName));
		EVENTS.add(new RetryEvent(intentName, scenarioName, expectedResponse, actualResponse, retryTriggerReason,
				retryCount, retryOutcome, retryApplied, LocalDateTime.now().format(FORMATTER)));
	}

	public static synchronized void record(String workflowKey, String scenarioName, int attempt, int maxRetries,
			String status, String message) {
		String intent = mapWorkflowToIntent(workflowKey);
		boolean applied = attempt > 0;
		String outcome;
		if ("RECOVERED".equals(status) || "SUCCESS".equals(status)) {
			outcome = "Successfully Recovered";
		} else if ("FAILED".equals(status)) {
			outcome = "Failure";
		} else {
			outcome = status;
		}
		record(intent, scenarioName, "", "", message, attempt, outcome, applied);
	}

	public static synchronized List<RetryEvent> getEvents() {
		return new ArrayList<>(EVENTS);
	}

	public static synchronized boolean hasAppliedRetries() {
		return EVENTS.stream().anyMatch(RetryEvent::isRetryApplied);
	}

	public static synchronized String buildRetryRecoverySummary() {
		StringBuilder summary = new StringBuilder();
		boolean headerAdded = false;
		for (RetryEvent event : EVENTS) {
			if (event.isRetryApplied()) {
				if (!headerAdded) {
					summary.append("\n========================================\n");
					summary.append("Retry Recovery Summary\n");
					summary.append("========================================\n");
					headerAdded = true;
				}
				summary.append("Intent: ").append(event.getIntentName()).append("\n");
				summary.append("Scenario: ").append(event.getScenarioName()).append("\n");
				summary.append("Retry Applied: Yes\n");
				summary.append("Retry Count: ").append(event.getRetryCount()).append("\n");
				summary.append("Reason: ").append(event.getRetryTriggerReason()).append("\n");
				summary.append("Final Outcome: ").append(event.getRetryOutcome()).append("\n");
				summary.append("----------------------------------------\n");
			}
		}
		return summary.toString();
	}

	public static synchronized String buildEmailSummary() {
		if (EVENTS.isEmpty()) {
			return "Platform Retry Results: No transient platform error retries were triggered.";
		}

		StringBuilder summary = new StringBuilder();
		summary.append("Platform Retry Results").append(System.lineSeparator());
		for (RetryEvent event : EVENTS) {
			summary.append("- ").append(event.getIntentName()).append(" | ").append(event.getScenarioName())
					.append(" | Applied: ").append(event.isRetryApplied() ? "Yes" : "No")
					.append(" | Count: ").append(event.getRetryCount())
					.append(" | Outcome: ").append(event.getRetryOutcome())
					.append(" | ").append(event.getTimestamp())
					.append(" | ").append(event.getRetryTriggerReason()).append(System.lineSeparator());
		}
		return summary.toString();
	}

	public static String mapWorkflowToIntent(String workflowKey) {
		if (workflowKey == null) return "Unknown Search";
		switch (workflowKey.toLowerCase()) {
			case "aupatent": return "AU Patent Search";
			case "autrademark": return "AU Trademark Search";
			case "eppatent": return "EP Patent Search";
			case "globalpatent": return "Global Patent Search";
			case "uspatent": return "US Patent Search";
			case "ustrademark": return "US Trademark Search";
			default: return workflowKey + " Search";
		}
	}

	public static class RetryEvent {
		private final String intentName;
		private final String scenarioName;
		private final String expectedResponse;
		private final String actualResponse;
		private final String retryTriggerReason;
		private final int retryCount;
		private final String retryOutcome;
		private final boolean retryApplied;
		private final String timestamp;

		private RetryEvent(String intentName, String scenarioName, String expectedResponse, String actualResponse,
				String retryTriggerReason, int retryCount, String retryOutcome, boolean retryApplied,
				String timestamp) {
			this.intentName = intentName;
			this.scenarioName = scenarioName;
			this.expectedResponse = expectedResponse;
			this.actualResponse = actualResponse;
			this.retryTriggerReason = retryTriggerReason;
			this.retryCount = retryCount;
			this.retryOutcome = retryOutcome;
			this.retryApplied = retryApplied;
			this.timestamp = timestamp;
		}

		public String getIntentName() {
			return intentName;
		}

		public String getScenarioName() {
			return scenarioName;
		}

		public String getExpectedResponse() {
			return expectedResponse;
		}

		public String getActualResponse() {
			return actualResponse;
		}

		public String getRetryTriggerReason() {
			return retryTriggerReason;
		}

		public int getRetryCount() {
			return retryCount;
		}

		public String getRetryOutcome() {
			return retryOutcome;
		}

		public boolean isRetryApplied() {
			return retryApplied;
		}

		public String getTimestamp() {
			return timestamp;
		}
	}
}
