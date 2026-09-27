-- RDBMS Doctor Sample Data

-- Insert Departments
INSERT INTO departments (department_id, department_name) VALUES
(1, 'Computer Science'),
(2, 'Commerce'),
(3, 'Mathematics'),
(4, 'Physics');

-- Insert Students (Rahul and Meena have NULL department_id for LEFT JOIN demonstration)
INSERT INTO students (student_id, name, department_id, age, marks) VALUES
(1, 'Arun', 1, 21, 85),
(2, 'Priya', 2, 22, 91),
(3, 'Karthik', 1, 20, 78),
(4, 'Divya', 3, 21, 88),
(5, 'Rahul', NULL, 22, 67),
(6, 'Ananya', 1, 23, 95),
(7, 'Suresh', 2, 21, 72),
(8, 'Meena', NULL, 20, 81);

-- Insert Courses
INSERT INTO courses (course_id, course_name, department_id) VALUES
(101, 'Database Systems', 1),
(102, 'Data Structures', 1),
(103, 'Financial Accounting', 2),
(104, 'Linear Algebra', 3),
(105, 'Quantum Mechanics', 4);

-- Insert Enrollments
INSERT INTO enrollments (enrollment_id, student_id, course_id, grade) VALUES
(1, 1, 101, 'A'),
(2, 1, 102, 'A'),
(3, 2, 103, 'O'),
(4, 3, 101, 'B'),
(5, 4, 104, 'A'),
(6, 6, 101, 'O'),
(7, 7, 103, 'C');
