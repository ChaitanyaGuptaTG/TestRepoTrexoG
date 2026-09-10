package pages.IDSPage.helpers;

import api.clients.UsptoDocumentClient;
import org.testng.Assert;
import utils.Log;

import java.util.Map;


public class UsptoCountCrossChecker {

    private static final String FORM_1449 = "1449";
    private static final String FORM_892 = "892";
    private UsptoDocumentClient usptoApi;

    private UsptoDocumentClient usptoApi() {
        if (usptoApi == null) {
            usptoApi = UsptoDocumentClient.fromConfig();
        }
        return usptoApi;
    }
    public void assertCountsMatch(String appNum, int zip1449Count, int zip892Count) {
        Map<String, Integer> apiCounts = usptoApi().countDocumentsByType(appNum, FORM_1449, FORM_892);
        int api1449Count = apiCounts.get(FORM_1449);
        int api892Count = apiCounts.get(FORM_892);

        Log.info("Application {}: number of {} in zip is {} and {} in zip is {}",
                appNum, FORM_1449, zip1449Count, FORM_892, zip892Count);
        Log.info("Application {}: number of {} in API is {} and {} in API is {}",
                appNum, FORM_1449, api1449Count, FORM_892, api892Count);

        Assert.assertEquals(zip1449Count, api1449Count,
                "Application " + appNum + ": " + FORM_1449 + " PDF count mismatch - zip contained "
                        + zip1449Count + " but API reports " + api1449Count);
        Assert.assertEquals(zip892Count, api892Count,
                "Application " + appNum + ": " + FORM_892 + " PDF count mismatch - zip contained "
                        + zip892Count + " but API reports " + api892Count);

        Log.pass("Application {}: zip and API counts match - {}: {}, {}: {}",
                appNum, FORM_1449, zip1449Count, FORM_892, zip892Count);
    }
}
