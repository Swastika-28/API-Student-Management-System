package com.project.student.service;

import com.project.student.dto.GradDetailsDto;
import com.project.student.dto.GradResponseDto;
import com.project.student.dto.StudentReport;
import com.project.student.repo.EducationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GraduationService {
    private final EducationRepo educationRepo;
    private final PdfService pdfService;
    private final EmailService emailService;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public GradResponseDto processGPA(Long sId, Long cId) {

        if (educationRepo.checkAssignmentExists(sId, cId) == 0 || educationRepo.countUngraded(sId, cId) > 0) {
            throw new RuntimeException("Incomplete Assignment for student " + sId.toString());
        }
        Double avg = educationRepo.getAvg(sId, cId);
        if (avg == null || avg < 2.0) {
            throw new RuntimeException("Student " + sId.toString() + " marks is lower than 2.0. Failed.");
        }
        educationRepo.finalizeEnrollment(sId, cId, avg);

        boolean ishonors = (avg >= 3.5);
        if (ishonors) {
            educationRepo.addCertificate(sId, cId);
        }

        return GradResponseDto.builder()
                .status("Completed")
                .studentId(sId)
                .honors(ishonors)
                .msg("Graduated Successfully")
                .grade(avg).build();
    }

    public ResponseEntity<?> generateCertificate(Long sId, Long cId) {
        String studentEmail = "";
        if (educationRepo.checkAssignmentExists(sId, cId) > 0 && educationRepo.countCertificate(sId, cId) == 0) {
            processGPA(sId, cId);
        }

        if (educationRepo.countCertificate(sId, cId) > 0) {
            GradDetailsDto gradDetailsDto = educationRepo.getGradDetails(sId, cId);
            byte[] pdf = pdfService.generateCertificate(
                    gradDetailsDto.getStudentName(),
                    gradDetailsDto.getCourseTitle(),
                    gradDetailsDto.getGrade(),
                    dateTimeFormatter.format(gradDetailsDto.getCompletionDate())
            );

            try {
                studentEmail = educationRepo.getStudentEmail(sId);
                emailService.sendCertificate(studentEmail, gradDetailsDto.getStudentName(), pdf);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + studentEmail + ": " + e.getMessage());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", gradDetailsDto.getStudentName() + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        }

        return new ResponseEntity<>("Incomplete certification", HttpStatus.BAD_REQUEST);
    }

    public byte[] getStudentTranscript() {
        List<StudentReport> reports = educationRepo.getAllStudentReports();
        return pdfService.generateTranscriptPdf(reports);
    }
}