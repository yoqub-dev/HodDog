package com.example.hoddog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

            if (originalFileName.contains("..")) {
                throw new RuntimeException("Invalid file path: " + originalFileName);
            }

            // ✅ MIME TYPE CHECK
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            // ✅ SIZE CHECK (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("File size exceeds 5MB");
            }

            // ✅ XAVFSIZ EXTENSION
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf(".");
            if (dotIndex != -1) {
                fileExtension = originalFileName.substring(dotIndex);
            }

            String newFileName = UUID.randomUUID() + fileExtension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetLocation = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/products/" + newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }


    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir).resolve(fileName);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file", ex);
        }
    }
}