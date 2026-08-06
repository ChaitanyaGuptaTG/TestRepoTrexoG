package test;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;
import pages.LogoutPage;
import pages.LoginPage;
import utils.WaitUtils;

public class LogoutTest extends BaseTest {

	@Test(priority = 9)
	public void testLogout() {
		LoginPage loginPage = new LoginPage(getDriver());
		loginPage.enterUsername(config.getProperty("username"));
		loginPage.enterPassword(config.getProperty("password"));
		loginPage.clickSignin();
		WaitUtils.waitForPageToLoadCompletely(getDriver(), 10);

		LogoutPage logoutPage = new LogoutPage(getDriver());
		logoutPage.clickProfileIcon();
		logoutPage.clickLogoutOption();

		Assert.assertTrue(loginPage.isSignInButtonVisible(),
				"Sign-in submit button should be visible on login page after successful logout.");

//		org.openqa.selenium.WebElement submitBtn = new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(20))
//				.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@type='submit']")));
//		Assert.assertTrue(submitBtn.isDisplayed(), "Sign-in submit button should be visible on login page after successful logout.");
	}
}
