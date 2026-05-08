package com.backend.global.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.global.error.exception.FileUploadFailedException;
import com.backend.global.error.exception.InvalidFileTypeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf"
    );

    private static final Map<String, byte[][]> MAGIC_BYTES = Map.of(
            ".jpg", new byte[][]{{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}},
            ".jpeg", new byte[][]{{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}},
            ".png", new byte[][]{{(byte) 0x89, 0x50, 0x4E, 0x47}},
            ".gif", new byte[][]{{0x47, 0x49, 0x46, 0x38}},
            ".webp", new byte[][]{{0x52, 0x49, 0x46, 0x46}},
            ".pdf", new byte[][]{{0x25, 0x50, 0x44, 0x46}}
    );

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    public List<String> uploadFiles(List<MultipartFile> files, String directory) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String url = uploadFile(file, directory);
            uploadedUrls.add(url);
        }

        return uploadedUrls;
    }

    public String uploadFile(MultipartFile file, String directory) {
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename).toLowerCase();
        validateFileExtension(extension);
        validateMagicBytes(file, extension);
        String key = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(detectContentType(originalFilename))
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            return getFileUrl(key);
        } catch (IOException e) {
            log.error("S3 upload failed - IOException", e);
            throw new FileUploadFailedException();
        } catch (SdkClientException | S3Exception e) {
            log.error("S3 upload failed - AWS SDK error", e);
            throw new FileUploadFailedException();
        }
    }

    private String getFileUrl(String key) {
        // Real AWS S3 URL format
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    private void validateFileExtension(String extension) {
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileTypeException();
        }
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        byte[][] expectedSignatures = MAGIC_BYTES.get(extension);
        if (expectedSignatures == null) {
            return;
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = is.read(header);
            if (bytesRead < 0) {
                throw new InvalidFileTypeException();
            }

            for (byte[] signature : expectedSignatures) {
                if (bytesRead >= signature.length && startsWith(header, signature)) {
                    return;
                }
            }
            throw new InvalidFileTypeException();
        } catch (IOException e) {
            throw new FileUploadFailedException();
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        for (String url : fileUrls) {
            deleteFile(url);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (SdkClientException | S3Exception e) {
            log.error("S3 delete failed for URL: {}", fileUrl, e);
        }
    }

    private String detectContentType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "application/octet-stream";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    public String generatePresignedUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return fileUrl;
        }
        String key = extractKeyFromUrl(fileUrl);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_EXPIRATION)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public List<String> generatePresignedUrls(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return Collections.emptyList();
        }
        return fileUrls.stream()
                .map(this::generatePresignedUrl)
                .toList();
    }

    private String extractKeyFromUrl(String fileUrl) {
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        String key = fileUrl.replace(prefix, "");
        return java.net.URLDecoder.decode(key, java.nio.charset.StandardCharsets.UTF_8);
    }
}
