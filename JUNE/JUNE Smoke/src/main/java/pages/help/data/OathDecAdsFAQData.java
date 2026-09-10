package pages.help.data;

import org.openqa.selenium.By;

public class OathDecAdsFAQData {

    public static final FAQEntry[] OATH_DEC_ADS_DOWNLOADER = {
            // & in question text needs By constructor to avoid XPath issues
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'Oath/Dec') and contains(text(),'ADS Downloader') and contains(text(),'do')]]"),
                    "What does the 'Oath/Dec & ADS Downloader' do ?",
                    "This feature allows you to instantly download filled Oath/Declaration and Application Data Sheet (ADS) documents."),
            new FAQEntry("What inputs are required to use this feature",
                    "What inputs are required to use this feature ?",
                    "You only need to provide a valid application number. The system will fetch the relevant documents automatically."),
            new FAQEntry("What is the output of this feature",
                    "What is the output of this feature ?",
                    "The Oath/Dec and ADS documents in PDF format, along with an Excel report (.xlsx) containing document availability and inventor details."),
            new FAQEntry("What if some documents are not available",
                    "What if some documents are not available ?",
                    "If either the Oath/Dec or ADS is missing for the given application number, JUNE will still generate the Excel report, clearly indicating which documents are available and which are not."),
            new FAQEntry("Can I download documents in bulk",
                    "Can I download documents in bulk ?",
                    "Yes. The Oath/Dec & ADS Downloader supports bulk downloads for up to 10 application numbers in a single request.")
    };

}