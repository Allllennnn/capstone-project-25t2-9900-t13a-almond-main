package demo.controller;

import demo.pojo.Result;
import demo.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private AliOSSUtils aliOSSUtils;

    /**
     * Generate a presigned URL for file preview
     */
    @PostMapping("/preview-url")
    public Result generatePreviewUrl(@RequestBody String fileUrl) {
        log.info("Request to generate preview URL, original URL: {}", fileUrl);

        try {
            // Extract objectName from the full URL
            String objectName = aliOSSUtils.extractObjectName(fileUrl);
            if (objectName == null) {
                return Result.error("Invalid file URL");
            }

            // Generate a presigned temporary access URL
            String presignedUrl = aliOSSUtils.generatePresignedUrl(objectName);
            if (presignedUrl == null) {
                return Result.error("Failed to generate preview URL");
            }

            log.info("Successfully generated preview URL: {}", presignedUrl);
            return Result.success(presignedUrl);

        } catch (Exception e) {
            log.error("Error occurred while generating preview URL: {}", e.getMessage(), e);
            return Result.error("Failed to generate preview URL: " + e.getMessage());
        }
    }

    /**
     * Proxy file download to handle CORS issues
     */
    @GetMapping("/proxy")
    public void proxyFile(@RequestParam String url,
                          @RequestParam(required = false) String filename,
                          HttpServletResponse response) {
        log.info("Proxy file download - URL: {}, filename: {}", url, filename);

        try {
            // Extract objectName from the full URL
            String objectName = aliOSSUtils.extractObjectName(url);
            if (objectName == null) {
                response.setStatus(404);
                return;
            }

            // Generate a presigned temporary access URL
            String presignedUrl = aliOSSUtils.generatePresignedUrl(objectName);
            if (presignedUrl == null) {
                response.setStatus(500);
                return;
            }

            // Redirect to the presigned URL
            response.sendRedirect(presignedUrl);

        } catch (Exception e) {
            log.error("Failed to proxy file download: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }
}
