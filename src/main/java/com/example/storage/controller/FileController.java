package com.example.storage.controller;

import com.example.storage.dto.FileDto;
import com.example.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @GetMapping("/search")
    public ResponseEntity<List<FileDto>> searchFiles(
            @RequestParam String userName,
            @RequestParam String searchTerm) {
        return ResponseEntity.ok(storageService.searchFiles(userName, searchTerm));
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String userName,
            @RequestParam String fileName) {
        Resource resource = storageService.downloadFile(userName, fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam String userName,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(storageService.uploadFile(userName, file));
    }
}
