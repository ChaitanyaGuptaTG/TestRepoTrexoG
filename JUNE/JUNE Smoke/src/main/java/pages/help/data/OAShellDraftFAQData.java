package pages.help.data;

public class OAShellDraftFAQData {

    public static final FAQEntry[] OA_SHELL_DRAFT = {
            new FAQEntry("How does OA Shell generate office action responses",
                    "Q: How does OA Shell generate office action responses?",
                    "Enter the application number. OA Shell retrieves the Office Action data and auto-generates a structured response draft."),
            new FAQEntry("Do I still need to upload the Office Action rejection documents",
                    "Q: Do I still need to upload the Office Action rejection documents?",
                    "Usually no, but in some cases where the system cannot fetch the documents, you will be prompted to upload them manually."),
            new FAQEntry("How should I name my PDF and DOCX files for upload",
                    "Q: How should I name my PDF and DOCX files for upload?",
                    "Ensure both files follow the USPTO naming convention and have identical filenames (except for the file extension)."),
            // Apostrophe in "What's" — use substring without it
            new FAQEntry("in the response package",
                    "Q: What's in the response package?",
                    "OA response draft"),
            new FAQEntry("How does OA Shell handle uploaded claims",
                    "Q: How does OA Shell handle uploaded claims?",
                    "It parses the claims, updates status identifiers, removes formatting (strikethroughs, brackets), and retains clean text."),
            new FAQEntry("Why is unrelated content appearing in my claims section",
                    "Q: Why is unrelated content appearing in my claims section?",
                    "This may happen if a previously filed response was uploaded instead of a clean claim set."),
            new FAQEntry("Can I process multiple Office Actions at once",
                    "Q: Can I process multiple Office Actions at once?",
                    "No, OA Shell processes one Office Action at a time."),
            new FAQEntry("Does OA Shell handle all types of rejections and objections",
                    "Q: Does OA Shell handle all types of rejections and objections?",
                    "OA Shell addresses the full range of claim rejections"),
            new FAQEntry("What if my application is unpublished",
                    "Q: What if my application is unpublished?",
                    "OA Shell cannot generate a response draft for unpublished applications, as required data is not yet available."),
            new FAQEntry("Why are the claims not appearing in the generated response draft",
                    "Q: Why are the claims not appearing in the generated response draft?",
                    "The system extracts claims based on standard claims headings."),
            new FAQEntry("What types of Office Action rejections are supported by OA Shell Draft",
                    "Q: What types of Office Action rejections are supported by OA Shell Draft?",
                    "OA Shell Draft currently supports Office Action rejection documents with USPTO document codes CTNF (Non-Final Rejection) and CTFR (Final Rejection).")
    };

}