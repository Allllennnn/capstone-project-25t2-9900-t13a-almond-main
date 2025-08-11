package demo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@Slf4j
@Component
public class DocumentParser {

    /**
     * Parse document content from a file URL
     * @param fileUrl the URL of the file
     * @return the parsed text content
     */
    public String parseDocumentFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return "Document URL is empty";
        }

        try {
            log.info("Starting to parse document: {}", fileUrl);

            // Open connection to the URL
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000); // 10s connect timeout
            connection.setReadTimeout(30000);    // 30s read timeout

            try (InputStream inputStream = connection.getInputStream()) {
                // Determine document type by Content-Type or file extension
                String contentType = connection.getContentType();
                String fileName = getFileNameFromUrl(fileUrl);

                log.info("Document type: {}, file name: {}", contentType, fileName);

                if (isPdfFile(contentType, fileName)) {
                    return parsePdfDocument(inputStream);
                } else if (isWordFile(contentType, fileName)) {
                    return parseWordDocument(inputStream, fileName);
                } else if (isTextFile(contentType, fileName)) {
                    return parseTextDocument(inputStream);
                } else {
                    return String.format("Unsupported document type: %s", contentType);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse document: {}", fileUrl, e);
            return String.format("Document parsing failed: %s", e.getMessage());
        }
    }

    /** Parse a PDF document */
    private String parsePdfDocument(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDF parsed successfully, content length: {}", text.length());
            return text.trim();
        }
    }

    /** Parse a Word document (.doc or .docx) */
    private String parseWordDocument(InputStream inputStream, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".docx")) {
            // Parse .docx
            try (XWPFDocument document = new XWPFDocument(inputStream)) {
                StringBuilder content = new StringBuilder();
                List<XWPFParagraph> paragraphs = document.getParagraphs();
                for (XWPFParagraph paragraph : paragraphs) {
                    String text = paragraph.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        content.append(text).append("\n");
                    }
                }
                String result = content.toString().trim();
                log.info("DOCX parsed successfully, content length: {}", result.length());
                return result;
            }
        } else {
            // Parse .doc
            try (HWPFDocument document = new HWPFDocument(inputStream)) {
                WordExtractor extractor = new WordExtractor(document);
                String text = extractor.getText();
                log.info("DOC parsed successfully, content length: {}", text.length());
                return text.trim();
            }
        }
    }

    /** Parse a plain text document */
    private String parseTextDocument(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            content.append(new String(buffer, 0, bytesRead, "UTF-8"));
        }
        String result = content.toString().trim();
        log.info("Text document parsed successfully, content length: {}", result.length());
        return result;
    }

    /** Determine if the file is a PDF */
    private boolean isPdfFile(String contentType, String fileName) {
        return (contentType != null && contentType.contains("pdf")) ||
                (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }

    /** Determine if the file is a Word document */
    private boolean isWordFile(String contentType, String fileName) {
        if (contentType != null) {
            return contentType.contains("msword") ||
                    contentType.contains("wordprocessingml") ||
                    contentType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            return lower.endsWith(".doc") || lower.endsWith(".docx");
        }
        return false;
    }

    /** Determine if the file is a plain text file */
    private boolean isTextFile(String contentType, String fileName) {
        if (contentType != null) {
            return contentType.contains("text/") || contentType.contains("text/plain");
        }
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            return lower.endsWith(".txt") || lower.endsWith(".md");
        }
        return false;
    }

    /** Extract the file name from a URL */
    private String getFileNameFromUrl(String url) {
        if (url == null) return null;
        try {
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash < url.length() - 1) {
                String fileName = url.substring(lastSlash + 1);
                int q = fileName.indexOf('?');
                if (q != -1) {
                    fileName = fileName.substring(0, q);
                }
                return fileName;
            }
        } catch (Exception e) {
            log.warn("Unable to extract file name from URL: {}", url);
        }
        return null;
    }
}
