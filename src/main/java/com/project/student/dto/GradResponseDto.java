package com.project.student.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradResponseDto {
    private Long studentId;
    private String status;
    private Double grade;
    private boolean honors;
    private String msg;
}
