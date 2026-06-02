package com.project.student.controller;


import com.project.student.dto.GradResponseDto;
import com.project.student.service.GraduationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api-grad")
public class GraduationController {

    private final GraduationService graduationService;

    @PostMapping("/finalize/{sId}/{cId}")
    public GradResponseDto finalize(@PathVariable Long sId, @PathVariable Long cId) {
        return graduationService.processGPA(sId, cId);
    }

    @GetMapping("/download/{sId}/{cId}")
    public ResponseEntity<?> downloadCertificate(@PathVariable Long sId, @PathVariable Long cId) {

        return graduationService.generateCertificate(sId, cId);
    }



    @GetMapping("/transcript/{sId}")
    public ResponseEntity<byte[]> downloadTranscript(@PathVariable Long sId){
        byte[] pdf= graduationService.getStudentTranscript();
        return ResponseEntity.ok().
                header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=transcript_"+ sId +".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);

    }
}
