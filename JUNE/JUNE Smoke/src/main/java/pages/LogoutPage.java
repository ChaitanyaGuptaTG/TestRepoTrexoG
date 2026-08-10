package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LogoutPage extends BasePage {
	// In this class we are identifying WebElements of LogoutPage
	// Author : Yash Shrivastava

	private final By profileIcon = By.xpath("//*[name()='svg' and @data-testid='PersonIcon']");
	private final By logoutOption = By.xpath("//span[normalize-space()='Logout']");

	public LogoutPage(WebDriver driver) {
		super(driver);
	}

	public void clickProfileIcon() {
		safeClick(profileIcon);
	}

	public void clickLogoutOption() {
		waitVisible(logoutOption);
		safeClick(logoutOption);
	}
}
