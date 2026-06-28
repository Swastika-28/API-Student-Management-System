package com.project.student.controller;

import com.project.student.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

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
            throw new RuntimeException("Wrong upload..");
        }

    }
}
