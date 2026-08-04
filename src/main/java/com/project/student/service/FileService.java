package com.project.student.service;

import com.project.student.repo.EducationRepo;
import com.project.student.repo.FileRepo;
import utility.Constants;

import exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardCopyOption;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

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
            String uniqueFileName = saveToDiskOnly(file);
            educationRepo.saveDocument(studentId, uniqueFileName);
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

    public List<String> listAllFiles(Long studentId){
        if(educationRepo.countSId(studentId)==0){
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



}