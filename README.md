# User Document Storage Service

## 1. Project Overview
This is a Backend Storage Service compliant with the **"User Document Storage Service"** assignment. It provides REST APIs to Search, Download, and Upload files to a user-specific folder in an AWS S3 bucket.

### Key Features
*   **Mandatory**: Search Files (Optimized with S3 Prefix).
*   **Mandatory**: Download Files.
*   **Optional (Bonus)**: Upload Files (Full S3 lifecycle implementation).
*   **Security/Isolation**: Operations are strictly scoped to `userName/` folder prefixes.
*   **No Database**: State is managed entirely via S3 object storage.

## 2. Tech Stack
*   **Language**: Java 17
*   **Framework**: Spring Boot 3.2.2
*   **Cloud Storage**: AWS S3 (AWS SDK v2)
*   **Build Tool**: Maven
*   **Testing**: JUnit 5 + Mockito
*   **Documentation**: Swagger / OpenAPI

## 3. Configuration & Setup

### prerequisites
*   Java 17+ installed
*   Maven installed
*   An AWS S3 Bucket created

### Step 1: Clone & Configure
Open `src/main/resources/application.properties` and add your AWS credentials:

```properties
server.port=8080

# AWS Configuration
aws.s3.bucket-name=YOUR_BUCKET_NAME
aws.region=us-east-1
aws.accessKeyId=YOUR_ACCESS_KEY
aws.secretAccessKey=YOUR_SECRET_KEY
```

> **Security Note**: In a production environment, use Environment Variables or IAM Roles instead of hardcoding credentials.

### Step 2: Run Application
```bash
mvn spring-boot:run
```
The server will start at `http://localhost:8080`.

## 4. API Endpoints

You can test these via **Postman** or **Swagger UI** (`http://localhost:8080/swagger-ui.html`).

### 1. Upload File (Create Data)
*   **Method**: `POST`
*   **URL**: `/api/v1/files/upload`
*   **Parameters**:
    *   `userName`: (Query) The user folder name (e.g., `sandy`).
    *   `file`: (Form-Data) The file object to upload.
*   **Curl**:
    ```bash
    curl -X POST "http://localhost:8080/api/v1/files/upload?userName=sandy" -F "file=@test.txt"
    ```

### 2. Search Files (Mandatory)
Searches strictly within `userName/` folder using optimized S3 prefixing.
*   **Method**: `GET`
*   **URL**: `/api/v1/files/search`
*   **Parameters**:
    *   `userName`: (Query) e.g., `sandy`.
    *   `searchTerm`: (Query) e.g., `logistics`.
*   **Curl**:
    ```bash
    curl "http://localhost:8080/api/v1/files/search?userName=sandy&searchTerm=logistics"
    ```

### 3. Download File (Mandatory)
Downloads a specific file from the user's folder.
*   **Method**: `GET`
*   **URL**: `/api/v1/files/download`
*   **Parameters**:
    *   `userName`: (Query) e.g., `sandy`.
    *   `fileName`: (Query) The specific filename matches from search result.
*   **Curl**:
    ```bash
    curl -O -J "http://localhost:8080/api/v1/files/download?userName=sandy&fileName=report.pdf"
    ```

## 5. Testing Strategy
The project includes a comprehensive JUnit test suite using `Mockito` to mock Amazon S3. This ensures core logic is verified **without needing real AWS credentials**.

### Running Tests
```bash
mvn test
```

### Coverage (7 Tests)
1.  ✅ **Happy Path**: `searchFiles_shouldReturnMatchingFiles`
2.  ✅ **Happy Path**: `downloadFile_shouldReturnResource`
3.  ✅ **Happy Path**: `uploadFile_shouldReturnSuccessMessage`
4.  ✅ **Edge Case**: `searchFiles_shouldReturnEmptyList_whenNoFilesMatch`
5.  ✅ **Error Handling**: `searchFiles_shouldThrowException_whenS3Fails`
6.  ✅ **Error Handling**: `downloadFile_shouldThrowRuntimeException_whenS3Fails`
7.  ✅ **Error Handling**: `uploadFile_shouldThrowException_whenS3Fails`

## 6. Project Structure
```
src/main/java/com/example/storage
├── config/           # S3 Client Configuration
├── controller/       # REST API Endpoints
├── dto/              # Data Transfer Objects
├── exception/        # Global Exception Handler
├── service/          # Business Logic (S3 calls)
└── StorageServiceApplication.java
```
