package pages.help.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReleaseNotesData {

    private static final LinkedHashMap<String, String[]> CONTENT = new LinkedHashMap<>();

    static {
        CONTENT.put("Release 11.0", new String[]{
                "Introducing the latest JUNE update with enhancements designed to improve flexibility and usability",
                "Patent Image File Wrapper Downloader",
                "greater flexibility when downloading prosecution documents",
                "Dual Download Options", "Collated File",
                "complete prosecution history with bookmarks",
                "Individual Files", "Each prosecution document is downloaded as a separate file",
                "Descriptive File Naming", "Application Number", "Mailing Date", "Prosecution Event"
        });
        CONTENT.put("Release 10.0", new String[]{
                "Introducing the latest JUNE update with new features and enhancements",
                "Global Patent Bibliographic Search",
                "Search patent or publication numbers across multiple jurisdictions",
                "bibliographic data directly from the EPO", "AppGen Module",
                "Customize document titles for CIP, 371, and Bypass Continuation",
                "SB-08 Generator", "supported reference limit to 4,000 references",
                "1,000 references for both DOCX and PDF outputs", "Embedded Font Removal",
                "USPTO-compatible PDFs with reduced file size", "OA Shell",
                "automatically retrieves and extracts claims",
                "eliminating the need to upload a separate claims document"
        });
        CONTENT.put("Release 9.0", new String[]{
                "Introducing trademark image retrieval capability within Bibliographic Data Extraction",
                "US Trademark Image Download", "now available under Bibliographic Data Extraction",
                "Download US trademark images in bulk",
                "Submit one or more US trademark application numbers",
                "Download images in PNG or JPG format",
                "Process up to 500 trademark images per request",
                "faster and more efficient way to retrieve trademark image assets",
                "Refer to the FAQ and Tutorial sections"
        });
        CONTENT.put("Release 8.0", new String[]{
                "Introducing new form support and enhancements to reference-related workflows",
                "RCE Transmittal EFS (SB-30) Form",
                "Patent Center-compatible version of the SB-30 form",
                "select, generate, and download the SB-30 form", "SB-08 Generator",
                "Excel file containing a structured list of all reference numbers",
                "easier tracking, review, and downstream processing",
                "1449 and 892 Downloader", "access to recently filed SB-08 documents",
                "not yet have been reviewed or processed by the examiner",
                "Refer to the FAQ and Tutorial sections"
        });
        CONTENT.put("Release 7.0", new String[]{
                "Introducing new automation capabilities for reference retrieval and document generation",
                "Reference Downloader", "Retrieve multiple patent references through a single request",
                "USPTO-ready output package", "Bulk reference retrieval",
                "Built-in validation checks", "Delivery status tracking",
                "Support for English and non-English references", "37 CFR 1.46",
                "Correct or Update Applicant Name",
                "generate the 1.46 form directly from the Document Generation module",
                "POA/GPOA and 37 CFR 3.73(c) workflows",
                "Refer to the FAQ and Tutorial sections"
        });
        CONTENT.put("Release 6.0", new String[]{
                "enhancements to the Reference Extractor",
                "improving extraction accuracy and reporting capabilities", "Reference Extractor",
                "Improved extraction accuracy for both patent and non-patent literature",
                "structured Excel output with separate Patent and NPL reference columns",
                "identified page numbers", "probable NPL publication titles",
                "Refer to the FAQ and Tutorial sections"
        });
        CONTENT.put("Release 5.0", new String[]{
                "Introducing new features and module enhancements across JUNE",
                "Patent File Wrapper Downloader",
                "Download the complete prosecution history for U.S. patents",
                "single, collated Image File Wrapper", "EP Bibliographic Data Extraction",
                "bibliographic data extraction for European Patent (EP) publications",
                "Claims Formatter", "clean and standardize claims by updating identifiers",
                "removing unwanted formatting", "generating observation reports",
                "SB-08 Generator", "now supports DOCX output", "AppGen Module",
                "Preliminary Amendment documents for 371 filing packages",
                "Initial IDS documents can now be auto-generated",
                "option to enable or disable IDS generation",
                "Specifications, Drawings, Power of Attorney",
                "Refer to the FAQ and Tutorial sections"
        });
        CONTENT.put("Release 4.0", new String[]{
                "Introducing new forms and module enhancements across JUNE", "AIA-83",
                "Request for Withdrawal as Attorney or Agent", "AIA-81A",
                "Power of Attorney (PoA) or Revocation of PoA",
                "generate the above forms by simply following the system prompts",
                "Bibliographic Data Extraction (US Patent)", "Entity Size",
                "Office Action Mailing Date"
        });
        CONTENT.put("Release 3.0", new String[]{
                "streamline workflows and improve usability across the Trexo Platform",
                "Help Section", "access support and learning resources directly within JUNE",
                "Feedback", "Share suggestions or report issues", "Release Notes",
                "Stay informed about the latest changes", "FAQs",
                "Searchable, feature-specific answers", "Tutorials",
                "Watch short video walkthroughs", "OA Shell Draft",
                "Office Action response drafts by simply entering the application number",
                "auto-retrieves the necessary documents", "AppGen",
                "Bypass Continuation Filing", "Non-English Filing Package",
                "Refer to the FAQ and Tutorial sections"
        });
        CONTENT.put("Release 2.0", new String[]{
                "Introducing new forms and workflow enhancements", "Document Generation module",
                "Change of Correspondence Address", "AIA-122", "AIA-123",
                "RCE Transmittal (SB-30)", "POA + 37 CFR",
                "merged copy of POA and 37 CFR in a single PDF",
                "Refer to the FAQ and Tutorial sections"
        });
        CONTENT.put("Release 1.0", new String[]{
                "Introducing the new 1449 and 892 Downloader in JUNE",
                "simplify and accelerate IDS preparation", "1449 and 892 Downloader",
                "1449 (applicant-cited) and 892 (examiner-cited) references",
                "one or multiple U.S. application numbers",
                "fetches all documents in one go", "organizes them by mailing date",
                "Refer to the FAQ and Tutorial sections"
        });
    }

    private ReleaseNotesData() {
    }

    public static Map<String, String[]> getAll() {
        return CONTENT;
    }

    public static String[] getTexts(String releaseTitle) {
        return CONTENT.get(releaseTitle);
    }

    public static boolean hasContent(String releaseTitle) {
        String[] texts = CONTENT.get(releaseTitle);
        return texts != null && texts.length > 0;
    }
}
