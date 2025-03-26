package Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsEngine implements CourseAnalytics {
    private DatabaseConnectionManager dbManager;

    public AnalyticsEngine() {
        this.dbManager = new DatabaseConnectionManager();
    }

    @Override
    public Map<String, Object> generateEnrollmentStatistics() {
        Map<String, Object> enrollmentStats = new HashMap<>();
        String sql = "SELECT c.courseName, COUNT(e.enrollmentId) as enrollmentCount, " +
                "c.credits, i.instructorName " +
                "FROM course c " +
                "LEFT JOIN enrollment e ON c.courseId = e.courseId " +
                "LEFT JOIN instructor i ON c.instructorId = i.instructorId " +
                "GROUP BY c.courseId, c.courseName, c.credits, i.instructorName";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<Map<String, Object>> courseEnrollments = new ArrayList<>();
            int totalEnrollments = 0;
            int totalCourses = 0;

            while (rs.next()) {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("courseName", rs.getString("courseName"));
                courseData.put("enrollmentCount", rs.getInt("enrollmentCount"));
                courseData.put("credits", rs.getInt("credits"));
                courseData.put("instructorName", rs.getString("instructorName"));

                courseEnrollments.add(courseData);
                totalEnrollments += rs.getInt("enrollmentCount");
                totalCourses++;
            }

            enrollmentStats.put("courseEnrollments", courseEnrollments);
            enrollmentStats.put("totalEnrollments", totalEnrollments);
            enrollmentStats.put("totalCourses", totalCourses);
            enrollmentStats.put("averageEnrollmentPerCourse",
                    totalCourses > 0 ? (double) totalEnrollments / totalCourses : 0);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate enrollment statistics", e);
        }

        return enrollmentStats;
    }

    @Override
    public Map<String, Object> generateGradeDistribution() {
        Map<String, Object> gradeDistribution = new HashMap<>();
        String sql = "SELECT " +
                "CASE " +
                "  WHEN score >= 90 THEN 'A' " +
                "  WHEN score >= 80 THEN 'B' " +
                "  WHEN score >= 70 THEN 'C' " +
                "  WHEN score >= 60 THEN 'D' " +
                "  ELSE 'F' " +
                "END as grade, " +
                "COUNT(*) as gradeCount, " +
                "ROUND(AVG(score), 2) as averageScore " +
                "FROM assignmentdistribution " +
                "WHERE isGraded = 1 " +
                "GROUP BY grade";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            Map<String, Integer> gradeCounts = new HashMap<>();
            Map<String, Double> gradeAverages = new HashMap<>();
            int totalGradedAssignments = 0;

            while (rs.next()) {
                String grade = rs.getString("grade");
                int count = rs.getInt("gradeCount");
                double avgScore = rs.getDouble("averageScore");

                gradeCounts.put(grade, count);
                gradeAverages.put(grade, avgScore);
                totalGradedAssignments += count;
            }

            gradeDistribution.put("gradeCounts", gradeCounts);
            gradeDistribution.put("gradeAverages", gradeAverages);
            gradeDistribution.put("totalGradedAssignments", totalGradedAssignments);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate grade distribution", e);
        }

        return gradeDistribution;
    }

    @Override
    public List<Map<String, Object>> getAverageGradesByCourse() {
        List<Map<String, Object>> courseGrades = new ArrayList<>();
        String sql = "SELECT c.courseName, " +
                "ROUND(AVG(ad.score), 2) as averageScore, " +
                "COUNT(ad.enrollmentId) as studentCount, " +
                "i.instructorName " +
                "FROM course c " +
                "LEFT JOIN enrollment e ON c.courseId = e.courseId " +
                "LEFT JOIN assignmentdistribution ad ON e.enrollmentId = ad.enrollmentId " +
                "LEFT JOIN instructor i ON c.instructorId = i.instructorId " +
                "WHERE ad.isGraded = 1 " +
                "GROUP BY c.courseId, c.courseName, i.instructorName";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("courseName", rs.getString("courseName"));
                courseData.put("averageScore", rs.getDouble("averageScore"));
                courseData.put("studentCount", rs.getInt("studentCount"));
                courseData.put("instructorName", rs.getString("instructorName"));

                String gradeLetter = determineGradeLetter(rs.getDouble("averageScore"));
                courseData.put("gradeLetter", gradeLetter);

                courseGrades.add(courseData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve average grades by course", e);
        }

        return courseGrades;
    }

    private String determineGradeLetter(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}