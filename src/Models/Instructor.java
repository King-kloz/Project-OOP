package Models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Instructor extends Person {
    private int instructorId;
    private String department;
    private String imageUrl;

    public Instructor(String name, String email, String passwordHash, String department) {
        super(name, email, passwordHash);

        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be null or empty");
        }

        this.department = department;
    }

    @Override
    public void updateProfile() {
        String sql = "UPDATE instructor SET instructorName = ?, email = ?, " +
                "department = ?, imageUrl = ?, isActive = ?, " +
                "lastUpdated = CURRENT_TIMESTAMP WHERE instructorId = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, department);
            pstmt.setString(4, imageUrl);
            pstmt.setBoolean(5, isActive);
            pstmt.setInt(6, instructorId);

            pstmt.executeUpdate();

            this.lastUpdated = LocalDateTime.now();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update instructor profile", e);
        }
    }

    public List<Course> getManagedCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT courseId, courseName, credits, isActive " +
                "FROM course WHERE instructorId = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, this.instructorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(
                            rs.getInt("courseId"),
                            rs.getString("courseName"),
                            rs.getInt("credits"),
                            this.instructorId,
                            rs.getBoolean("isActive")
                    );
                    courses.add(course);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve managed courses", e);
        }

        return courses;
    }

    public void assignGrade(int enrollmentId, double score, String feedback) {
        String sql = "UPDATE assignmentdistribution SET " +
                "score = ?, isGraded = 1, feedback = ? " +
                "WHERE enrollmentId = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, score);
            pstmt.setString(2, feedback);
            pstmt.setInt(3, enrollmentId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to assign grade", e);
        }
    }

    public int getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static class Course {
        private int courseId;
        private String courseName;
        private int credits;
        private int instructorId;
        private boolean isActive;

        public Course(int courseId, String courseName, int credits,
                      int instructorId, boolean isActive) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.credits = credits;
            this.instructorId = instructorId;
            this.isActive = isActive;
        }

        public int getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }
        public int getCredits() { return credits; }
        public int getInstructorId() { return instructorId; }
        public boolean isActive() { return isActive; }
    }
}