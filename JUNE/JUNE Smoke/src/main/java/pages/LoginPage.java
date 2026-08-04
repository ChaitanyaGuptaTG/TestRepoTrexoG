package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {
	// In this class we are identifying WebElements of LoginPage
	// Author : Yash Shrivastava

	private By emailField = By.xpath("//input[@name='email']");

	public LoginPage(WebDriver driver) {
		super(driver);
	}

	public void enterUsername(String username) {
		safeType(emailField, username);
	}

	public void enterPassword(String password) {
		By locator = By.xpath("//input[@name='password']");
		safeType(locator, password);
	}

	public void clickSignin() {
		safeClick(By.xpath("//button[@type='submit']"));
	}

	public String checkInvalidLogin() {
		By locator = By.xpath("//div[@role='alert']/div[2]");
		WebElement element = waitVisible(locator);
		return element.getText();
	}
}
