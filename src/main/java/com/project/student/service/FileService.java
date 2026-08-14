package com.project.student.service;

import com.project.student.dto.FileDetailsDto;
import com.project.student.repo.EducationRepo;
import com.project.student.repo.FileRepo;
import exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import utility.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileService {
    private final FileRepo fileRepo;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.max-size-bytes}")
    private Long maxSizeBytes;

    private final EducationRepo educationRepo;

    public void storeMultipleFiles(Long studentId, MultipartFile[] files) {
        if (educationRepo.countSId(studentId) == 0) {
            throw new FileStorageException("Invalid Student id ");
        }

        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = saveToDiskOnly(file);
            LocalDateTime uploadDateTime = LocalDateTime.now();
            educationRepo.saveDocument(studentId, uniqueFileName, originalFileName, uploadDateTime);
        }
    }

    private String saveToDiskOnly(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty() || originalFileName.contains("..")) {
            throw new FileStorageException("Invalid file name");
        }
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        if (!Constants.ALLOWED_EXTENSION.contains(ext)) {
            throw new FileStorageException("Invalid Extension");
        }

        String uniqueFileName = UUID.randomUUID().toString() + "." + ext;
        try {
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);
            Path targetFilePath = targetLocation.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

            if (Files.size(targetFilePath) > maxSizeBytes) {
                Files.deleteIfExists(targetFilePath);
                throw new FileStorageException("File exceeds max size");
            }
            return uniqueFileName;
        } catch (IOException e) {
            throw new FileStorageException("Could not store file: " + e.getMessage());
        }
    }

    public List<String> listAllFiles(Long studentId) {
        if (educationRepo.countSId(studentId) == 0) {
            throw new FileStorageException("Invalid Student_id");
        }
        return educationRepo.listFiles(studentId);
    }

    public File downloadStudentFile(Long studentId, String fileName) {
        if (educationRepo.countSId(studentId) == 0) {
            throw new FileStorageException("Invalid Student id");
        }
        if (!educationRepo.isFileOwnedByStudent(studentId, fileName)) {
            throw new FileStorageException("File does not belong to this student or does not exist");
        }
        File file = fileRepo.getFile(uploadDir, fileName);
        if (!fileRepo.fileExists(file)) {
            throw new FileStorageException("File not found on device storage");
        }
        return file;
    }

    public List<FileDetailsDto> newListAllFiles(Long studentId) {
        if (educationRepo.countSId(studentId) == 0) {
            throw new FileStorageException("Invalid Student_id");
        }
        return educationRepo.newListFiles(studentId);
    }

    public ResponseEntity<?> storeFile(InputStream inputStream, String originalFileName, Long studentId) {
        if (educationRepo.countSId(studentId) == 0) {
            throw new FileStorageException("Invalid Student id ");
        }

        if (originalFileName == null || originalFileName.isEmpty() || originalFileName.contains("..")) {
            throw new FileStorageException("Invalid file path sequence or file name");
        }
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex == -1) {
            throw new FileStorageException("File is missing a valid exception");
        }
        String ext = originalFileName.substring(dotIndex + 1).toLowerCase();
        if (!Constants.ALLOWED_EXTENSION.contains(ext)) {
            throw new FileStorageException("Invalid Extension . Only " + Constants.ALLOWED_EXTENSION.stream().collect(Collectors.joining(",")) + " are allowed");
        }
        String uniqueFileName = UUID.randomUUID().toString() + "." + ext;
        Path targetLocation;
        Path targetFilePath;
        try {
            targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);
            targetFilePath = targetLocation.resolve(uniqueFileName);

            File targetFile = targetFilePath.toFile();


            Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

            long fileSizeBytes = Files.size(targetFilePath);

            if (fileSizeBytes > maxSizeBytes) {
                Files.deleteIfExists(targetFilePath);

                throw new FileStorageException("File size exceeds the max limit " + maxSizeBytes + "bytes");
            }

        } catch (IOException e) {
            throw new FileStorageException("Couldn't store file " + e.getMessage());
        }
        educationRepo.uploadDoc(studentId, uniqueFileName);

        log.info("Original file {} stored as {}", originalFileName, uniqueFileName);
        return ResponseEntity.status(HttpStatus.OK).body("File upload successful\n file path=" + targetFilePath);
    }

    public File loadFile(String fileName) {
        File file = fileRepo.getFile(uploadDir, fileName);
        if (!fileRepo.fileExists(file)) {
            throw new FileStorageException("File not found " + fileName);
        }
        return file;
    }

    public ResponseEntity<Resource> viewFile(Long sId) throws IOException {
        String fileName = educationRepo.getFileForStudent(sId);
        File file = fileRepo.getFile(uploadDir, fileName);

        if (!fileRepo.fileExists(file)) {
            throw new FileStorageException("File not found " + fileName);
        }
        Resource resource = new FileSystemResource(file);
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok().
                header(HttpHeaders.CONTENT_DISPOSITION, fileName)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

}