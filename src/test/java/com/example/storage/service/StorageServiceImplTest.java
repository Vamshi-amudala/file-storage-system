package com.example.storage.service;

import com.example.storage.dto.FileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "bucketName", "test-bucket");
    }

    @Test
    void searchFiles_shouldReturnMatchingFiles() {
        String userName = "sandy";
        String searchTerm = "logistics";
        String prefix = userName + "/";

        S3Object obj1 = S3Object.builder().key(prefix + "logistics_report.pdf").size(1024L).lastModified(Instant.now())
                .build();
        S3Object obj2 = S3Object.builder().key(prefix + "other_file.txt").size(512L).lastModified(Instant.now())
                .build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(obj1, obj2)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<FileDto> result = storageService.searchFiles(userName, searchTerm);

        assertEquals(1, result.size());
        assertEquals("logistics_report.pdf", result.get(0).getFileName());
    }

    @Test
    void downloadFile_shouldReturnResource() {
        String userName = "sandy";
        String fileName = "test.txt";
        String content = "Hello World";

        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(
                GetObjectResponse.builder().build(),
                content.getBytes(StandardCharsets.UTF_8));

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        Resource resource = storageService.downloadFile(userName, fileName);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void searchFiles_shouldReturnEmptyList_whenNoFilesMatch() {
        String userName = "sandy";
        String searchTerm = "nonexistent";
        ListObjectsV2Response response = ListObjectsV2Response.builder().contents(Collections.emptyList()).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<FileDto> result = storageService.searchFiles(userName, searchTerm);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchFiles_shouldThrowException_whenS3Fails() {
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> storageService.searchFiles("sandy", "term"));
    }

    @Test
    void downloadFile_shouldThrowRuntimeException_whenS3Fails() {
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 Service Unavailable"));

        assertThrows(RuntimeException.class, () -> storageService.downloadFile("sandy", "test.txt"));
    }

    @Test
    void uploadFile_shouldReturnSuccessMessage() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        String userName = "vamshi";

        String result = storageService.uploadFile(userName, mockFile);

        assertTrue(result.contains("successfully"));
    }

    @Test
    void uploadFile_shouldThrowException_whenS3Fails() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> storageService.uploadFile("vamshi", mockFile));
    }
}
