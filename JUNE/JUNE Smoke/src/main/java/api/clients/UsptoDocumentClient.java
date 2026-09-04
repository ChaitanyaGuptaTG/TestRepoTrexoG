package api.clients;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import utils.ConfigReader;
import utils.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Client for the USPTO Patent Application Documents API.
 * Lives in api/clients/ following the project's existing structure.
 * <p>
 * Typical usage (from a page object or test):
 * UsptoDocumentClient api = UsptoDocumentClient.fromConfig();
 * api.assertFormsPresent("18263932", "1449", "892");
 */
public class UsptoDocumentClient {

    private static final String BASE_URL =
            "https://api.uspto.gov/api/v1/patent/applications/%s/documents";

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;

    // ── Construction ──────────────────────────────────────────────────────────

    public UsptoDocumentClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.gson = new Gson();
    }

    /**
     * Convenience factory that reads the API key from your existing
     * config properties (config-qa.properties, config-prod.properties, etc.).
     * <p>
     * Expected property:  uspto.api.key=qkysdhhelcgwesdjwdenhficgfprsf
     */
    public static UsptoDocumentClient fromConfig() {
        Properties config = ConfigReader.loadConfig();
        String key = config.getProperty("uspto.api.key");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Missing 'uspto.api.key' in config properties");
        }
        return new UsptoDocumentClient(key);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Calls the USPTO API and returns the raw JSON response.
     */
    public JsonObject fetchDocuments(String applicationNumber) {
        String url = String.format(BASE_URL, applicationNumber.trim());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("X-API-KEY", apiKey)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "USPTO API returned HTTP " + response.statusCode()
                                + " for application " + applicationNumber
                                + ": " + response.body());
            }

            Log.info("USPTO API — application {}: HTTP {}", applicationNumber, response.statusCode());
            return gson.fromJson(response.body(), JsonObject.class);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException(
                    "Failed to call USPTO API for application " + applicationNumber, e);
        }
    }

    /**
     * Extracts the array of document entries from the API response.
     * Confirmed via curl against the live endpoint: the array is "documentBag",
     * with each entry's form type in its "documentCode" field (e.g. "1449", "892").
     */
    public List<JsonObject> getDocumentEntries(String applicationNumber) {
        JsonObject root = fetchDocuments(applicationNumber);
        List<JsonObject> entries = new ArrayList<>();

        JsonArray docs = findDocumentArray(root);
        if (docs != null) {
            for (JsonElement el : docs) {
                if (el.isJsonObject()) {
                    entries.add(el.getAsJsonObject());
                }
            }
        }

        Log.info("USPTO API — application {}: {} document entries found",
                applicationNumber, entries.size());
        return entries;
    }

    /**
     * Returns true if any document entry's "documentCode" exactly matches the
     * given form code (e.g. "1449", "892").
     */
    public boolean hasDocumentType(String applicationNumber, String formCode) {
        List<JsonObject> entries = getDocumentEntries(applicationNumber);
        boolean found = entries.stream().anyMatch(e -> matchesFormCode(e, formCode));
        Log.info("USPTO API — application {} — form {} present: {}",
                applicationNumber, formCode, found);
        return found;
    }

    /**
     * Returns only the document entries whose "documentCode" matches the given form code.
     */
    public List<JsonObject> findDocumentsByType(String applicationNumber, String formCode) {
        return getDocumentEntries(applicationNumber).stream()
                .filter(e -> matchesFormCode(e, formCode))
                .collect(Collectors.toList());
    }

    /**
     * Fetches the document list ONCE and returns the count for each requested form
     * code, keyed by form code - avoids a separate API round-trip (and duplicate
     * "document entries found" log line) per form when a caller needs several counts.
     */
    public Map<String, Integer> countDocumentsByType(String applicationNumber, String... formCodes) {
        List<JsonObject> entries = getDocumentEntries(applicationNumber);

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String code : formCodes) {
            int count = (int) entries.stream().filter(e -> matchesFormCode(e, code)).count();
            counts.put(code, count);
        }
        return counts;
    }

    /**
     * Asserts that the API response contains documents matching every
     * supplied form code.  Throws AssertionError if any is missing.
     * <p>
     * Usage:  api.assertFormsPresent("18263932", "1449", "892");
     */
    public void assertFormsPresent(String applicationNumber, String... formCodes) {
        List<JsonObject> entries = getDocumentEntries(applicationNumber);

        List<String> missing = new ArrayList<>();
        for (String code : formCodes) {
            boolean found = entries.stream().anyMatch(e -> matchesFormCode(e, code));
            if (!found) {
                missing.add(code);
            }
        }

        if (!missing.isEmpty()) {
            throw new AssertionError(
                    "USPTO API validation failed for application " + applicationNumber
                            + ": forms NOT found via API → " + missing
                            + " (total documents returned: " + entries.size() + ")");
        }

        Log.pass("USPTO API confirms forms {} present for application {}",
                List.of(formCodes), applicationNumber);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private JsonArray findDocumentArray(JsonObject root) {
        if (root.has("documentBag") && root.get("documentBag").isJsonArray()) {
            return root.getAsJsonArray("documentBag");
        }
        return null;
    }

    /**
     * A document's form type lives in "documentCode" as an exact code (e.g. "1449",
     * "892"), not as freeform text - so this is an exact, case-insensitive match on
     * that one field, not a substring scan across the whole entry (which would risk
     * false positives against unrelated fields like documentIdentifier or downloadUrl).
     */
    private boolean matchesFormCode(JsonObject entry, String formCode) {
        JsonElement code = entry.get("documentCode");
        return code != null && code.isJsonPrimitive()
                && code.getAsString().trim().equalsIgnoreCase(formCode.trim());
    }
}