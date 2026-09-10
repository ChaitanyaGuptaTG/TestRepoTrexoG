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
import pages.IDSPage.helpers.StatusPagePoller;
import utils.Log;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class referenceExtractorMenuPage extends BasePage {

    // ── Timeouts ──────────────────────────────────────────────────────────────
    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration OPTION_VISIBLE_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration BUBBLE_RETRY_PROBE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration RETRY_SETTLE_BUFFER = Duration.ofSeconds(2);
    private static final int MAX_SELECTION_ATTEMPTS = 3;

    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(90);
    private static final long EXPECTED_ACK_SECONDS = 60L;

    // Constants
    private static final String WORKFLOW_NAME = "Reference Extractor";
    private static final String UPLOAD_INSTRUCTION_TEXT = "Upload DOC/DOCX or PDF files to extract references";
    private static final String EXPECTED_SUBTASK = "Reference Extractor";

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("ID\\s*(\\d+)");

    // Sidebar locators
    private static final By REFERENCE_EXTRACTOR_OPTION = By.xpath(
            "//li[@role='option' and contains(.,'" + WORKFLOW_NAME + "')]");

    // Chat transcript: workflow selection bubble
    private static final By ALL_REFERENCE_EXTRACTOR_BUBBLES = By.xpath(
            "//span[normalize-space()='" + WORKFLOW_NAME + "'][not(ancestor::li[@role='option'])]");

    // Bot prompt: asking to upload a file
    private static final By UPLOAD_INSTRUCTION = By.xpath("//*[contains(text(),\"" + UPLOAD_INSTRUCTION_TEXT + "\")]");

    // File upload

    private static final By FILE_INPUT = By.cssSelector("input[type='file']");

    private static final By ALL_ATTACHED_FILE_BUBBLES = By.xpath("//span[contains(text(),'file attached')]");

    // ── Submission acknowledgement (async only - no synchronous shape observed) ──
    private static final By ALL_SUBMITTED_MESSAGES = By.xpath("//span[contains(text(),'is now submitted')]");

    private static final By STATUS_LINK = By.xpath(
            "(//span[contains(text(),'is now submitted')]//span[normalize-space()='click here'])[last()]");

    // Continue dialog: scoped by WORKFLOW NAME
    private static final String CONTINUE_ANCHOR = "//*[contains(text(),'Would you like to continue with " + WORKFLOW_NAME + "')]";

    private static final By CONTINUE_CONFIRMATION = By.xpath("(" + CONTINUE_ANCHOR + ")[last()]");

    private static final By CONTINUE_YES = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='YES' or normalize-space()='Yes'])[last()]");

    private static final By CONTINUE_NO = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='NO' or normalize-space()='No'])[last()]");

    // ── State ─────────────────────────────────────────────────────────────────
    private int bubbleCountBeforeSelection = 0;
    private int attachedBubbleBaseline = 0;
    private int submittedMessageBaseline = 0;
    private final List<String> requestIds = new ArrayList<>();

    private final IdsDropdownHelper dropdown;
    private final ContinueDialogHelper continueDialog;
    private final StatusPagePoller statusPagePoller;

    public referenceExtractorMenuPage(WebDriver driver) {
        super(driver);
        this.dropdown = new IdsDropdownHelper(driver);
        this.continueDialog = new ContinueDialogHelper(driver);
        this.statusPagePoller = new StatusPagePoller(driver, EXPECTED_SUBTASK);
    }

    // ── Sidebar actions ───────────────────────────────────────────────────────

    public void clickIdsDropdown() {
        dropdown.clickIdsDropdown();
    }

    public void selectReferenceExtractor() {
        bubbleCountBeforeSelection = driver.findElements(ALL_REFERENCE_EXTRACTOR_BUBBLES).size();

        for (int attempt = 1; attempt <= MAX_SELECTION_ATTEMPTS; attempt++) {
            boolean clicked = tryClickReferenceExtractorOption();
            if (clicked && newBubbleAppearedWithin(BUBBLE_RETRY_PROBE_TIMEOUT)) {
                return;
            }

            if (attempt == MAX_SELECTION_ATTEMPTS) {
                break; // exhausted retries - verifyReferenceExtractorBubble() reports the final failure
            }

            Log.warn("No '{}' bubble within {}s of attempt {}/{} - forcing the dropdown closed and " +
                            "reopening before retrying (known app-side glitch can replay a different IDS " +
                            "workflow instead of switching)",
                    WORKFLOW_NAME, BUBBLE_RETRY_PROBE_TIMEOUT.getSeconds(), attempt, MAX_SELECTION_ATTEMPTS);
            dropdown.closeDropdown();
            sleepUninterruptibly(RETRY_SETTLE_BUFFER);
            dropdown.clickIdsDropdown();
        }
    }

    private boolean tryClickReferenceExtractorOption() {
        try {
            new WebDriverWait(driver, OPTION_VISIBLE_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(REFERENCE_EXTRACTOR_OPTION));

            // The option can report visible within milliseconds of the dropdown opening -
            // before the app has finished settling. Give it a real beat before trusting it,
            // same fix already applied to referenceDownloaderMenuPage.
            sleepUninterruptibly(RETRY_SETTLE_BUFFER);

            WebElement option = new WebDriverWait(driver, QUICK_PROBE_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(REFERENCE_EXTRACTOR_OPTION));
            Log.info("Clicking IDS dropdown option with text: '{}'", option.getText());
            safeClick(option);
            Log.info("Selected '{}' from the IDS dropdown", WORKFLOW_NAME);
            return true;
        } catch (TimeoutException e) {
            Log.warn("'{}' option did not become visible/clickable within {}s", WORKFLOW_NAME, OPTION_VISIBLE_TIMEOUT.getSeconds());
            return false;
        }
    }

    private boolean newBubbleAppearedWithin(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(d -> d.findElements(ALL_REFERENCE_EXTRACTOR_BUBBLES).size() > bubbleCountBeforeSelection);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        dropdown.verifyDropdownAvailableAfterSelection(REFERENCE_EXTRACTOR_OPTION, WORKFLOW_NAME, QUICK_PROBE_TIMEOUT);
    }

    public void verifyUploadInstructionShown() {
        Assert.assertTrue(waitVisible(UPLOAD_INSTRUCTION).isDisplayed(),
                "Upload instruction is not displayed for " + WORKFLOW_NAME);
        Log.pass("Upload instruction displayed for {}", WORKFLOW_NAME);
    }

    // ── File upload & submission ─────────────────────────────────────────────

    public void baselineExistingState() {
        attachedBubbleBaseline = driver.findElements(ALL_ATTACHED_FILE_BUBBLES).size();
        submittedMessageBaseline = driver.findElements(ALL_SUBMITTED_MESSAGES).size();
        Log.info("Baselined transcript - {} existing 'file attached' bubble(s), {} existing 'submitted' message(s)",
                attachedBubbleBaseline, submittedMessageBaseline);
    }

    public void uploadFile(Path file) {
        sleepUninterruptibly(RETRY_SETTLE_BUFFER);
        WebElement input = new WebDriverWait(driver, QUICK_PROBE_TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(FILE_INPUT));
        input.sendKeys(file.toAbsolutePath().toString());
        Log.info("Uploaded file: {}", file.getFileName());
    }

    public void verifyFileAttachedBubble(String fileName) {
        try {
            new WebDriverWait(driver, UI_TIMEOUT)
                    .until(d -> d.findElements(ALL_ATTACHED_FILE_BUBBLES).size() > attachedBubbleBaseline);
        } catch (TimeoutException e) {
            throw new TimeoutException("No NEW 'file attached' bubble appeared after uploading " + fileName, e);
        }
        List<WebElement> bubbles = driver.findElements(ALL_ATTACHED_FILE_BUBBLES);
        String lastBubbleText = bubbles.get(bubbles.size() - 1).getText();
        Assert.assertTrue(lastBubbleText.contains(fileName),
                "Attached-file bubble does not contain '" + fileName + "'. Actual: " + lastBubbleText);
        attachedBubbleBaseline = bubbles.size();
        Log.pass("'{}' attached and shown in transcript", fileName);
    }

    public void clickSubmitButton() {
        safeClick(By.xpath("//*[name()='svg' and @data-testid='SendOutlinedIcon']"));
        Log.info("Submitted the query");
    }

    public String awaitSubmissionAcknowledgement() {
        Log.info(" Validating {} submission ", WORKFLOW_NAME);

        String requestId = waitForNewAcknowledgement();
        Assert.assertTrue(requestId.matches("\\d+"), "Request ID should be numeric but was: " + requestId);
        requestIds.add(requestId);

        statusPagePoller.followAsyncStatusFlow(STATUS_LINK, requestId);

        return requestId;
    }

    private String waitForNewAcknowledgement() {
        long start = System.currentTimeMillis();
        try {
            String requestId = new WebDriverWait(driver, BACKEND_TIMEOUT).until(d -> {
                List<WebElement> submitted = d.findElements(ALL_SUBMITTED_MESSAGES);
                if (submitted.size() <= submittedMessageBaseline) {
                    return null;
                }
                return parseRequestId(submitted.get(submitted.size() - 1).getText());
            });

            long elapsedMillis = System.currentTimeMillis() - start;
            Log.info("Backend acknowledged the request in {} ms ({}s)", elapsedMillis, elapsedMillis / 1000);
            if (elapsedMillis > EXPECTED_ACK_SECONDS * 1000) {
                Log.warn("Acknowledgement took {}s - above the expected {}s ceiling", elapsedMillis / 1000, EXPECTED_ACK_SECONDS);
            }

            submittedMessageBaseline = driver.findElements(ALL_SUBMITTED_MESSAGES).size();
            return requestId;

        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Timed out after " + BACKEND_TIMEOUT.getSeconds()
                            + "s waiting for a NEW 'is now submitted' acknowledgement for " + WORKFLOW_NAME, e);
        }
    }

    public List<String> submittedRequestIds() {
        return List.copyOf(requestIds);
    }

    // Continue dialog

    public void clickContinueYes() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickYes(CONTINUE_YES, WORKFLOW_NAME);

        waitVisible(UPLOAD_INSTRUCTION);
        Log.pass("Upload prompt reappeared - ready for next input");
    }

    public void clickContinueNo() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickNo(CONTINUE_NO, WORKFLOW_NAME);
        continueDialog.waitUntilNoButtonDisabled(CONTINUE_NO, WORKFLOW_NAME);
    }

    //Private helpers

    private static void sleepUninterruptibly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.warn("Interrupted while waiting before retrying the '{}' selection", WORKFLOW_NAME);
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
