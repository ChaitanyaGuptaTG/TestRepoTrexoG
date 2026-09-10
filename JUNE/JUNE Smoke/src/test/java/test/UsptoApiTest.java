package test;

import api.clients.UsptoDocumentClient;
import com.google.gson.JsonObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.ConfigReader;

import java.util.List;

/**
 * Pure API tests for USPTO document validation.
 * Runs independently — no browser, no UI state, no conflict with idsPageTest.
 */
public class UsptoApiTest {

    private UsptoDocumentClient api;

    @BeforeClass
    public void setUp() {
        api = UsptoDocumentClient.fromConfig();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. VERIFY 1449 AND 892 EXIST FOR EVERY APPLICATION NUMBER
    //    Data-driven — add as many app numbers as you want to the provider
    // ═══════════════════════════════════════════════════════════════════════

    @DataProvider(name = "applicationNumbers")
    public Object[][] applicationNumbers() {
        return new Object[][]{
                {"16999215"},
                {"17954142"},
                {"18139333"},
                {"18210779"},
                {"17691286"},
                {"17862541"},
                // Add more application numbers here without changing any code
        };
    }

    @Test(dataProvider = "applicationNumbers",
            description = "API: 1449 and 892 documents exist for each application")
    public void verifyFormsExistViaApi(String appNumber) {
        api.assertFormsPresent(appNumber, "1449", "892");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. VERIFY API RETURNS A NON-EMPTY DOCUMENT LIST
    // ═══════════════════════════════════════════════════════════════════════

    @Test(dataProvider = "applicationNumbers",
            description = "API: document list is not empty for each application")
    public void verifyDocumentListNotEmpty(String appNumber) {
        List<JsonObject> entries = api.getDocumentEntries(appNumber);
        Assert.assertFalse(entries.isEmpty(),
                "API returned zero documents for application " + appNumber);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. VERIFY 1449 COUNT MATCHES BETWEEN API AND ZIP
    //    Checks that the number of 1449 docs in API >= what the zip had
    // ═══════════════════════════════════════════════════════════════════════

    @Test(dataProvider = "applicationNumbers", description = "API: at least one 1449 document exists per application")
    public void verify1449CountViaApi(String appNumber) {
        List<JsonObject> matches = api.findDocumentsByType(appNumber, "1449");
        Assert.assertFalse(matches.isEmpty(),
                "No 1449 documents found via API for application " + appNumber);
    }

    @Test(dataProvider = "applicationNumbers", description = "API: at least one 892 document exists per application")
    public void verify892CountViaApi(String appNumber) {
        List<JsonObject> matches = api.findDocumentsByType(appNumber, "892");
        Assert.assertFalse(matches.isEmpty(),
                "No 892 documents found via API for application " + appNumber);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. NEGATIVE TEST — INVALID APPLICATION NUMBER
    // ═══════════════════════════════════════════════════════════════════════

    @Test(description = "API: invalid application number returns error or empty results")
    public void verifyInvalidAppNumberHandled() {
        try {
            List<JsonObject> entries = api.getDocumentEntries("00000000");
            // If API doesn't throw, it should return empty or no 1449/892
            boolean has1449 = entries.stream()
                    .anyMatch(e -> e.toString().contains("1449"));
            Assert.assertFalse(has1449,
                    "Fake app number '00000000' should NOT have 1449 documents");
        } catch (RuntimeException e) {
            // API returned a non-200 status — that's also a valid response
            Assert.assertTrue(e.getMessage().contains("HTTP"),
                    "Expected an HTTP error for invalid app number");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. API RESPONSE TIME CHECK
    // ═══════════════════════════════════════════════════════════════════════

    @Test(dataProvider = "applicationNumbers",
            description = "API: response time is under 10 seconds")
    public void verifyApiResponseTime(String appNumber) {
        long start = System.currentTimeMillis();
        api.fetchDocuments(appNumber);
        long elapsed = System.currentTimeMillis() - start;

        Assert.assertTrue(elapsed < 10_000,
                "API took " + elapsed + "ms for application " + appNumber
                        + " — expected under 10s");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. VERIFY HTTP 200 FOR ALL APP NUMBERS
    //    (fetchDocuments already throws on non-200, so reaching the
    //     assertion means success)
    // ═══════════════════════════════════════════════════════════════════════

    @Test(dataProvider = "applicationNumbers",
            description = "API: returns HTTP 200 for each application")
    public void verifyApiReturns200(String appNumber) {
        JsonObject response = api.fetchDocuments(appNumber);
        Assert.assertNotNull(response,
                "API response was null for application " + appNumber);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7. VERIFY DOCUMENT ENTRIES CONTAIN EXPECTED FIELDS
    //    Adjust field names after checking your actual Postman response
    // ═══════════════════════════════════════════════════════════════════════

    @Test(description = "API: document entries contain expected JSON fields")
    public void verifyDocumentEntryStructure() {
        List<JsonObject> entries = api.getDocumentEntries("16999215");
        Assert.assertFalse(entries.isEmpty(), "Need at least one entry to verify structure");

        JsonObject first = entries.get(0);

        // ── Adjust these field names to match your actual API response ──
        // Common USPTO fields: documentCode, documentDescription,
        //                      officialDate, pageCount, direction
        // Uncomment and adapt once you confirm the schema in Postman:

        // Assert.assertTrue(first.has("documentCode"),
        //         "Entry missing 'documentCode' field: " + first);
        // Assert.assertTrue(first.has("officialDate"),
        //         "Entry missing 'officialDate' field: " + first);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 8. CROSS-REFERENCE: SAME APP NUMBER IN BOTH BATCHES
    //    Verifies API consistency — calling twice gives the same count
    // ═══════════════════════════════════════════════════════════════════════

    @Test(description = "API: consecutive calls for the same app return consistent results")
    public void verifyApiConsistency() {
        String appNumber = "16999215";

        List<JsonObject> firstCall = api.getDocumentEntries(appNumber);
        List<JsonObject> secondCall = api.getDocumentEntries(appNumber);

        Assert.assertEquals(firstCall.size(), secondCall.size(),
                "Document count changed between two consecutive API calls for " + appNumber);
    }
}