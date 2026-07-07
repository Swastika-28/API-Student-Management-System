package com.project.student.repo;

import com.project.student.dto.GradDetailsDto;
import com.project.student.dto.StudentReport;
import com.project.student.mapper.StudentResultSetExtractor;
import exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EducationRepo {
    private final NamedParameterJdbcTemplate db;

    private static final RowMapper<GradDetailsDto> mapper = (rs, rownum) -> {
        GradDetailsDto gradDetailsDto = new GradDetailsDto();
        gradDetailsDto.setCourseTitle(rs.getString("title"));
        gradDetailsDto.setStudentName(rs.getString("full_name"));
        gradDetailsDto.setGrade(rs.getDouble("final_grade"));
        gradDetailsDto.setCompletionDate(rs.getObject("completion_date", LocalDate.class));
        return gradDetailsDto;
    };


    public Integer countUngraded(Long sId, Long cId) {
        String sql = "SELECT COUNT(*) FROM assignments " +
                "WHERE course_id=:courseId " +
                "AND student_id=:studentId AND grade is null";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("courseId", cId);
        param.addValue("studentId", sId);
        return db.queryForObject(sql, param, Integer.class);
    }

    public Integer checkAssignmentExists(Long sId, Long cId) {
        String sql = "SELECT COUNT(*) FROM assignments " +
                "WHERE course_id=:courseId " +
                "AND student_id=:studentId";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("courseId", cId);
        param.addValue("studentId", sId);
        return db.queryForObject(sql, param, Integer.class);
    }

    public Double getAvg(Long sId, Long cId) {
        String sql = "SELECT AVG(grade) FROM assignments " +
                "WHERE student_id=:studentId AND " +
                "course_id=:courseId";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("studentId", sId);
        param.addValue("courseId", cId);
        return db.queryForObject(sql, param, Double.class);
    }

    public void finalizeEnrollment(Long sId, Long cId, Double grade) {
        String sql = "UPDATE enrollments set final_grade=:grade, " +
                "completion_date=now()," +
                "status='COMPLETED'" +
                " where student_id=:studentId and course_id=:courseId";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("grade", grade);
        param.addValue("studentId", sId);
        param.addValue("courseId", cId);
        db.update(sql, param);
    }

    public boolean addCertificate(Long sId, Long cId) {
        String sql = "INSERT INTO certificates(student_id,course_id) VALUES (:studentId,:courseId) ";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("studentId", sId);
        param.addValue("courseId", cId);
        db.update(sql, param);
        return false;
    }

    public GradDetailsDto getGradDetails(Long sId, Long cId) {
        String sql = "SELECT c.title, s.full_name, e.final_grade, e.completion_date  FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.course_id JOIN students s ON e.student_id = s.student_id WHERE c.course_id = :course_id AND s.student_id = :student_id";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("student_id", sId);
        param.addValue("course_id", cId);

        return db.queryForObject(sql, param, mapper);
    }

    public Integer countCertificate(Long sId, Long cId) {
        String sql = "SELECT COUNT(*) FROM certificates" +
                "  WHERE course_id=:course_id and student_id=:student_id";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("student_id", sId);
        param.addValue("course_id", cId);
        return db.queryForObject(sql, param, Integer.class);
    }

    public String getStudentEmail(Long sId) {
        String sql = "SELECT email FROM students WHERE student_id=:studentId";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("studentId", sId);
        try {
            return db.queryForObject(sql, param, String.class);
        } catch (Exception e) {
            throw new RuntimeException("No email address found registered for student ID: " + sId);
        }
    }

    public List<StudentReport> getAllStudentReports() {
        String sql = """
                SELECT
                       s.student_id,
                       s.full_name,
                       c.title AS course_title,
                       a.grade,
                       a.title AS assignment_title,
                       a.assignment_id,
                       AVG(a.grade) OVER(PARTITION BY s.student_id) AS average_grade
                       FROM students s
                LEFT JOIN assignments a ON s.student_id = a.student_id
                LEFT JOIN courses c ON c.course_id = a.course_id""";
        return db.query(sql, new StudentResultSetExtractor());
    }

    public Integer countSId(Long sId) {
        String sql = "SELECT COUNT(*) FROM students  " +
                "WHERE student_id=:student_id";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("student_id", sId);
        return db.queryForObject(sql, param, Integer.class);
    }

    public void uploadDoc(Long student_id, String additional_documents) {
        String sql = "UPDATE students " +
                "SET additional_documents=:additional_documents" +
                " WHERE student_id=:student_id";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("student_id", student_id);
        param.addValue("additional_documents", additional_documents);
        db.update(sql, param);
    }

    public String getFileForStudent(Long sId) {
        String sql = "SELECT additional_documents FROM students " +
                "WHERE student_id=:student_id";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("student_id", sId);
        RowMapper<String> mapper = (rs, rowNum) -> rs.getString("additional_documents");
        List<String> res = db.query(sql, param, mapper);
        if (!res.isEmpty() && res.get(0) != null) {
            return res.get(0);
        } else {
            throw new FileStorageException("No file for student " + sId);
        }
    }
}
