package pages.help.data;

public class BibliographicFAQData {

    public static final FAQEntry[] US_TRADEMARK = {
            new FAQEntry("What is 'US Trademark' feature",
                    "Q: What is 'US Trademark' feature in JUNE ?",
                    "US Trademark feature lets users retrieve bibliographic details for US Trademark"),
            new FAQEntry("How many application numbers",
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"),
            new FAQEntry("extract data for multiple jurisdictions",
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"),
            new FAQEntry("enter an invalid number",
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"),
            new FAQEntry("How quickly will I get the results",
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download")
    };

    public static final FAQEntry[] US_PATENT = {
            new FAQEntry("What is 'US Patents' feature",
                    "Q: What is 'US Patents' feature ?",
                    "US Patent feature lets users retrieve bibliographic details for US Patents"),
            new FAQEntry("How many application numbers",
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"),
            new FAQEntry("extract data for multiple jurisdictions",
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"),
            new FAQEntry("enter an invalid number",
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"),
            new FAQEntry("How quickly will I get the results",
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download")
    };

    public static final FAQEntry[] AU_TRADEMARK = {
            new FAQEntry("What is 'AU Trademark' feature",
                    "Q: What is 'AU Trademark' feature ?",
                    "AU Trademark feature lets users retrieve bibliographic details for Australian trademarks"),
            new FAQEntry("How many application numbers",
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"),
            new FAQEntry("extract data for multiple jurisdictions",
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"),
            new FAQEntry("enter an invalid number",
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"),
            new FAQEntry("How quickly will I get the results",
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download")
    };

    public static final FAQEntry[] EP_PATENT = {
            new FAQEntry("What is the EP Patents feature",
                    "Q1: What is the EP Patents feature?",
                    "EP Patent feature lets users retrieve bibliographic details for European Patent"),
            new FAQEntry("What input format is supported",
                    "Q2: What input format is supported?",
                    "EP publication numbers only, consisting of 7 digits and a kind code"),
            new FAQEntry("Can I extract data for multiple jurisdictions",
                    "Q3: Can I extract data for multiple jurisdictions at once?",
                    "No. You must select EP Patent and enter publication numbers"),
            new FAQEntry("How many publication numbers",
                    "Q4: How many publication numbers can I enter at once?",
                    "You can enter up to 500 publication numbers"),
            new FAQEntry("What happens if I enter an invalid number",
                    "Q5: What happens if I enter an invalid number?",
                    "JUNE displays an error message in the UI and sends an email notification"),
            new FAQEntry("How quickly will I get the results",
                    "Q6: How quickly will I get the results?",
                    "results are generated instantly and are available for download")
    };

    public static final FAQEntry[] AU_PATENT = {
            new FAQEntry("What is 'AU Patent' feature",
                    "Q: What is 'AU Patent' feature in JUNE ?",
                    "AU Patent feature lets users retrieve bibliographic details for Australian Patents"),
            new FAQEntry("How many application numbers",
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"),
            new FAQEntry("extract data for multiple jurisdictions",
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"),
            new FAQEntry("enter an invalid number",
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"),
            new FAQEntry("How quickly will I get the results",
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download")
    };

    public static final FAQEntry[] US_TM_IMAGE_DOWNLOAD = {
            new FAQEntry("What is the US Trademark Image Download",
                    "Q: What is the US Trademark Image Download module?",
                    "allows users to download trademark images associated with US trademark application numbers in bulk"),
            new FAQEntry("What image formats are supported",
                    "Q: What image formats are supported?",
                    "downloaded in either PNG or JPG format"),
            new FAQEntry("How do I download trademark images",
                    "Q: How do I download trademark images?",
                    "Enter one or more US trademark application numbers, select the desired image format"),
            new FAQEntry("How many images can I download",
                    "Q: How many images can I download at once?",
                    "You can download up to 500 trademark images in a single request"),
            new FAQEntry("download images for multiple application numbers",
                    "Q: Can I download images for multiple application numbers simultaneously?",
                    "module supports bulk processing, allowing you to enter multiple US trademark application numbers"),
            new FAQEntry("do not have associated trademark images",
                    "Q: What happens if some application numbers do not have associated trademark images?",
                    "download available images and notify you of any application numbers"),
            new FAQEntry("choose different formats for different images",
                    "Q: Can I choose different formats for different images in the same request?",
                    "single download request can be processed in only one selected format"),
            new FAQEntry("downloaded images provided in their original quality",
                    "Q: Are the downloaded images provided in their original quality?",
                    "retrieves and downloads the trademark images as available from the source records"),
            new FAQEntry("limit on the number of application numbers",
                    "Q: Is there a limit on the number of application numbers I can submit?",
                    "supports downloading up to 500 images per request")
    };

    public static final FAQEntry[] GLOBAL_PATENT = {
            new FAQEntry("What is Global Patent Feature",
                    "Q: What is Global Patent Feature?",
                    "retrieve bibliographic data for patent publications across multiple jurisdictions"),
            new FAQEntry("What input format is supported",
                    "Q: What input format is supported?",
                    "Enter one publication or patent number per line or separate multiple numbers using commas"),
            new FAQEntry("Which jurisdictions are supported",
                    "Q: Which jurisdictions are supported?",
                    "jurisdictions for which bibliographic data is available through the European Patent Office"),
            new FAQEntry("How many publication or patent numbers",
                    "Q: How many publication or patent numbers can I enter at once?",
                    "You can enter up to 500 publication or patent numbers in a single request"),
            new FAQEntry("publication numbers from different jurisdictions",
                    "Q: Can I enter publication numbers from different jurisdictions in the same request?",
                    "include publication or patent numbers from multiple jurisdictions in a single request"),
            new FAQEntry("invalid or unsupported patent number",
                    "Q: What happens if I enter an invalid or unsupported patent number?",
                    "JUNE displays an error message in the UI and sends an email notification"),
            new FAQEntry("How quickly will I get the results",
                    "Q: How quickly will I get the results?",
                    "results are generated instantly and are available for download"),
            new FAQEntry("mix publication numbers and patent numbers",
                    "Q: Can I mix publication numbers and patent numbers in the same request?",
                    "accepts both publication and patent numbers, provided they are prefixed")
    };

    private BibliographicFAQData() {
    }
}
