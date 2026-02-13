package com.example.storage.service;

import com.example.storage.dto.FileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public List<FileDto> searchFiles(String userName, String searchTerm) {
        String prefix = userName + "/";
        log.info("Searching files for user: {} with term: {}", userName, searchTerm);

        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
            List<S3Object> s3Objects = listObjectsV2Response.contents();

            if (s3Objects.isEmpty()) {
                return Collections.emptyList();
            }

            return s3Objects.stream()
                    .filter(s3Object -> {
                        String key = s3Object.key();
                        String fileName = key.substring(prefix.length());
                        return fileName.toLowerCase().contains(searchTerm.toLowerCase());
                    })
                    .map(s3Object -> {
                        String key = s3Object.key();
                        String fileName = key.substring(prefix.length());
                        return FileDto.builder()
                                .fileName(fileName)
                                .size(s3Object.size())
                                .lastModified(s3Object.lastModified())
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching files for user: {}", userName, e);
            throw new RuntimeException("Failed to search files from S3", e);
        }
    }

    @Override
    public Resource downloadFile(String userName, String fileName) {
        String key = userName + "/" + fileName;
        log.info("Downloading file: {}", key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            byte[] data = objectBytes.asByteArray();
            return new ByteArrayResource(data);

        } catch (Exception e) {
            log.error("Error downloading file: {}", key, e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    @Override
    public String uploadFile(String userName, MultipartFile file) {
        String key = userName + "/" + file.getOriginalFilename();
        log.info("Uploading file: {}", key);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            return "File uploaded successfully: " + key;

        } catch (IOException e) {
            log.error("Error uploading file: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
}
