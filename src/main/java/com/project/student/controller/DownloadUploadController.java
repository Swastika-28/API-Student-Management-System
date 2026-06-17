package com.project.student.controller;

import com.project.student.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.InputStream;


@RequestMapping("/api-grad/file")
public class DownloadUploadController {

    private FileService fileService;
    @PostMapping("/upload")
    public ResponseEntity<String> upload(HttpServletRequest httpServletRequest, @RequestHeader("File-Name") String  filaName){
        try(InputStream inputStream=httpServletRequest.getInputStream()) {
            if(inputStream==null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Input Stream is empty");
            }
            String savedFileName=
        }
    }
}
