import java.io.File;

/**
 * ============================================================================
 * Class Name: Document
 * OOP Concepts Demonstrated:
 *   1. Encapsulation: Private member variables with public getters and setters.
 *   2. Data Abstraction: Models an uploaded student credential document.
 * ============================================================================
 * What this class does:
 *   - Represents a student verification document (e.g. Aadhaar Card, SSC Marksheet,
 *     HSC Marksheet, CET Scorecard, Other supporting document).
 *   - Stores file metadata including document type, original file name, stored
 *     file name, file size, and upload timestamp.
 * ============================================================================
 */
public class Document {

    // -------------------------------------------------------------
    // Private instance variables (Encapsulation)
    // -------------------------------------------------------------

    // Type identifier: "aadhaar", "ssc", "hsc", "cet", or "other"
    private String docType;

    // Human-readable title: "Aadhaar Card", "SSC Marksheet", etc.
    private String docTitle;

    // Original file name uploaded by candidate (e.g. "my_aadhaar.pdf")
    private String originalFileName;

    // Stored file name on server disk (e.g. "aadhaar_my_aadhaar.pdf")
    private String storedFileName;

    // File size in bytes
    private long fileSize;

    // File format / MIME type (e.g. "application/pdf", "image/png", "image/jpeg")
    private String fileType;

    // -------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------
    public Document() {
    }

    public Document(String docType, String docTitle, String originalFileName,
                    String storedFileName, long fileSize, String fileType) {
        this.docType = docType;
        this.docTitle = docTitle;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    // Helper: Maps document type key to official title
    public static String getTitleForType(String docType) {
        if (docType == null) return "Supporting Document";
        switch (docType.toLowerCase()) {
            case "aadhaar":
                return "Aadhaar Card";
            case "ssc":
                return "SSC (10th) Marksheet";
            case "hsc":
                return "HSC (12th) Marksheet";
            case "cet":
                return "CET Marksheet / Scorecard";
            case "other":
                return "Other Supporting Document";
            default:
                return "Supporting Document";
        }
    }

    // Helper: Returns readable file size (e.g. "1.2 MB", "450 KB")
    public String getFormattedFileSize() {
        if (fileSize <= 0) return "0 KB";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        }
        return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
    }

    // -------------------------------------------------------------
    // Getters and Setters (Encapsulation)
    // -------------------------------------------------------------
    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
