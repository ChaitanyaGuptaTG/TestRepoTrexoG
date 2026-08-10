package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {
    // In this class we are identifying WebElements of LoginPage
    // Author : Yash Shrivastava

    //XPaths :
    private By invalidLoginMessage = By.xpath("//div[contains(text(),'Incorrect credentials')]");
    private By emailIsRequiredMessage = By.xpath("//*[@id='mui-20-helper-text']");
    private By passwordRequiredMessage = By.cssSelector("#mui-21-helper-text");

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

    public boolean isEmailRequiredMessageVisible() {
        return wait.until(
                        ExpectedConditions.visibilityOfElementLocated(emailIsRequiredMessage))
                .isDisplayed();
    }

    public boolean isPasswordRequiredMessageVisible() {
        return wait.until(
                        ExpectedConditions.visibilityOfElementLocated(passwordRequiredMessage))
                .isDisplayed();
    }

    public boolean isSignInButtonVisible() {
        By submitBtn = By.xpath("//button[@type='submit']");
        try {
            return new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(submitBtn))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

}
