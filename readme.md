CREATE DATABASE IF NOT EXISTS education_system;
USE education_system;

CREATE TABLE students (
student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
full_name VARCHAR(150) NOT NULL,
email VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE courses (
course_id BIGINT AUTO_INCREMENT PRIMARY KEY,
course_code VARCHAR(15) UNIQUE NOT NULL,
title VARCHAR(200) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE enrollments (
student_id BIGINT NOT NULL,
course_id BIGINT NOT NULL,
status ENUM('ACTIVE', 'COMPLETED', 'FAILED') DEFAULT 'ACTIVE',
final_grade DECIMAL(4, 2) DEFAULT NULL,
PRIMARY KEY (student_id, course_id),
FOREIGN KEY (student_id) REFERENCES students(student_id),
FOREIGN KEY (course_id) REFERENCES courses(course_id)
) ENGINE=InnoDB;

CREATE TABLE assignments (
assignment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
course_id BIGINT NOT NULL,
student_id BIGINT NOT NULL,
grade DECIMAL(4, 2), -- NULL means "Ungraded"
FOREIGN KEY (student_id) REFERENCES students(student_id),
FOREIGN KEY (course_id) REFERENCES courses(course_id)
) ENGINE=InnoDB;

CREATE TABLE certificates (
certificate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
student_id BIGINT NOT NULL,
course_id BIGINT NOT NULL,
issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (student_id) REFERENCES students(student_id)
) ENGINE=InnoDB;


------------------------------------------------------------------

INSERT SCRIPTS


INSERT INTO students (full_name, email) VALUES
('Alice Johnson', 'alice.j@example.edu'),
('Bob Smith', 'b.smith@example.edu'),
('Charlie Davis', 'charlie.d@example.edu');

INSERT INTO courses (course_code, title, credits) VALUES
('CS101', 'Introduction to Computer Science', 4),
('MATH202', 'Advanced Calculus', 3),
('ENG105', 'Technical Writing', 2);

INSERT INTO enrollments (student_id, course_id, status) VALUES
(1, 1, 'ACTIVE'), -- Alice in CS101
(1, 2, 'ACTIVE'), -- Alice in Calculus
(2, 1, 'ACTIVE'), -- Bob in CS101
(3, 3, 'ACTIVE'); -- Charlie in Tech Writing

Alice's Assignments (All Graded - Should pass validation)
INSERT INTO assignments (course_id, student_id, title, grade) VALUES
(1, 1, 'Midterm Project', 3.8),
(1, 1, 'Final Quiz', 4.0);

Bob's Assignments (One NULL grade - Should fail validation)
INSERT INTO assignments (course_id, student_id, title, grade) VALUES
(1, 2, 'Midterm Project', 3.5),
(1, 2, 'Final Quiz', NULL);