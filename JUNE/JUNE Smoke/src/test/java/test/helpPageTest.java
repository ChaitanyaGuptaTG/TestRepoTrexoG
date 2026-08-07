package test;

import org.testng.Assert;
import org.testng.annotations.Test;
import base.BaseTest;

import pages.LoginPage;
import pages.helpPage;
import utils.WaitUtils;

public class helpPageTest extends BaseTest {


    @Test(priority = 10)
    public void testHelpPage() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(config.getProperty("username"));
        loginPage.enterPassword(config.getProperty("password"));
        loginPage.clickSignin();
        WaitUtils.waitForPageToLoadCompletely(getDriver(), 10);
        helpPage helpPage = new helpPage(getDriver());
        helpPage.clickJuneIcon();
        helpPage.clickHelpIcon();
        helpPage.checkElementsOfHelpIcon();

    }
}