package pages.help.data;

import org.openqa.selenium.By;

public class IDSFAQData {

    public static final FAQEntry[] DOWNLOADER_1449_892 = {
            // & in question text needs By constructor to avoid XPath issues
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'1449') and contains(text(),'892') and contains(text(),'feature do')]]"),
                    "Q: What does '1449 & 892' feature do ?",
                    "This module allows you to download 1449 and 892 documents by simply entering a U.S. patent application number."),
            new FAQEntry("What are 1449 and 892 documents",
                    "Q: What are 1449 and 892 documents ?",
                    "1449 is the applicant's IDS listing prior art. 892 is the USPTO's form listing examiner-cited references"),
            new FAQEntry("download multiple forms (if exist) for a single application",
                    "Q: Can the system download multiple forms (if exist) for a single application ?",
                    "JUNE will download all of them"),
            new FAQEntry("How many applications can I enter at one time",
                    "Q: How many applications can I enter at one time ?",
                    "Up to 100 applications can be entered at once"),
            new FAQEntry("Can I enter a US patent or publication number",
                    "Q: Can I enter a US patent or publication number in JUNE ?",
                    "Only valid US application/serial numbers are accepted")
    };

    // Coming Soon
    public static final FAQEntry[] CORRESPONDING_REF_CHECK = {};

    public static final FAQEntry[] SB08_GENERATOR = {
            // First entry uses By constructor — double quotes in question text
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'SB-08 Generator') and contains(text(),'JUNE')]]"),
                    "Q: What is \"SB-08 Generator\" in JUNE ?",
                    "creates SB-08 forms using just an application number and a list of references"),
            new FAQEntry("What inputs are required to create a SB-08 form",
                    "Q: What inputs are required to create a SB-08 form ?",
                    "one valid application number and a list of patent references"),
            new FAQEntry("How many references can I enter in one request",
                    "Q: How many references can I enter in one request ?",
                    "You can enter up to 500 references for a single application number"),
            new FAQEntry("What happens if I skip the country code",
                    "Q: What happens if I skip the country code in my references ?",
                    "JUNE shows an error in the UI and sends an email"),
            new FAQEntry("Can I generate SB-08 form for multiple applications",
                    "Q: Can I generate SB-08 form for multiple applications at a time ?",
                    "system accepts only one application number per request"),
            new FAQEntry("number of references I enter exceeds the allowed limit",
                    "Q: What if the number of references I enter exceeds the allowed limit per form ?",
                    "system automatically creates additional forms"),
            new FAQEntry("What information is automatically filled in SB-08",
                    "Q: What information is automatically filled in SB-08 form ?",
                    "auto-fills the bibliographic header and reference details for US and foreign patents"),
            new FAQEntry("What is included in the output",
                    "Q: What is included in the output ?",
                    "Completed SB-08 form(s) in USPTO-compliant PDF format"),
            new FAQEntry("SB-08 form Editable after Generation",
                    "Q: Is SB-08 form Editable after Generation ?",
                    "SB-08 form can still be edited using software that supports the XFA PDF format")
    };

    public static final FAQEntry[] REMOVE_EMBEDDED_FONTS = {
            // First entry uses By constructor — double quotes in question text
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'Remove Embedded Fonts') and contains(text(),'feature do')]]"),
                    "Q: What does \"Remove Embedded Fonts\" feature do?",
                    "removes embedded fonts from uploaded PDFs or ZIPs, ensuring USPTO compliance"),
            new FAQEntry("What file types are supported",
                    "Q: What file types are supported ?",
                    "PDF files or ZIP files containing multiple PDFs"),
            new FAQEntry("What are the file size and upload limits",
                    "Q: What are the file size and upload limits ?",
                    "files up to 100 MB in size, with a maximum of 15 files per batch and up to 500 pages per file"),
            new FAQEntry("Will the content or formatting of my file change",
                    "Q: Will the content or formatting of my file change ?",
                    "Only the embedded fonts are removed. The document's content and formatting remain intact"),
            new FAQEntry("What happens if my file exceeds the limits",
                    "Q: What happens if my file exceeds the limits ?",
                    "system will notify you. You may need to split your document into smaller files")
    };

    public static final FAQEntry[] REFERENCE_EXTRACTOR = {
            new FAQEntry("What does the Reference Extractor module do",
                    "Q: What does the Reference Extractor module do ?",
                    "scans uploaded documents, identifies patent and non-patent literature (NPL) references"),
            new FAQEntry("Which file formats are supported",
                    "Q: Which file formats are supported ?",
                    "PDF, DOC, and DOCX"),
            new FAQEntry("Can I upload multiple documents",
                    "Q: Can I upload multiple documents ?",
                    "Yes, up to a combined size of 25 MB"),
            new FAQEntry("What do the highlight colors mean",
                    "Q: What do the highlight colors mean ?",
                    "Patent and NPL references = Yellow"),
            new FAQEntry("Does the module provide a full list of references",
                    "Q: Does the module provide a full list of references ?",
                    "PDF outputs include bookmarks listing references, their locations, and clickable links"),
            new FAQEntry("Why is my output sometimes PDF and sometimes Word",
                    "Q: Why is my output sometimes PDF and sometimes Word ?",
                    "output keeps the same format as the uploaded file"),
            new FAQEntry("Why do not I see bookmarks in my Word file",
                    "Q: Why do not I see bookmarks in my Word file ?",
                    "Bookmarking is currently supported only for PDFs")
    };

    public static final FAQEntry[] REFERENCE_DOWNLOADER = {
            // ── General Overview ──
            new FAQEntry("What is the Reference Downloader module",
                    "Q: What is the Reference Downloader module?",
                    "high-speed retrieval tool in JUNE that allows users to bulk-download patent references"),
            new FAQEntry("Can I use this feature to download references from any country",
                    "Q: Can I use this feature to download references from any country?",
                    "supports patent document retrieval from all countries globally"),
            new FAQEntry("How do I format my search request",
                    "Q: How do I format my search request?",
                    "Country Code+Publication Number+Kind Code"),
            new FAQEntry("Is there a limit on how many I can download",
                    "Q: Is there a limit on how many I can download?",
                    "You can submit up to 100 references per request"),

            // ── Automation & Delivery ──
            new FAQEntry("How long does it take to get my references",
                    "Q: How long does it take to get my references?",
                    "If references are already in the JUNE library, delivery is immediate.\n" +
                            "If the document must be retrieved from external sources or requires additional processing (like foreign publications), the system follows automated service windows with real-time status updates."),
            new FAQEntry("How are delivery timelines determined",
                    "Q: How are delivery timelines determined for my request?",
                    "Delivery timelines are automatically determined based on the number of references submitted and the time of request. The standard service windows are as follows:\n" +
                            "Up to 25 references per request:\n" +
                            "a) Submitted before 12 PM (EST): ~4 hours\n" +
                            "b) Submitted after 12 PM (EST): ~16 hours\n" +
                            "26+ references per request:\n" +
                            "Submitted at any time: ~18 hours"),
            new FAQEntry("status mean",
                    "Q: What does an 'SLA Update' or 'Delayed' status mean?",
                    "This indicates the system is processing a complex request that requires more time due to large file sizes, high system load, or the need to retrieve documents from external patent office databases."),
            new FAQEntry("Are the files ready for the USPTO",
                    "Q: Are the files ready for the USPTO?",
                    "Yes. Documents are automatically processed to ensure USPTO compatibility, including embedded font removal and file size optimization (maximum 25 MB per document)."),

            // ── English vs Foreign Reference Handling ──
            new FAQEntry("How are English and non-English references handled",
                    "Q: How are English and non-English references handled?",
                    "JUNE prepares the reference in one of the following formats"),
            new FAQEntry("What is included in the download package",
                    "Q: What is included in the download package?",
                    "Reference PDFs ready for USPTO submission"),
            new FAQEntry("What specific data is in the metadata file",
                    "Q: What specific data is in the metadata file?",
                    "reference numbers, publication dates, applicant and inventor names"),
            new FAQEntry("Why is some information (like names) in a foreign language",
                    "Q: Why is some information (like names) in a foreign language?",
                    "system pulls data exactly as it appears at the original source"),

            // ── Interpreting the Observations Report ──
            new FAQEntry("How do I fix an \"Invalid format\" error",
                    "Q: How do I fix an \"Invalid format\" error in the report?",
                    "Ensure each reference follows the required format: Country Code+Pub/Pat Number+Kind Code"),
            new FAQEntry("reference is marked as \"Not Found\"",
                    "Q: What if a reference is marked as \"Not Found\" in the report?",
                    "specific information or the PDF could not be retrieved from the primary source databases"),
            new FAQEntry("Abstract- Not applicable\" mean the download failed",
                    "Q: Does \"Abstract- Not applicable\" mean the download failed?",
                    "simply means that for that specific jurisdiction or publication type, an abstract is not a formal requirement"),

            // ── Troubleshooting ──
            new FAQEntry("How do I know if a reference failed",
                    "Q: How do I know if a reference failed?",
                    "JUNE provides a Processing Observations Report"),
            new FAQEntry("Who do I contact for technical support",
                    "Q: Who do I contact for technical support?",
                    "product@trexoglobal.com"),
            new FAQEntry("What information should I provide for support",
                    "Q: What information should I provide for support?",
                    "Request ID, the reference numbers used, a description of the issue")
    };

    public static final FAQEntry[] REFERENCE_COUNT = {
            new FAQEntry("What does Reference Count module do",
                    "Q: What does Reference Count module do ?",
                    "The Reference Count feature provides a quick summary of the total number of references cited in an IDS (Information Disclosure Statement). It helps users track and manage references across multiple submissions efficiently."),
            new FAQEntry("Why might the reference count appear incorrect for some application numbers",
                    "Q: Why might the reference count appear incorrect for some application numbers ?",
                    "JUNE generates counts based on the XML version of IDS filings available on official patent websites. If the XML data is incomplete or contains errors, those issues will also appear in the Excel output. If you notice discrepancies, please contact our product support team."),
            // Double quotes in question text — needs By constructor
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'Failed to fetch data') and contains(text(),'error')]]"),
                    "Q: Why do I see a \"Failed to fetch data\" error for an application number ?",
                    "This error usually occurs when:\n" +
                            "• No IDS documents are found for the given application number, or\n" +
                            "• The available IDS PDFs are corrupted or unreadable."),
            new FAQEntry("Can I check reference counts for multiple applications at once",
                    "Q: Can I check reference counts for multiple applications at once ?",
                    "Yes. The Reference Count feature supports bulk input for up to 100 application numbers per request.")
    };

}