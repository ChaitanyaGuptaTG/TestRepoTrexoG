package pages.help.data;

import org.openqa.selenium.By;

public class PatentFileWrapperDownloaderData {

    public static final FAQEntry[] PATENT_FILE_WRAPPER_DOWNLOADER = {
            // Double quotes in question text — needs By constructor
            new FAQEntry(
                    By.xpath("//div[@role='button'][.//p[contains(text(),'Patent File Wrapper Downloader') and contains(text(),'do')]]"),
                    "Q: What does \"Patent File Wrapper Downloader\" do?",
                    "It downloads the entire prosecution history available at the USPTO for a given application."),
            new FAQEntry("What download options are available in the Patent File Wrapper Downloader",
                    "Q: What download options are available in the Patent File Wrapper Downloader?",
                    "The Patent File Wrapper Downloader provides two download options:\n" +
                            "Collated File\n" +
                            "Downloads the complete prosecution history as a single PDF.\n" +
                            "Includes bookmarks for easy navigation between prosecution events.\n" +
                            "Best suited for users who prefer reviewing the entire file wrapper in one document.\n" +
                            "Individual Files\n" +
                            "Downloads each prosecution document as a separate file.\n" +
                            "Each file is automatically named for easy identification.\n" +
                            "Ideal for document management, sharing, and selective review."),
            new FAQEntry("How many applications can I enter at once",
                    "Q: How many applications can I enter at once?",
                    "Up to 10 applications."),
            new FAQEntry("Can I search using a publication or patent number",
                    "Q: Can I search using a publication or patent number?",
                    "No. Only valid US application/serial numbers are accepted."),
            new FAQEntry("How are the individual files named",
                    "Q: How are the individual files named?",
                    "Each file is automatically named using the following format:\n" +
                            "Application Number – Mailing Date – Prosecution Event\n" +
                            "This naming convention makes it easy to identify the document without opening it."),
            new FAQEntry("Which download option should I use",
                    "Q: Which download option should I use?",
                    "It depends on how you plan to use the prosecution documents:\n" +
                            "Collated File if you want to review the complete prosecution history in a single PDF.\n" +
                            "Individual Files if you need to organize, upload, share, or review specific prosecution documents independently."),
            new FAQEntry("Will all prosecution documents be included in the Individual Files download",
                    "Q: Will all prosecution documents be included in the Individual Files download?",
                    "Yes. All available prosecution documents associated with the application are downloaded as separate files."),
            new FAQEntry("Will the contents of the downloaded documents change",
                    "Q: Will the contents of the downloaded documents change?",
                    "No. The download option changes only the output format. The document content remains unchanged."),
            new FAQEntry("Can I download both formats for the same application",
                    "Q: Can I download both formats for the same application?",
                    "Yes. You can download the prosecution documents in either format whenever required. However, you can select only one download option (Collated File or Individual Files) per request."),
            new FAQEntry("Why are Individual Files beneficial",
                    "Q: Why are Individual Files beneficial?",
                    "Downloading individual files helps users:\n" +
                            "Locate specific prosecution documents more quickly.\n" +
                            "Organize files more efficiently.\n" +
                            "Upload documents into Document Management Systems (DMS) without manual splitting.\n" +
                            "Share only the required prosecution documents with colleagues or clients."),
            new FAQEntry("How can I find a specific document in the downloaded files",
                    "Q: How can I find a specific document in the downloaded files?",
                    "For Collated File downloads, use the bookmarks available in the output PDF. They show the document type and mailing date, allowing you to jump directly to any document. For Individual Files, each document is saved separately and named for easy identification.")
    };

}