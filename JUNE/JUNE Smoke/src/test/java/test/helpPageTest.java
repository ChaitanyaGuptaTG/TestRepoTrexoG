package test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import base.BaseTest;
import pages.LoginPage;
import pages.helpPage;
import utils.WaitUtils;

public class helpPageTest extends BaseTest {

    private helpPage helpPage;

    @BeforeClass
    public void setUpHelpPage() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(config.getProperty("username"));
        loginPage.enterPassword(config.getProperty("password"));
        loginPage.clickSignin();
        WaitUtils.waitForPageToLoadCompletely(getDriver(), 10);
        helpPage = new helpPage(getDriver());
    }

    @Test(priority = 1, description = "Verify all Help menu options are displayed")
    public void testHelpPage() {
        System.out.println("Starting test case 1 for helpPage");
        helpPage.clickJuneIcon();
        helpPage.clickHelpIcon();
        helpPage.checkElementsOfHelpIcon();
    }

    @Test(priority = 2, description = "Verify Feedback form opens and can be cancelled")
    public void testFeedbackPage() {
        System.out.println("Starting test case 2 for feedback page");
        helpPage.clickHelpIcon();
        helpPage.clickFeedback();
    }

    @Test(priority = 3, description = "Verify Release Notes with month mapping")
    public void testReleaseNote() {
        System.out.println("Starting test case 3 for Release Note");
        helpPage.clickHelpIcon();
        helpPage.clickReleaseNotes();
        helpPage.clickHelpIcon();
        helpPage.searchReleaseNotes();
    }
}