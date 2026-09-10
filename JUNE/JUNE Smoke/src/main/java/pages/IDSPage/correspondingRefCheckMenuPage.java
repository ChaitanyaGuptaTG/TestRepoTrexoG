package pages.IDSPage;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import pages.IDSPage.helpers.ContinueDialogHelper;
import pages.IDSPage.helpers.IdsDropdownHelper;
import pages.IDSPage.helpers.QueryInputHelper;
import pages.IDSPage.helpers.StatusPagePoller;
import utils.Log;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class correspondingRefCheckMenuPage extends BasePage {

    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);

    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(90);
    private static final long EXPECTED_ACK_SECONDS = 60L;

    private static final String WORKFLOW_NAME = "Corresponding RefCheck";
    private static final String APPLICATION_NUMBER_INSTRUCTION = "Please enter the application number.";
    private static final String REFERENCE_LIST_INSTRUCTION = "Please provide a list of patent references to be filled, with the country code prefixed.";

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("ID\\s*(\\d+)");

    // No substring overlap with the other IDS option names, so - unlike Reference Count's
    // option locator - this does not need to exclude any sibling option's text.
    private static final By CORRESPONDING_REF_CHECK_OPTION = By.xpath("//li[@role='option' and contains(.,'" + WORKFLOW_NAME + "')]");

    // Plain <span>Corresponding RefCheck</span> transcript bubble, same shape as every
    // other IDS workflow's bubble (no aria-label to anchor on instead).
    private static final By CORRESPONDING_REF_CHECK_BUBBLE = By.xpath("(//span[normalize-space()='" + WORKFLOW_NAME + "'])[last()]");

    // ── Step 1: application number ──────────────────────────────────────────
    private static final By ALL_APPLICATION_NUMBER_INSTRUCTIONS = By.xpath("//span[contains(text(),'" + APPLICATION_NUMBER_INSTRUCTION + "')]");

    // ── Step 2: patent reference list (newline- or comma-separated; both observed) ──
    private static final By ALL_REFERENCE_LIST_INSTRUCTIONS = By.xpath("//span[contains(text(),'" + REFERENCE_LIST_INSTRUCTION + "')]");

    private static final By STOP_BUTTON = By.xpath("//*[name()='svg' and @data-testid='StopCircleOutlinedIcon']");

    private static final By ALL_SUBMITTED_MESSAGES = By.xpath("//span[contains(text(),'is now submitted')]");

    // contains(., ...), not contains(text(), ...) - the completed message can carry more
    // than one text node (see referenceCountMenuPage's note on the same shape), so this
    // stays safe even if a future build adds a leading sentence the way Reference Count's
    // "The total references count is N." does.
    private static final By ALL_COMPLETED_MESSAGES = By.xpath("//p[contains(.,'Request ID') and contains(.,'has been completed')]");

    private static final By STATUS_LINK = By.xpath("(//span[contains(text(),'is now submitted')]//span[normalize-space()='click here'])[last()]");

    // ── Status page (reached via the async "click here" link) ──────────────────
    private static final String EXPECTED_SUBTASK = "Corresponding Reference Check";

    // ── Synchronous completion path: inline "Download" link inside the chat message ──
    private static final By COMPLETED_DOWNLOAD_LINK = By.xpath("(//p[contains(.,'Request ID') and contains(.,'has been completed')]//a[normalize-space()='Download'])[last()]");

    // ── Continue dialog: scoped by WORKFLOW NAME ────────────────────────────────
    // Observed live: for this workflow's async path, this prompt renders immediately
    // alongside the "is now submitted" message rather than being gated behind a status-page
    // visit (unlike Reference Count) - waitForConfirmation() below only waits for visibility,
    // so it holds correctly either way.
    private static final String CONTINUE_ANCHOR =
            "//*[contains(text(),'Would you like to continue with " + WORKFLOW_NAME + "')]";

    private static final By CONTINUE_CONFIRMATION = By.xpath("(" + CONTINUE_ANCHOR + ")[last()]");

    private static final By CONTINUE_YES = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='YES' or normalize-space()='Yes'])[last()]");

    private static final By CONTINUE_NO = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='NO' or normalize-space()='No'])[last()]");

    private static final By WORKFLOW_EXITED_MESSAGE = By.xpath("(//*[contains(text(),'Enter your query or select a task to get started')])[last()]");

    // Element counts, not "last ID seen" - see referenceCountMenuPage for why (the
    // "has been completed" shape is shared across IDS workflows, so a stale message
    // elsewhere in the transcript could otherwise be misread as a new acknowledgement).
    private int submittedMessageBaseline = 0;
    private int completedMessageBaseline = 0;

    // Recorded so a full acknowledgement timeout can retry by re-submitting the same query.
    private String lastQuery;

    private final List<String> requestIds = new ArrayList<>();

    private final IdsDropdownHelper dropdown;
    private final QueryInputHelper queryInput;
    private final ContinueDialogHelper continueDialog;
    private final StatusPagePoller statusPagePoller;

    public correspondingRefCheckMenuPage(WebDriver driver) {
        super(driver);
        this.dropdown = new IdsDropdownHelper(driver);
        this.queryInput = new QueryInputHelper(driver);
        this.continueDialog = new ContinueDialogHelper(driver);
        this.statusPagePoller = new StatusPagePoller(driver, EXPECTED_SUBTASK);
    }

    public void clickIdsDropdown() {
        dropdown.clickIdsDropdown();
    }

    public void selectCorrespondingRefCheck() {
        Assert.assertTrue(driver.findElement(CORRESPONDING_REF_CHECK_OPTION).isDisplayed(), "The workflow option should be visible.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(CORRESPONDING_REF_CHECK_OPTION)).click();
        Log.info("Selected '{}' from the IDS dropdown", WORKFLOW_NAME);
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        dropdown.verifyDropdownAvailableAfterSelection(CORRESPONDING_REF_CHECK_OPTION, WORKFLOW_NAME, QUICK_PROBE_TIMEOUT);
    }

    public void verifyCorrespondingRefCheckBubble() {
        Assert.assertTrue(waitVisible(CORRESPONDING_REF_CHECK_BUBBLE).isDisplayed(), "'" + WORKFLOW_NAME + "' bubble is not displayed in the transcript after selection");
        Log.pass("'{}' bubble is displayed in the transcript", WORKFLOW_NAME);
    }

    public int applicationNumberInstructionCount() {
        return driver.findElements(ALL_APPLICATION_NUMBER_INSTRUCTIONS).size();
    }

    public void verifyNewApplicationNumberInstruction(int previousCount) {
        try {
            new WebDriverWait(driver, UI_TIMEOUT).until(d -> d.findElements(ALL_APPLICATION_NUMBER_INSTRUCTIONS).size() > previousCount);
        } catch (TimeoutException e) {
            throw new TimeoutException("No NEW application-number instruction appeared for " + WORKFLOW_NAME
                    + " - still showing the " + previousCount + " from earlier turns", e);
        }
        Log.pass("New application-number instruction displayed for {}", WORKFLOW_NAME);
    }

    public int referenceListInstructionCount() {
        return driver.findElements(ALL_REFERENCE_LIST_INSTRUCTIONS).size();
    }

    public void verifyNewReferenceListInstruction(int previousCount) {
        try {
            new WebDriverWait(driver, UI_TIMEOUT).until(d -> d.findElements(ALL_REFERENCE_LIST_INSTRUCTIONS).size() > previousCount);
        } catch (TimeoutException e) {
            throw new TimeoutException("No NEW reference-list instruction appeared for " + WORKFLOW_NAME
                    + " - still showing the " + previousCount + " from earlier turns", e);
        }
        Log.pass("New reference-list instruction displayed for {}", WORKFLOW_NAME);
    }

    public void enterQuery(String query) {
        lastQuery = query;
        queryInput.enterQuery(query);
    }

    public void enterIntentAsQuery(String intentText) {
        queryInput.enterIntentAsQuery(intentText);
    }

    public void enterSingleReference(String reference) {
        enterQuery(reference);
    }

    public void enterReferencesCommaSeparated(List<String> references) {
        enterQuery(String.join(",", references));
    }

    public void clickSubmitButton() {
        queryInput.clickSubmitButton();
    }

    public void baselineExistingRequestIds() {
        submittedMessageBaseline = driver.findElements(ALL_SUBMITTED_MESSAGES).size();
        completedMessageBaseline = driver.findElements(ALL_COMPLETED_MESSAGES).size();
        Log.info("Baselined transcript - {} existing 'submitted' message(s), {} existing 'completed' message(s)",
                submittedMessageBaseline, completedMessageBaseline);
    }

    public String awaitSubmissionAcknowledgement() {
        Log.info("Validating {} submission", WORKFLOW_NAME);

        Acknowledgement ack;
        try {
            ack = waitForNewAcknowledgement();
        } catch (TimeoutException firstAttempt) {
            Log.warn("No acknowledgement arrived within {}s for '{}' - retrying once by re-submitting the same query",
                    BACKEND_TIMEOUT.getSeconds(), lastQuery);
            Assert.assertNotNull(lastQuery, "Cannot retry - no query was recorded to resubmit");

            enterQuery(lastQuery);
            clickSubmitButton();

            ack = waitForNewAcknowledgement();
            Log.pass("Retry succeeded - acknowledgement arrived on the second attempt");
        }

        Assert.assertTrue(ack.requestId.matches("\\d+"), "Request ID should be numeric but was: " + ack.requestId);

        requestIds.add(ack.requestId);

        boolean stillGenerating = !new WebDriverWait(driver, BACKEND_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(STOP_BUTTON));
        Assert.assertFalse(stillGenerating, "Stop button is still present - the request has not finished generating");

        if (ack.isAsync) {
            statusPagePoller.followAsyncStatusFlow(STATUS_LINK, ack.requestId);
        } else {
            Log.pass("Request {} completed synchronously ('has been completed') - no status page to check", ack.requestId);
            statusPagePoller.downloadFromCompletedMessage(COMPLETED_DOWNLOAD_LINK, ack.requestId);
        }

        return ack.requestId;
    }

    public List<String> submittedRequestIds() {
        return List.copyOf(requestIds);
    }

    public void clickContinueYes() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        int before = applicationNumberInstructionCount();

        continueDialog.clickYes(CONTINUE_YES, WORKFLOW_NAME);

        // "Yes" leads back to the FIRST step (application number), not the reference-list
        // step - confirmed live, matches the "Please enter the application number." prompt
        // reappearing after continuing.
        verifyNewApplicationNumberInstruction(before);
        Log.pass("Instruction prompt reappeared - ready for next application number");
    }

    public void clickContinueNo() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickNo(CONTINUE_NO, WORKFLOW_NAME);
        continueDialog.waitUntilNoButtonDisabled(CONTINUE_NO, WORKFLOW_NAME);

        waitVisible(WORKFLOW_EXITED_MESSAGE);
        Log.pass("Workflow exited - chat returned to its default prompt");
    }

    private Acknowledgement waitForNewAcknowledgement() {
        long start = System.currentTimeMillis();
        try {
            Acknowledgement ack = new WebDriverWait(driver, BACKEND_TIMEOUT).until(d -> {
                List<WebElement> submitted = d.findElements(ALL_SUBMITTED_MESSAGES);
                if (submitted.size() > submittedMessageBaseline) {
                    String id = parseRequestId(submitted.get(submitted.size() - 1).getText());
                    if (id != null) {
                        return new Acknowledgement(id, true);
                    }
                }
                List<WebElement> completed = d.findElements(ALL_COMPLETED_MESSAGES);
                if (completed.size() > completedMessageBaseline) {
                    String id = parseRequestId(completed.get(completed.size() - 1).getText());
                    if (id != null) {
                        return new Acknowledgement(id, false);
                    }
                }
                return null;
            });

            long elapsedMillis = System.currentTimeMillis() - start;
            Log.info("Backend acknowledged the request in {} ms ({}s) - {}",
                    elapsedMillis, elapsedMillis / 1000, ack.isAsync ? "async (submitted)" : "sync (completed)");

            if (elapsedMillis > EXPECTED_ACK_SECONDS * 1000) {
                Log.warn("Acknowledgement took {}s - above the expected {}s ceiling", elapsedMillis / 1000, EXPECTED_ACK_SECONDS);
            }

            submittedMessageBaseline = driver.findElements(ALL_SUBMITTED_MESSAGES).size();
            completedMessageBaseline = driver.findElements(ALL_COMPLETED_MESSAGES).size();

            return ack;

        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Timed out after " + BACKEND_TIMEOUT.getSeconds()
                            + "s waiting for a NEW acknowledgement ('is now submitted' or 'has been completed') "
                            + "for " + WORKFLOW_NAME + ".", e);
        }
    }

    private static final class Acknowledgement {
        final String requestId;
        final boolean isAsync;

        Acknowledgement(String requestId, boolean isAsync) {
            this.requestId = requestId;
            this.isAsync = isAsync;
        }
    }

    private static String parseRequestId(String messageText) {
        if (messageText == null) {
            return null;
        }
        Matcher matcher = REQUEST_ID_PATTERN.matcher(messageText);
        return matcher.find() ? matcher.group(1) : null;
    }
}

