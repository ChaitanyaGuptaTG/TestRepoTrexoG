package test;

import jdk.jfr.Description;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

// import com.test.automation.base.BaseTest;
// import com.test.automation.pages.DashboardPage;
// import com.test.automation.pages.LoginPage;

import pages.LoginPage;
import pages.DashboardPage;
import base.BaseTest;
import utils.ScreenshotUtil;

import javax.management.Descriptor;

//Verify User is able to login into the application using credentials.
//Author : Yash Shrivastava
public class LoginTest extends BaseTest {


    @Test(description = "Negative login test case", priority = 2, dataProvider = "invalidLoginData")
    public void login_withInvalidCredentials_shouldShowError(String username, String password) {

        LoginPage login = new LoginPage(getDriver());

        login.enterUsername(username);
        login.enterPassword(password);
        login.clickSignin();

        Assert.assertEquals(login.checkInvalidLogin(), "Incorrect username or password.");
        ScreenshotUtil.captureScreenshot(getDriver(), "InvalidLogin");
    }

    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() {
        return new Object[][]{
                {"Treco.global@trexoglobal.com", "test@123"}
        };
    }

    @Test(priority = 3)
    public void testLogin() {
        LoginPage login = new LoginPage(getDriver());
        login.enterUsername(config.getProperty("username"));
        login.enterPassword(config.getProperty("password"));
        login.clickSignin();
        DashboardPage dash = new DashboardPage(getDriver());
        Boolean check = dash.isHomeIconDisplayed();
        Assert.assertTrue(check, "Dashboard home avatar icon should be displayed after successful login.");
    }

}
