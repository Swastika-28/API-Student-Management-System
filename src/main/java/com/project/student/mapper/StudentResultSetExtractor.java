package com.project.student.mapper;

import com.project.student.dto.StudentReport;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StudentResultSetExtractor implements ResultSetExtractor<List<StudentReport>> {

    @Override
    public List<StudentReport> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, StudentReport> resultMap = new LinkedHashMap<>();

        while (rs.next()) {
            Long sId = rs.getLong("student_id");
            StudentReport studentReport = resultMap.computeIfAbsent(sId, id -> {
                try {
                    StudentReport report = new StudentReport();
                    report.setSId(id);
                    report.setFullName(rs.getString("full_name"));
                    report.setTitle(rs.getString("course_title"));
                    report.setAvgGrade(rs.getDouble("average_grade"));
                    report.setAssignments(new ArrayList<>());
                    return report;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            Long assignmentId = rs.getLong("assignment_id");
            if (!rs.wasNull()) {
                StudentReport.AssignmentDetail assignmentDetail = new StudentReport.AssignmentDetail();
                assignmentDetail.setGrade(rs.getLong("grade"));
                assignmentDetail.setAssignmentName(rs.getString("assignment_title"));
                assignmentDetail.setAssignmentId(String.valueOf(assignmentId));
                studentReport.addAssignment(assignmentDetail);
                if((!Objects.isNull(assignmentDetail.getGrade()) ) &&( assignmentDetail.getGrade())==0){
                    assignmentDetail.setStatus("Pending");
                }
                else{
                    assignmentDetail.setStatus("Completed");
                }
            }
        }

        return new ArrayList<>(resultMap.values());
    }
}