package Utilities;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class CourseAnalyticsImpl implements CourseAnalytics {
    private DatabaseConnectionManager dbManager;

    public CourseAnalyticsImpl() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public Map<String, Object> generateEnrollmentStatistics() {
        Map<String, Object> enrollmentStats = new HashMap<>();
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT c.courseName, COUNT(e.enrollmentId) as total_enrollments " +
                             "FROM course c " +
                             "LEFT JOIN enrollment e ON c.courseId = e.courseId " +
                             "GROUP BY c.courseId, c.courseName"
             )) {

            Map<String, Integer> courseEnrollments = new HashMap<>();
            while (rs.next()) {
                String courseName = rs.getString("courseName");
                int enrollments = rs.getInt("total_enrollments");
                courseEnrollments.put(courseName, enrollments);
            }

            enrollmentStats.put("courseEnrollments", courseEnrollments);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollmentStats;
    }

    @Override
    public Map<String, Object> generateGradeDistribution() {
        Map<String, Object> gradeDistribution = new HashMap<>();
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT grade, COUNT(*) as grade_count " +
                             "FROM enrollment " +
                             "WHERE grade IS NOT NULL " +
                             "GROUP BY grade"
             )) {

            Map<String, Integer> gradeCount = new HashMap<>();
            while (rs.next()) {
                String grade = rs.getString("grade");
                int count = rs.getInt("grade_count");
                gradeCount.put(grade, count);
            }

            gradeDistribution.put("gradeDistribution", gradeCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gradeDistribution;
    }

    @Override
    public List<Map<String, Object>> getAverageGradesByCourse() {
        List<Map<String, Object>> averageGrades = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT c.courseName, " +
                             "AVG(CASE " +
                             "    WHEN e.grade = 'A+' THEN 4.0 " +
                             "    WHEN e.grade = 'A' THEN 4.0 " +
                             "    WHEN e.grade = 'A-' THEN 3.7 " +
                             "    WHEN e.grade = 'B+' THEN 3.3 " +
                             "    WHEN e.grade = 'B' THEN 3.0 " +
                             "    WHEN e.grade = 'B-' THEN 2.7 " +
                             "    WHEN e.grade = 'C+' THEN 2.3 " +
                             "    WHEN e.grade = 'C' THEN 2.0 " +
                             "    WHEN e.grade = 'C-' THEN 1.7 " +
                             "    WHEN e.grade = 'D+' THEN 1.3 " +
                             "    WHEN e.grade = 'D' THEN 1.0 " +
                             "    WHEN e.grade = 'D-' THEN 0.7 " +
                             "    WHEN e.grade = 'F' THEN 0.0 " +
                             "    ELSE NULL END) as average_gpa, " +
                             "COUNT(e.enrollmentId) as total_students " +
                             "FROM course c " +
                             "LEFT JOIN enrollment e ON c.courseId = e.courseId " +
                             "GROUP BY c.courseId, c.courseName"
             )) {

            while (rs.next()) {
                Map<String, Object> courseGrade = new HashMap<>();
                courseGrade.put("courseName", rs.getString("courseName"));
                courseGrade.put("averageGPA", rs.getDouble("average_gpa"));
                courseGrade.put("totalStudents", rs.getInt("total_students"));
                averageGrades.add(courseGrade);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return averageGrades;
    }

    public List<Map<String, Object>> getStudentCourseMarks() {
        List<Map<String, Object>> studentMarks = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.studentName, c.courseName, e.grade " +
                             "FROM enrollment e " +
                             "JOIN student s ON e.studentId = s.studentId " +
                             "JOIN course c ON e.courseId = c.courseId " +
                             "ORDER BY s.studentName, c.courseName"
             )) {

            while (rs.next()) {
                Map<String, Object> studentCourseGrade = new HashMap<>();
                studentCourseGrade.put("studentName", rs.getString("studentName"));
                studentCourseGrade.put("courseName", rs.getString("courseName"));
                studentCourseGrade.put("grade", rs.getString("grade"));
                studentMarks.add(studentCourseGrade);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentMarks;
    }
}