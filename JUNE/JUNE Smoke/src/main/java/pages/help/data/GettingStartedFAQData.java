package pages.help.data;

import org.openqa.selenium.By;

public class GettingStartedFAQData {

    public static final FAQEntry[] FAQS = {
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: What is JUNE ?']]"),
                    "Q: What is JUNE ?",
                    "Trexo product designed for paralegals to streamline IP-related workflows"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: How do I get started ?']]"),
                    "Q: How do I get started ?",
                    "Once your subscription is active, we will need your user details"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: Does JUNE support Single Sign-On (SSO) ?']]"),
                    "Q: Does JUNE support Single Sign-On (SSO) ?",
                    "Yes, JUNE supports SSO for secure access within your organization"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: What tasks can I perform using JUNE ?']]"),
                    "Q: What tasks can I perform using JUNE ?",
                    "JUNE simplifies IP workflows with the following capabilities"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: How do I start a task in JUNE ?']]"),
                    "Q: How do I start a task in JUNE ?",
                    "Either select an intent from the left menu or type its name"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: Can JUNE handle multiple simultaneous requests ?']]"),
                    "Q: Can JUNE handle multiple simultaneous requests ?",
                    "JUNE is built to process multiple requests concurrently"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: Can I view or download my task history ?']]"),
                    "Q: Can I view or download my task history ?",
                    "Track Task Screen allows you to view your task history for up to 7 days"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: How often does JUNE update its features ?']]"),
                    "Q: How often does JUNE update its features ?",
                    "JUNE provides regular updates, and all subscribers receive release notes by email"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: Why are some features disabled in my account ?']]"),
                    "Q: Why are some features disabled in my account ?",
                    "role-based restrictions set by your administrator or limitations of your subscription plan"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: Can I customize JUNE to fit my workflow ?']]"),
                    "Q: Can I customize JUNE to fit my workflow ?",
                    "JUNE includes prebuilt features for fast deployment"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: What data is available in JUNE for US applications ?']]"),
                    "Q: What data is available in JUNE for US applications ?",
                    "bibliographic data for US applications filed from 2000 onward"),
            new FAQEntry(By.xpath("//div[@role='button'][.//p[normalize-space()='Q: What happens if my request takes more than 30 seconds to process ?']]"),
                    "Q: What happens if my request takes more than 30 seconds to process ?",
                    "JUNE will display a notification so you can track its progress")
    };

    private GettingStartedFAQData() {
    }
}
