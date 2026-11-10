package com.amoney.rentify_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/data/storage")
public class DataStorageController {

    @Value("${azure.blob.local-storage-path:./data/storage/}")
    private String localStoragePath;

    @GetMapping("/{blobName}")
    public ResponseEntity<Resource> getBlob(
            @PathVariable String blobName,
            @RequestParam(value = "sas", required = false) String sas,
            @RequestParam(value = "exp", required = false) Long exp) {

        try {
            Path filePath = Paths.get(localStoragePath).resolve(blobName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            String lower = blobName.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (lower.endsWith(".png")) contentType = "image/png";
            else if (lower.endsWith(".webp")) contentType = "image/webp";
            else if (lower.endsWith(".pdf")) contentType = "application/pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
