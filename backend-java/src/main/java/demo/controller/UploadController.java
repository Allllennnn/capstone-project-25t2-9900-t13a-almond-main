package demo.controller;

import demo.pojo.Result;
import demo.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
public class UploadController {

    @Autowired
    private AliOSSUtils aliOSSUtils;

    @PostMapping("/upload")
    @Transactional
    public Result upload(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request, filename: {}, size: {} bytes",
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getSize() : 0);

        try {
            // Validate file presence
            if (file == null || file.isEmpty()) {
                log.warn("File is empty");
                return Result.error("Uploaded file cannot be empty");
            }

            // Validate file size (10MB limit)
            if (file.getSize() > 10 * 1024 * 1024) {
                log.warn("File too large: {} bytes", file.getSize());
                return Result.error("File size cannot exceed 10MB");
            }

            // Validate filename
            String filename = file.getOriginalFilename();
            if (filename == null || filename.trim().isEmpty()) {
                log.warn("Filename is empty");
                return Result.error("Filename cannot be empty");
            }

            // Upload file via Aliyun OSS
            String url = aliOSSUtils.upload(file);
            log.info("File uploaded successfully, access URL: {}", url);

            return Result.success(url);

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return Result.error("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during file upload: {}", e.getMessage(), e);
            return Result.error("File upload failed, please try again later");
        }
    }
}
