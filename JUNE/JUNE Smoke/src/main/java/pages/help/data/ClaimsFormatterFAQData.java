package pages.help.data;

import org.openqa.selenium.By;

public class ClaimsFormatterFAQData {

    public static final FAQEntry[] CLAIMS_FORMATTER = {
            new FAQEntry("What does the Claims Formatter do",
                    "Q: What does the Claims Formatter do?",
                    "The Claims Formatter standardizes claims by updating status identifiers, removing strikethrough and bracketed text, preserving clean claim language without underlines, and reporting any identified claim or text discrepancies directly in the output document."),
            new FAQEntry("How is Claims Formatter triggered in JUNE",
                    "Q: How is Claims Formatter triggered in JUNE?",
                    "Users can select Claims Formatter from the menu or enter prompts such as clean claims or claims formatting."),
            new FAQEntry("What type of file can be uploaded",
                    "Q: What type of file can be uploaded?",
                    "Only .docx claims documents are supported."),
            new FAQEntry("Why are my claims not showing up in the output",
                    "Q: Why are my claims not showing up in the output?",
                    "Claims Formatter identifies the claims section using standard claim headers"),
            new FAQEntry("Does it change the claim text",
                    "Q: Does it change the claim text?",
                    "No. The formatter does not rewrite or alter substantive claim language."),
            new FAQEntry("What observations are flagged",
                    "Q: What observations are flagged?",
                    "The formatter reports observations such as"),
            // Double quotes in question text — needs By constructor
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'Keyword mismatch') and contains(text(),'mean')]]"),
                    "Q: What does \"Keyword mismatch\" mean?",
                    "A keyword mismatch is reported when expected claim-structuring keywords or patterns used to determine claim relationships or structure are inconsistent or improperly used."),
            new FAQEntry("Are observations automatically fixed",
                    "Q: Are observations automatically fixed?",
                    "No. Observations are reported but not auto-corrected, allowing users to review and make informed edits."),
            new FAQEntry("Does it include the full OA Shell workflow",
                    "Q: Does it include the full OA Shell workflow?",
                    "No. Only the Clean Claims functionality is included. The broader OA Shell workflow is out of scope."),
            new FAQEntry("Can I upload a last-filed response draft",
                    "Q: Can I upload a last-filed response draft?",
                    "Yes. Claims Formatter supports both last-filed response drafts and claims-only .docx files."),
            new FAQEntry("What happens when I upload a last-filed response draft",
                    "Q: What happens when I upload a last-filed response draft?",
                    "When a last-filed response draft is uploaded, Claims Formatter extracts only the claims section and applies the claims formatting logic. The output is a new, claims-only document."),
            new FAQEntry("Are pharma claims files with chemical structures supported",
                    "Q: Are pharma claims files with chemical structures supported?",
                    "Claims files containing chemical structures or complex embedded objects (commonly found in pharma-related claims) may not always be processed successfully.")
    };

}