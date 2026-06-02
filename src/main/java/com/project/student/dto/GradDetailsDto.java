package com.project.student.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
public class GradDetailsDto {
    private String studentName;
    private String courseTitle;
    private LocalDate completionDate;
    private Double grade;

}
