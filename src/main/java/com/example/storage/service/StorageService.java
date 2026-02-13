package com.example.storage.service;

import com.example.storage.dto.FileDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    List<FileDto> searchFiles(String userName, String searchTerm);

    Resource downloadFile(String userName, String fileName);

    String uploadFile(String userName, MultipartFile file);
}
