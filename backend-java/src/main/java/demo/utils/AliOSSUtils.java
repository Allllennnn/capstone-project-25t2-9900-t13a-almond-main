package demo.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.Date;

/**
 * Aliyun OSS utility class
 */
@Slf4j
@Component
public class AliOSSUtils {

    @Autowired
    private AliOSSProperties aliOSSProperties;

    /**
     * Upload a file to OSS
     */
    public String upload(MultipartFile file) throws IOException {
        OSS ossClient = null;
        InputStream inputStream = null;

        try {
            // Retrieve OSS configuration
            String endpoint = aliOSSProperties.getEndpoint();
            String accessKeyId = aliOSSProperties.getAccessKeyId();
            String accessKeySecret = aliOSSProperties.getAccessKeySecret();
            String bucketName = aliOSSProperties.getBucketName();

            log.info("OSS config - endpoint: {}, bucketName: {}", endpoint, bucketName);

            // Validate configuration
            if (endpoint == null || accessKeyId == null || accessKeySecret == null || bucketName == null) {
                throw new IllegalArgumentException("Incomplete OSS configuration");
            }

            inputStream = file.getInputStream();

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new IllegalArgumentException("Uploaded file must have an original filename");
            }

            // Safely extract file extension
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                extension = originalFilename.substring(dotIndex);
            }

            // Generate a unique object name to avoid collisions
            String objectName = UUID.randomUUID().toString() + extension;

            String fullEndpoint = endpoint.startsWith("http") ? endpoint : "https://" + endpoint;
            ossClient = new OSSClientBuilder().build(fullEndpoint, accessKeyId, accessKeySecret);

            log.info("Uploading file to OSS, objectName: {}", objectName);
            ossClient.putObject(bucketName, objectName, inputStream);
            log.info("File uploaded to OSS successfully");

            // Construct access URL
            String url;
            if (endpoint.startsWith("http")) {
                String cleanEndpoint = endpoint.replaceFirst("^https?://", "");
                url = String.format("https://%s.%s/%s", bucketName, cleanEndpoint, objectName);
            } else {
                url = String.format("https://%s.%s/%s", bucketName, endpoint, objectName);
            }
            log.info("Generated file URL: {}", url);

            return url;

        } catch (OSSException e) {
            log.error("OSS service error: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to OSS: " + e.getMessage(), e);
        } catch (ClientException e) {
            log.error("OSS client error: {}", e.getMessage(), e);
            throw new IOException("OSS client exception during upload: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("File upload error: {}", e.getMessage(), e);
            throw new IOException("File upload failed: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing input stream: {}", e.getMessage());
                }
            }
            if (ossClient != null) {
                try {
                    ossClient.shutdown();
                } catch (Exception e) {
                    log.warn("Error closing OSS client: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Generate a presigned URL valid for 24 hours
     */
    public String generatePresignedUrl(String objectName) {
        OSS ossClient = null;
        try {
            String endpoint = aliOSSProperties.getEndpoint();
            String accessKeyId = aliOSSProperties.getAccessKeyId();
            String accessKeySecret = aliOSSProperties.getAccessKeySecret();
            String bucketName = aliOSSProperties.getBucketName();

            if (endpoint == null || accessKeyId == null || accessKeySecret == null || bucketName == null) {
                throw new IllegalArgumentException("Incomplete OSS configuration");
            }

            String fullEndpoint = endpoint.startsWith("http") ? endpoint : "https://" + endpoint;
            ossClient = new OSSClientBuilder().build(fullEndpoint, accessKeyId, accessKeySecret);

            Date expiration = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
            request.setExpiration(expiration);

            String presignedUrl = ossClient.generatePresignedUrl(request).toString();
            log.info("Generated presigned URL: {}", presignedUrl);

            return presignedUrl;

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            return null;
        } finally {
            if (ossClient != null) {
                try {
                    ossClient.shutdown();
                } catch (Exception e) {
                    log.warn("Error closing OSS client: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Extract the object name from a full URL
     */
    public String extractObjectName(String fileUrl) {
        try {
            String bucketName = aliOSSProperties.getBucketName();
            String endpoint = aliOSSProperties.getEndpoint();

            String expectedPrefix;
            if (endpoint.startsWith("http")) {
                String cleanEndpoint = endpoint.replaceFirst("^https?://", "");
                expectedPrefix = String.format("https://%s.%s/", bucketName, cleanEndpoint);
            } else {
                expectedPrefix = String.format("https://%s.%s/", bucketName, endpoint);
            }

            if (fileUrl.startsWith(expectedPrefix)) {
                return fileUrl.substring(expectedPrefix.length());
            }

            log.warn("Unable to extract object name from URL: {}", fileUrl);
            return null;
        } catch (Exception e) {
            log.error("Error extracting object name: {}", e.getMessage(), e);
            return null;
        }
    }
}
