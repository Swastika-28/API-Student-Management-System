package com.project.student.service;

import com.project.student.repo.FileRepo;
import com.project.student.utility.Constants;
import exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileService {
    @Value("${file.upload.dir}")
    private final FileRepo fileRepo;
    @Value("${file.upload.dir}")
    private String uploadDir;
    @Value("${file.max-size-bytes}")
    private Long maxSizeBytes;


    public String storeFile(InputStream inputStream, String originalFileName) {
        if (originalFileName.isEmpty() || originalFileName.contains("..")) {
            throw new FileStorageException("Invalid file path sequence or file name");
        }
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex == -1) {
            throw new FileStorageException("File is missing a valid exception");
        }
        String ext = originalFileName.substring(dotIndex + 1).toLowerCase();
        if (!Constants.ALLOWED_EXTENSION.contains(ext)) {
            throw new FileStorageException("Invalid Extension . Only" + Constants.ALLOWED_EXTENSION.stream().collect(Collectors.joining(",")) + " are allowed");
        }
        String uniqueFileName = UUID.randomUUID().toString() + "." + ext;
        Path targetLocation;
        try {
            targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);
            Path targetFilePath = targetLocation.resolve(uniqueFileName);

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
        log.info("Orinal file {} stored as {}", originalFileName, uniqueFileName);
        return uniqueFileName;
    }

    public File loadFile(String fileName) {
        File file = fileRepo.getFile(uploadDir, fileName);
        if (!fileRepo.fileExists(file)) {
            throw new FileStorageException("File not found " + fileName);
        }
        return file;
    }
}
