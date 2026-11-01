package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads";

    public FileUploadController() {
        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String cleanFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            Path targetLocation = Paths.get(UPLOAD_DIR).resolve(cleanFileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(cleanFileName)
                    .toUriString();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", fileDownloadUri);
            responseData.put("fileName", cleanFileName);
            responseData.put("size", file.getSize());
            responseData.put("contentType", file.getContentType());

            return ResponseEntity.ok(ApiResponse.ok("Image uploaded successfully", responseData));
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Could not store file: " + ex.getMessage()));
        }
    }

    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> uploadedList = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String originalFilename = file.getOriginalFilename();
                    String extension = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }

                    String cleanFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
                    Path targetLocation = Paths.get(UPLOAD_DIR).resolve(cleanFileName);

                    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/uploads/")
                            .path(cleanFileName)
                            .toUriString();

                    Map<String, Object> data = new HashMap<>();
                    data.put("url", fileDownloadUri);
                    data.put("fileName", cleanFileName);
                    data.put("size", file.getSize());
                    data.put("contentType", file.getContentType());
                    uploadedList.add(data);
                } catch (IOException ex) {
                    // continue with remaining files
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.ok("Uploaded " + uploadedList.size() + " files", uploadedList));
    }
}

