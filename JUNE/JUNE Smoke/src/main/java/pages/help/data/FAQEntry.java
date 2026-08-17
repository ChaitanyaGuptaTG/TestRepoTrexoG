package pages.help.data;

import org.openqa.selenium.By;

public class FAQEntry {

    private final By locator;
    private final String question;
    private final String expectedAnswer;

    public FAQEntry(String xpathContainsText, String question, String expectedAnswer) {
        String xpath = xpathContainsText.contains("\"")
                ? "//div[@role='button'][.//p[contains(text(),'" + xpathContainsText + "')]]"
                : "//div[@role='button'][.//p[contains(text(),\"" + xpathContainsText + "\")]]";
        this.locator = By.xpath(xpath);
        this.question = question;
        this.expectedAnswer = expectedAnswer;
    }

    public FAQEntry(By locator, String question, String expectedAnswer) {
        this.locator = locator;
        this.question = question;
        this.expectedAnswer = expectedAnswer;
    }

    public By getLocator() {
        return locator;
    }

    public String getQuestion() {
        return question;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }
}
