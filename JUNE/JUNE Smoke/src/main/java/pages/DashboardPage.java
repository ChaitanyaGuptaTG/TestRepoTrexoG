package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {
	// In this class we are identifying weblements in Dashboard page.
	// Author : Yash Shrivastava

	public DashboardPage(WebDriver driver) {
		super(driver);
	}

	public void clickonDocgenImg() {
		// wait for entire docgen container to appear before clicking
		By docgenLocator = By.xpath("//img[@alt='JUNE']");
		waitVisible(docgenLocator); // wait until visible
		safeClick(docgenLocator);
	}

	public void clickonSettingImg() {
		By settingLocator = By.xpath("//*[local-name()='svg' and @data-testid='SettingsIcon']");
		safeClick(settingLocator);
	}

	public boolean inviteUserDisplayed() {
		By inviteLocator = By.xpath("//span[text()='Invite Users']");
		return waitVisible(inviteLocator).isDisplayed();
	}

	public boolean isHomeIconDisplayed() {
		By homeLocator = By.xpath("(//div[contains(@class,'MuiAvatar-root')])[1]");
		return waitVisible(homeLocator).isDisplayed();
	}

	public boolean isSettingIconDisplayed() {
		By locator = By.xpath("//*[name()='svg' and @data-testid='SettingsIcon']");
		return waitVisible(locator).isDisplayed();
	}

	public boolean isProfileIconDisplayed() {
		By locator = By.xpath("//*[name()='svg' and @data-testid='PersonIcon']");
		return waitVisible(locator).isDisplayed();
	}
}
