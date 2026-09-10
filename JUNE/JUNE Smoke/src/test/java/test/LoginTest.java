package test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pages.LoginPage;
import pages.DashboardPage;
import base.BaseTest;
import utils.Log;

public class LoginTest extends BaseTest {


    @Test(description = "Probe: submit with invalid password and inspect error", priority = 1)
    public void probeInvalidPassword() throws InterruptedException {
        LoginPage login = new LoginPage(getDriver());
        login.enterUsername(config.getProperty("username"));
        login.enterPassword("WrongPassword123!");
        login.clickSignin();
        Thread.sleep(3000);
        Log.info("URL after invalid password submit: " + getDriver().getCurrentUrl());
        try {
            String alertText = login.checkInvalidLogin();
            Log.info("Error alert text: [" + alertText + "]");
        } catch (Exception e) {
            Log.info("No role=alert element found: " + e.getMessage());
        }
    }

    @Test(description = "Probe: submit with empty fields and inspect validation",
            priority = 1, dependsOnMethods = "probeInvalidPassword", alwaysRun = true)
    public void probeEmptySubmit() throws InterruptedException {
        getDriver().navigate().refresh();
        Thread.sleep(2000);
        WebElement email = getDriver().findElement(By.xpath("//input[@name='email']"));
        WebElement submit = getDriver().findElement(By.xpath("//button[@type='submit']"));
        Log.info("Submit button disabled attr (empty fields): " + submit.getAttribute("disabled"));
        String emailRequired = email.getAttribute("required");
        String emailType = email.getAttribute("type");
        Log.info("Email field required=" + emailRequired + " type=" + emailType);
        submit.click();
        Thread.sleep(2000);
        Log.info("URL after empty submit click: " + getDriver().getCurrentUrl());
        Object validationMsg = ((JavascriptExecutor) getDriver())
                .executeScript("return arguments[0].validationMessage;", email);
        Log.info("Email native validationMessage: [" + validationMsg + "]");
        try {
            String alertText = new LoginPage(getDriver()).checkInvalidLogin();
            Log.info("Error alert text after empty submit: [" + alertText + "]");
        } catch (Exception e) {
            Log.info("No role=alert element found after empty submit: " + e.getMessage());
        }
    }

    @Test(description = "Probe: submit with malformed email and inspect validation",
            priority = 1, dependsOnMethods = "probeEmptySubmit", alwaysRun = true)
    public void probeInvalidEmail() throws InterruptedException {
        getDriver().navigate().refresh();
        Thread.sleep(2000);
        LoginPage login = new LoginPage(getDriver());
        login.enterUsername("not-an-email");
        login.enterPassword("whatever");
        login.clickSignin();
        Thread.sleep(2000);
        WebElement email = getDriver().findElement(By.xpath("//input[@name='email']"));
        Object validationMsg = ((JavascriptExecutor) getDriver())
                .executeScript("return arguments[0].validationMessage;", email);
        Log.info("Malformed email native validationMessage: [" + validationMsg + "]");
        Log.info("URL after malformed email submit: " + getDriver().getCurrentUrl());
    }

    @Test(description = "Negative login test case", priority = 2, dataProvider = "invalidLoginData")
    public void login_withInvalidCredentials_shouldShowError(String username, String password) {
        LoginPage login = new LoginPage(getDriver());
        login.enterUsername(username);
        login.enterPassword(password);
        login.clickSignin();
        Assert.assertEquals(login.checkInvalidLogin(), "Incorrect username or password.");
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