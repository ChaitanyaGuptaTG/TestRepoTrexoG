package pages.help.data;

import org.openqa.selenium.By;

public class DocumentGenerationFAQData {

    public static final FAQEntry[] CHANGE_CORR_APP = {
            new FAQEntry("Change of Correspondence Address (Application)",
                    "Q: What is the \"Change of Correspondence Address (Application)\" feature used for?",
                    "generate correspondence address change forms for US patent applications"),
            new FAQEntry("What inputs are required for generating these forms",
                    "Q: What inputs are required for generating these forms?",
                    "application number(s), customer number, attorney name, and registration number"),
            new FAQEntry("In which format are the forms downloaded",
                    "Q: In which format are the forms downloaded?",
                    "Forms are generated in PDF format and can be edited using any standard PDF editor"),
            new FAQEntry("Where can I download the generated forms",
                    "Q: Where can I download the generated forms?",
                    "downloaded from the JUNE interface, or via an email link")
    };

    public static final FAQEntry[] RCE_TRANSMITTAL = {
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'RCE Transmittal') and contains(text(),'feature used for')]]"),
                    "Q: What is the \"RCE Transmittal\" feature used for ?",
                    "generate Request for Continued Examination (RCE) transmittal forms"),
            new FAQEntry("inputs are required for generating RCE Transmittal",
                    "Q: What inputs are required for generating RCE Transmittal forms ?",
                    "application number(s), attorney name, and registration number"),
            new FAQEntry("In which format are the forms downloaded",
                    "Q: In which format are the forms downloaded ?",
                    "Forms are generated in PDF format and can be edited using any standard PDF editor"),
            new FAQEntry("Where can I download the generated forms",
                    "Q: Where can I download the generated forms ?",
                    "downloaded from the JUNE interface, or via an email link")
    };

    public static final FAQEntry[] POA_GPOA = {
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'POA') and contains(text(),'feature used for')]]"),
                    "Q: What is the \"POA\" feature used for ?",
                    "generate Power of Attorney forms quickly and efficiently"),
            new FAQEntry("inputs are required for generating POA forms",
                    "Q: What inputs are required for generating POA forms ?",
                    "application number(s), attorney name, and registration number"),
            new FAQEntry("In which format are the forms downloaded",
                    "Q: In which format are the forms downloaded ?",
                    "All POA forms are downloaded in PDF format"),
            new FAQEntry("Where can I download the generated POA forms",
                    "Q: Where can I download the generated POA forms ?",
                    "downloaded directly from the JUNE interface"),
            new FAQEntry("GPOA option work while generating POA",
                    "Q: How does the GPOA option work while generating POA forms ?",
                    "upload a GPOA while generating a POA, and it will be merged"),
            new FAQEntry("POA and 37 CFR be merged",
                    "Q: Can POA and 37 CFR be merged for an application number ?",
                    "both documents are generated together in a single PD")
    };

    public static final FAQEntry[] CHANGE_CORR_PATENT = {
            new FAQEntry("Change of Correspondence Address (Patent)",
                    "Q: What is the \"Change of Correspondence Address (Patent)\" feature used for ?",
                    "generate correspondence address change forms specific to issued patents"),
            new FAQEntry("What inputs are required for generating these forms",
                    "Q: What inputs are required for generating these forms ?",
                    "patent number(s), customer number, attorney name, and registration number"),
            new FAQEntry("In which format are the forms downloaded",
                    "Q: In which format are the forms downloaded ?",
                    "Forms are generated in PDF format and can be edited using any standard PDF editor"),
            new FAQEntry("Where can I download the generated forms",
                    "Q: Where can I download the generated forms ?",
                    "downloaded from the JUNE interface, or via an email link")
    };

    public static final FAQEntry[] RCE_EFS = {
            new FAQEntry("RCE Transmittal EFS (SB-30)",
                    "Q: What is the \"RCE Transmittal EFS (SB-30)\" feature used for?",
                    "generate Request for Continued Examination (RCE) transmittal forms in bulk"),
            new FAQEntry("inputs are required for generating SB-30",
                    "Q: What inputs are required for generating SB-30 forms?",
                    "provide one or more U.S. application numbers"),
            new FAQEntry("How many application numbers can be entered",
                    "Q: How many application numbers can be entered at once?",
                    "You can enter up to 500 application numbers at a time"),
            new FAQEntry("generate forms for multiple applications",
                    "Q: Can I generate forms for multiple applications at once?",
                    "enter multiple application numbers separated by commas or paste a list"),
            new FAQEntry("Are all fields mandatory",
                    "Q: Are all fields mandatory while configuring the form?",
                    "most fields are optional. However, attorney or applicant name is mandatory"),
            new FAQEntry("include an attorney signature",
                    "Q: Can I include an attorney signature in the generated forms?",
                    "include the attorney signature by selecting the signature checkbox"),
            new FAQEntry("In which format are the forms downloaded",
                    "Q: In which format are the forms downloaded?",
                    "generated in PDF format and provided as a ZIP file"),
            new FAQEntry("Where can I download the generated forms",
                    "Q: Where can I download the generated forms?",
                    "downloaded directly from the JUNE interface via a downloadable link")
    };
    public static final FAQEntry[] CFR_37 = {
            // First entry uses By constructor — double quotes in text cause XPath conflict
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'37 CFR') and contains(text(),'feature used for')]]"),
                    "Q: What is \"37 CFR\" feature used for ?",
                    "generate 37 CFR forms. It supports batch preparation for up to 500 application numbers"),
            new FAQEntry("What inputs are required for generating 37 CFR forms",
                    "Q: What inputs are required for generating 37 CFR forms ?",
                    "application number(s), attorney name, and registration number"),
            new FAQEntry("Power of Attorney (POA) and 37 CFR 3.73 documents be merged",
                    "Q: Can the Power of Attorney (POA) and 37 CFR 3.73 documents be merged for an application number ?",
                    "JUNE provides the option to generate POA and 3.73 documents together"),
            new FAQEntry("In which format are the forms downloaded",
                    "Q: In which format are the forms downloaded ?",
                    "forms are downloaded in PDF format"),
            new FAQEntry("Where can I download the generated forms",
                    "Q: Where can I download the generated forms ?",
                    "available both in the JUNE interface and via email once processing is complete"),
            new FAQEntry("multiple 37 CFR forms for a single application number",
                    "Q: What happens if I have multiple 37 CFR forms for a single application number ?",
                    "system will generate separate PDF forms for each request"),
            new FAQEntry("chain of title information is included",
                    "Q: What chain of title information is included in the 37 CFR form ?",
                    "The chain of title in the 37 CFR form includes only the Assignment of Assignors’ Interest information. Other assignment details are not captured in this version of the form."),
            new FAQEntry("applicant information populated in the 37 CFR form",
                    "Q: How is applicant information populated in the 37 CFR form ?",
                    "Applicant information in the 37 CFR form is pulled directly from USPTO records"),
            new FAQEntry("Can POA and 37 CFR be merged",
                    "Q: Can POA and 37 CFR be merged for an application number ?",
                    "both documents are generated together in a single PD")
    };

    // Coming Soon — no FAQ content published yet
    public static final FAQEntry[] POA_REVOCATION = {};
    public static final FAQEntry[] ATTORNEY_WITHDRAWAL = {};
    public static final FAQEntry[] CORRECT_APPLICANT_NAME = {};
    public static final FAQEntry[] CFR = {};

    private DocumentGenerationFAQData() {
    }
}
