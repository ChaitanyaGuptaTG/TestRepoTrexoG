package test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import base.BaseTest;
import utils.Log;

public class ProbeTest extends BaseTest {

	@Test
	public void probeInvalidPassword() throws InterruptedException {
		WebElement email = getDriver().findElement(By.xpath("//input[@name='email']"));
		WebElement pass = getDriver().findElement(By.xpath("//input[@name='password']"));
		email.sendKeys(config.getProperty("username"));
		pass.sendKeys("WrongPassword123!");
		getDriver().findElement(By.xpath("//button[@type='submit']")).click();
		Thread.sleep(3000);
		Log.info("URL after invalid password submit: " + getDriver().getCurrentUrl());
		try {
			WebElement alert = getDriver().findElement(By.xpath("//div[@role='alert']/div[2]"));
			Log.info("Error alert text: [" + alert.getText() + "]");
		} catch (Exception e) {
			Log.info("No role=alert element found: " + e.getMessage());
		}
	}

	@Test(dependsOnMethods = "probeInvalidPassword", alwaysRun = true)
	public void probeEmptySubmit() throws InterruptedException {
		getDriver().navigate().refresh();
		Thread.sleep(2000);
		WebElement email = getDriver().findElement(By.xpath("//input[@name='email']"));
		WebElement pass = getDriver().findElement(By.xpath("//input[@name='password']"));
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
			WebElement alert = getDriver().findElement(By.xpath("//div[@role='alert']/div[2]"));
			Log.info("Error alert text after empty submit: [" + alert.getText() + "]");
		} catch (Exception e) {
			Log.info("No role=alert element found after empty submit: " + e.getMessage());
		}
	}

	@Test(dependsOnMethods = "probeEmptySubmit", alwaysRun = true)
	public void probeInvalidEmail() throws InterruptedException {
		getDriver().navigate().refresh();
		Thread.sleep(2000);
		WebElement email = getDriver().findElement(By.xpath("//input[@name='email']"));
		WebElement pass = getDriver().findElement(By.xpath("//input[@name='password']"));
		email.sendKeys("not-an-email");
		pass.sendKeys("whatever");
		WebElement submit = getDriver().findElement(By.xpath("//button[@type='submit']"));
		submit.click();
		Thread.sleep(2000);
		Object validationMsg = ((JavascriptExecutor) getDriver())
				.executeScript("return arguments[0].validationMessage;", email);
		Log.info("Malformed email native validationMessage: [" + validationMsg + "]");
		Log.info("URL after malformed email submit: " + getDriver().getCurrentUrl());
	}
}
