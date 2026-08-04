package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LogoutPage extends BasePage {
	// In this class we are identifying WebElements of LogoutPage
	// Author : Yash Shrivastava

	public LogoutPage(WebDriver driver) {
		super(driver);
	}

	public void clickProfileIcon() {
		By profileIcon = By.xpath("//*[name()='svg' and @data-testid='PersonIcon']");
		safeClick(profileIcon);
	}

	public void clickLogoutOption() {
		By logoutLocator = By.xpath("//div//span[text()='Logout']");
		waitVisible(logoutLocator);
		safeClick(logoutLocator);
	}
}
