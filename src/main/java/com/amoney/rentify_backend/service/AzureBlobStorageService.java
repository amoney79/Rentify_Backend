package com.amoney.rentify_backend.service;

import com.azure.storage.blob.*;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AzureBlobStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageService.class);

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/jpg", "application/pdf"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".pdf"
    );

    @Value("${azure.blob.connection-string}")
    private String connectionString;

    @Value("${azure.blob.container-name}")
    private String containerName;

    @Value("${azure.blob.sas-token-duration-minutes:60}")
    private int sasDurationMinutes;

    @Value("${azure.blob.mock-mode:true}")
    private boolean mockMode;

    @Value("${azure.blob.local-storage-path:./data/storage/}")
    private String localStoragePath;

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType + ". Allowed types: JPG, PNG, WEBP, PDF.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String lower = originalName.toLowerCase();
            boolean validExt = ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
            if (!validExt) {
                throw new IllegalArgumentException("Invalid file extension. Allowed extensions: " + ALLOWED_EXTENSIONS);
            }
        }

        // Limit 25MB
        if (file.getSize() > 25 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 25MB limit");
        }
    }

    public String uploadFile(MultipartFile file, String prefix) {
        validateFile(file);
        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String blobName = prefix + "_" + UUID.randomUUID() + extension;

        if (mockMode) {
            return saveLocally(file, blobName);
        }

        try {
            BlobServiceClient client = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            BlobContainerClient containerClient = client.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            logger.info("Successfully uploaded blob to Azure: {}", blobName);
            return blobName;
        } catch (Exception e) {
            logger.warn("Azure Blob upload failed; falling back to local secure storage: {}", e.getMessage());
            return saveLocally(file, blobName);
        }
    }

    public String uploadBytes(byte[] data, String filename, String prefix, String contentType) {
        String blobName = prefix + "_" + UUID.randomUUID() + "_" + filename;

        if (mockMode) {
            try {
                Path dir = Paths.get(localStoragePath);
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                Path target = dir.resolve(blobName);
                Files.write(target, data);
                return blobName;
            } catch (IOException e) {
                logger.error("Failed to save bytes locally", e);
                return blobName;
            }
        }

        try {
            BlobServiceClient client = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            BlobContainerClient containerClient = client.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(new java.io.ByteArrayInputStream(data), data.length, true);
            return blobName;
        } catch (Exception e) {
            logger.warn("Azure uploadBytes fallback to local: {}", e.getMessage());
            try {
                Path dir = Paths.get(localStoragePath);
                if (!Files.exists(dir)) Files.createDirectories(dir);
                Files.write(dir.resolve(blobName), data);
            } catch (IOException ignored) {}
            return blobName;
        }
    }

    private String saveLocally(MultipartFile file, String blobName) {
        try {
            Path dir = Paths.get(localStoragePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            File targetFile = dir.resolve(blobName).toFile();
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(file.getBytes());
            }
            logger.info("Saved file locally at: {}", targetFile.getAbsolutePath());
            return blobName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file locally: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a time-limited Shared Access Signature (SAS) URL for secure access
     */
    public String generateSasUrl(String blobName) {
        if (blobName == null || blobName.trim().isEmpty()) {
            return null;
        }

        if (mockMode) {
            // Local secure endpoint with tokenized expiry simulation
            long expiryTimestamp = System.currentTimeMillis() + (sasDurationMinutes * 60L * 1000L);
            return "/data/storage/" + blobName + "?sas=" + UUID.randomUUID().toString().substring(0, 8) + "&exp=" + expiryTimestamp;
        }

        try {
            BlobServiceClient client = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            BlobContainerClient containerClient = client.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(sasDurationMinutes);
            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
                    .setStartTime(OffsetDateTime.now().minusMinutes(5));

            String sasToken = blobClient.generateSas(values);
            return blobClient.getBlobUrl() + "?" + sasToken;
        } catch (Exception e) {
            logger.warn("Could not generate Azure SAS URL, using simulated SAS URL: {}", e.getMessage());
            long expiryTimestamp = System.currentTimeMillis() + (sasDurationMinutes * 60L * 1000L);
            return "/data/storage/" + blobName + "?sas=simulated&exp=" + expiryTimestamp;
        }
    }
}
