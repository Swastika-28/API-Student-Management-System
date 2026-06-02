package com.project.student.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudentReport {
    private Long sId;
    private String fullName;
    private String title;
    private Double avgGrade;
    private List<AssignmentDetail> assignments=new ArrayList<>();

    public void addAssignment(AssignmentDetail assignmentDetail){
        this.assignments.add(assignmentDetail);
    }

    @Data
    public static class AssignmentDetail implements Comparable<AssignmentDetail> {
        private String assignmentId;
        private String assignmentName;
        private Long grade;
        private String status;

        @Override
        public int compareTo(AssignmentDetail other) {
            return this.assignmentId.compareTo(other.assignmentId);
        }
    }


}

