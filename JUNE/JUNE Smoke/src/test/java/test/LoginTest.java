package test;

import org.testng.Assert;
import org.testng.annotations.Test;

// import com.test.automation.base.BaseTest;
// import com.test.automation.pages.DashboardPage;
// import com.test.automation.pages.LoginPage;

import pages.LoginPage;
import pages.DashboardPage;
import base.BaseTest;

//Verify User is able to login into the application using credentials.
//Author : Yash Shrivastava
public class LoginTest extends BaseTest {

	@Test(priority = 1)
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
