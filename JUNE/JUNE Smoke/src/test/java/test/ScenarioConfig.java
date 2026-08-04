package test;

import org.openqa.selenium.By;

/**
 * Immutable value object encapsulating all parameters for a single test scenario.
 * Uses the Builder pattern to make scenario construction readable and self-documenting.
 *
 * <p>Usage example:</p>
 * <pre>
 * ScenarioConfig scenario = ScenarioConfig.builder("Scenario 1 (valid)")
 *     .input("98795545,98746094")
 *     .messageLocator(By.xpath("//p[contains(text(),'Request ID')]"))
 *     .expectedSnippet("You can download the file")
 *     .captureDownload(true)
 *     .scenarioKey("scenario-1-valid")
 *     .build();
 * </pre>
 *
 * @author Yash Shrivastava
 */
public class ScenarioConfig {

	private final String name;
	private final String input;
	private final String format; // null for non-image workflows
	private final By messageLocator;
	private final String expectedSnippet;
	private final boolean waitBeforeRequestId;
	private final String scenarioKey;
	private final String downloadFileContains;
	private final int downloadWaitSeconds;
	private final boolean captureDownload;
	private final boolean alertExpected; // Global Patent-specific

	private ScenarioConfig(Builder builder) {
		this.name = builder.name;
		this.input = builder.input;
		this.format = builder.format;
		this.messageLocator = builder.messageLocator;
		this.expectedSnippet = builder.expectedSnippet;
		this.waitBeforeRequestId = builder.waitBeforeRequestId;
		this.scenarioKey = builder.scenarioKey;
		this.downloadFileContains = builder.downloadFileContains;
		this.downloadWaitSeconds = builder.downloadWaitSeconds;
		this.captureDownload = builder.captureDownload;
		this.alertExpected = builder.alertExpected;
	}

	// ── Getters ──────────────────────────────────────────────

	public String getName()                 { return name; }
	public String getInput()                { return input; }
	public String getFormat()               { return format; }
	public By getMessageLocator()           { return messageLocator; }
	public String getExpectedSnippet()      { return expectedSnippet; }
	public boolean isWaitBeforeRequestId()  { return waitBeforeRequestId; }
	public String getScenarioKey()          { return scenarioKey; }
	public String getDownloadFileContains() { return downloadFileContains; }
	public int getDownloadWaitSeconds()     { return downloadWaitSeconds; }
	public boolean isCaptureDownload()      { return captureDownload; }
	public boolean isAlertExpected()        { return alertExpected; }

	// ── Builder ──────────────────────────────────────────────

	public static Builder builder(String name) {
		return new Builder(name);
	}

	public static class Builder {
		private final String name;
		private String input = "";
		private String format = null;
		private By messageLocator = null;
		private String expectedSnippet = "";
		private boolean waitBeforeRequestId = false;
		private String scenarioKey = "";
		private String downloadFileContains = "";
		private int downloadWaitSeconds = 10;
		private boolean captureDownload = false;
		private boolean alertExpected = false;

		private Builder(String name) {
			this.name = name;
		}

		public Builder input(String input) {
			this.input = input;
			return this;
		}

		public Builder format(String format) {
			this.format = format;
			return this;
		}

		public Builder messageLocator(By messageLocator) {
			this.messageLocator = messageLocator;
			return this;
		}

		public Builder expectedSnippet(String expectedSnippet) {
			this.expectedSnippet = expectedSnippet;
			return this;
		}

		public Builder waitBeforeRequestId(boolean waitBeforeRequestId) {
			this.waitBeforeRequestId = waitBeforeRequestId;
			return this;
		}

		public Builder scenarioKey(String scenarioKey) {
			this.scenarioKey = scenarioKey;
			return this;
		}

		public Builder downloadFileContains(String downloadFileContains) {
			this.downloadFileContains = downloadFileContains;
			return this;
		}

		public Builder downloadWaitSeconds(int downloadWaitSeconds) {
			this.downloadWaitSeconds = downloadWaitSeconds;
			return this;
		}

		public Builder captureDownload(boolean captureDownload) {
			this.captureDownload = captureDownload;
			return this;
		}

		public Builder alertExpected(boolean alertExpected) {
			this.alertExpected = alertExpected;
			return this;
		}

		public ScenarioConfig build() {
			return new ScenarioConfig(this);
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
