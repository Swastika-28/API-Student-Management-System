package com.project.student.controller;

import com.project.student.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import com.project.student.dto.FileDetailsDto;
import com.project.student.service.FileService;
import exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class DownloadUploadController {

    private final FileService fileService;

    @PostMapping("/upload/{studentId}")
    public ResponseEntity<?> upload(HttpServletRequest httpServletRequest, @PathVariable Long studentId, @RequestHeader("File-Name") String filaName) {
        try (InputStream inputStream = httpServletRequest.getInputStream()) {
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No student enrolled");
            }
            return fileService.storeFile(inputStream, filaName, studentId);
        } catch (Exception e) {
            throw new RuntimeException("Wrong upload.." + e);
        }

    }

    @GetMapping("/view/{sId}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long sId) throws IOException {
        return fileService.viewFile(sId);
    }

}
    @PostMapping("/upload-multiple/{studentId}")
    public ResponseEntity<?> uploadMultiple(@PathVariable Long studentId, @RequestParam("files") MultipartFile[] files) {
        fileService.storeMultipleFiles(studentId, files);
        return ResponseEntity.ok("All files uploaded successfully");
    }

    @GetMapping("/list-files/{studentId}")
    public ResponseEntity<?> listFileForStudent(@PathVariable Long studentId) {
        List<String> fileNames = fileService.listAllFiles(studentId);
        if (fileNames.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No file uploaded for student id :" + studentId);
        }
        return ResponseEntity.ok(fileNames);
    }


    @GetMapping("/download/{studentId}/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable Long studentId, @PathVariable String fileName) {
        try {
            File file = fileService.downloadStudentFile(studentId, fileName);
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(fileBytes);
        } catch (FileStorageException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/new-list-files/{studentId}")
    public List<FileDetailsDto> newListFileForStudent(@PathVariable Long studentId) {
        return fileService.newListAllFiles(studentId);
        }






}
