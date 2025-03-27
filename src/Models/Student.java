package Models;

<<<<<<< HEAD
public class Student {
}
=======
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Student extends Person {
    private int studentId;
    private LocalDate dateOfBirth;
    private String imageUrl;

    // Constructor with all required fields
    public Student(String name, String email, String passwordHash, LocalDate dateOfBirth) {
        super(name, email, passwordHash);

        // Validate date of birth
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public void updateProfile() {
        String sql = "UPDATE student SET studentName = ?, email = ?, " +
                "dateOfBirth = ?, imageUrl = ?, isActive = ?, " +
                "lastUpdated = CURRENT_TIMESTAMP WHERE studentId = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setObject(3, dateOfBirth);
            pstmt.setString(4, imageUrl);
            pstmt.setBoolean(5, isActive);
            pstmt.setInt(6, studentId);

            pstmt.executeUpdate();
            this.lastUpdated = LocalDateTime.now();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update student profile", e);
        }
    }

    // Method to get enrolled courses
    public List<Course> getEnrolledCourses() {
        List<Course> enrolledCourses = new ArrayList<>();
        String sql = "SELECT c.courseId, c.courseName, c.credits, c.isActive, " +
                "e.startDate, e.endDate, e.grade " +
                "FROM course c " +
                "JOIN enrollment e ON c.courseId = e.courseId " +
                "WHERE e.studentId = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, this.studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(
                            rs.getInt("courseId"),
                            rs.getString("courseName"),
                            rs.getInt("credits"),
                            0,  // instructorId not needed for this method
                            rs.getBoolean("isActive")
                    );

                    // Adding additional enrollment details
                    course.setStartDate(rs.getDate("startDate").toLocalDate());
                    course.setEndDate(rs.getDate("endDate").toLocalDate());
                    course.setGrade(rs.getString("grade"));

                    enrolledCourses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve enrolled courses", e);
        }

        return enrolledCourses;
    }

    // Method to submit an assignment
    public void submitAssignment(int assignmentId, String submissionText) {
        String sql = "INSERT INTO assignmentdistribution " +
                "(assignmentId, enrollmentId, submissionText, submittedAt) " +
                "SELECT ?, enrollmentId, ?, CURRENT_TIMESTAMP " +
                "FROM enrollment " +
                "WHERE studentId = ? AND courseId = " +
                "(SELECT courseId FROM assignment WHERE assignmentId = ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assignmentId);
            pstmt.setString(2, submissionText);
            pstmt.setInt(3, this.studentId);
            pstmt.setInt(4, assignmentId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to submit assignment", e);
        }
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Inner class for Course to match Instructor's implementation
    public static class Course {
        private int courseId;
        private String courseName;
        private int credits;
        private int instructorId;
        private boolean isActive;
        private LocalDate startDate;
        private LocalDate endDate;
        private String grade;

        public Course(int courseId, String courseName, int credits, int instructorId, boolean isActive) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.credits = credits;
            this.instructorId = instructorId;
            this.isActive = isActive;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public int getCredits() {
            return credits;
        }

        public int getInstructorId() {
            return instructorId;
        }

        public boolean isActive() {
            return isActive;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }
    }
}
>>>>>>> 857e242 (update)
