package Views;

import Utilities.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AdminDashboard extends JFrame {
    private DatabaseConnectionManager dbManager;
    private UserSession userSession;
    private JTabbedPane tabbedPane;

    public AdminDashboard() {
        this.dbManager = DatabaseConnectionManager.getInstance();
        this.userSession = UserSession.getInstance();

        setTitle("Administrator Dashboard - " + userSession.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Students Management", createStudentsPanel());
        tabbedPane.addTab("Instructors Management", createInstructorsPanel());
        tabbedPane.addTab("Courses Management", createCoursesPanel());
        tabbedPane.addTab("Enrollments", createEnrollmentsPanel());
        tabbedPane.addTab("System Analytics", createAnalyticsPanel());

        add(tabbedPane);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createStudentsPanel() {
        JPanel studentsPanel = new JPanel(new BorderLayout());

        String[] columnNames = {
                "Student ID", "Name", "Email", "Date of Birth", "Active Status"
        };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable studentsTable = new JTable(tableModel);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT studentId, studentName, email, dateOfBirth, isActive " +
                    "FROM student";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("studentId"));
                    row.add(rs.getString("studentName"));
                    row.add(rs.getString("email"));
                    row.add(rs.getDate("dateOfBirth"));
                    row.add(rs.getBoolean("isActive"));

                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving students: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JPanel buttonPanel = new JPanel();
        JButton addStudentButton = new JButton("Add Student");
        addStudentButton.addActionListener(e -> openAddStudentDialog());
        buttonPanel.add(addStudentButton);

        studentsPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        studentsPanel.add(buttonPanel, BorderLayout.SOUTH);

        return studentsPanel;
    }

    private void openAddStudentDialog() {
        JDialog addStudentDialog = new JDialog(this, "Add New Student", true);
        addStudentDialog.setSize(400, 300);
        addStudentDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        dialogPanel.add(nameField);

        dialogPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        dialogPanel.add(emailField);

        dialogPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        JTextField dobField = new JTextField();
        dialogPanel.add(dobField);

        dialogPanel.add(new JLabel("Initial Password:"));
        JPasswordField passwordField = new JPasswordField();
        dialogPanel.add(passwordField);

        dialogPanel.add(new JLabel("Active:"));
        JCheckBox activeCheckBox = new JCheckBox();
        activeCheckBox.setSelected(true);
        dialogPanel.add(activeCheckBox);

        JButton saveButton = new JButton("Save Student");
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String dob = dobField.getText().trim();
            String password = new String(passwordField.getPassword());
            boolean isActive = activeCheckBox.isSelected();

            if (name.isEmpty() || email.isEmpty() || dob.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(addStudentDialog,
                        "Please fill in all fields.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Connection conn = dbManager.getConnection();
                String insertQuery = "INSERT INTO student (studentName, email, dateOfBirth, passwordHash, isActive) " +
                        "VALUES (?, ?, ?, ?, ?)";

                String hashedPassword = new AuthenticationManager().hashPassword(password);

                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setDate(3, Date.valueOf(dob));
                    pstmt.setString(4, hashedPassword);
                    pstmt.setBoolean(5, isActive);

                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(addStudentDialog,
                        "Student added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                addStudentDialog.dispose();
                tabbedPane.setComponentAt(0, createStudentsPanel());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addStudentDialog,
                        "Error adding student: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        dialogPanel.add(saveButton);

        addStudentDialog.add(dialogPanel);
        addStudentDialog.setVisible(true);
    }

    private JPanel createInstructorsPanel() {
        JPanel instructorsPanel = new JPanel(new BorderLayout());

        String[] columnNames = {
                "Instructor ID", "Name", "Email", "Department", "Active Status"
        };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable instructorsTable = new JTable(tableModel);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT instructorId, instructorName, email, department, isActive " +
                    "FROM instructor";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("instructorId"));
                    row.add(rs.getString("instructorName"));
                    row.add(rs.getString("email"));
                    row.add(rs.getString("department"));
                    row.add(rs.getBoolean("isActive"));

                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving instructors: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JPanel buttonPanel = new JPanel();
        JButton addInstructorButton = new JButton("Add Instructor");
        addInstructorButton.addActionListener(e -> openAddInstructorDialog());
        buttonPanel.add(addInstructorButton);

        instructorsPanel.add(new JScrollPane(instructorsTable), BorderLayout.CENTER);
        instructorsPanel.add(buttonPanel, BorderLayout.SOUTH);

        return instructorsPanel;
    }

    private void openAddInstructorDialog() {
        JDialog addInstructorDialog = new JDialog(this, "Add New Instructor", true);
        addInstructorDialog.setSize(400, 300);
        addInstructorDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        dialogPanel.add(nameField);

        dialogPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        dialogPanel.add(emailField);

        dialogPanel.add(new JLabel("Department:"));
        JTextField departmentField = new JTextField();
        dialogPanel.add(departmentField);

        dialogPanel.add(new JLabel("Initial Password:"));
        JPasswordField passwordField = new JPasswordField();
        dialogPanel.add(passwordField);

        dialogPanel.add(new JLabel("Active:"));
        JCheckBox activeCheckBox = new JCheckBox();
        activeCheckBox.setSelected(true);
        dialogPanel.add(activeCheckBox);

        JButton saveButton = new JButton("Save Instructor");
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String department = departmentField.getText().trim();
            String password = new String(passwordField.getPassword());
            boolean isActive = activeCheckBox.isSelected();

            if (name.isEmpty() || email.isEmpty() || department.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(addInstructorDialog,
                        "Please fill in all fields.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Connection conn = dbManager.getConnection();
                String insertQuery = "INSERT INTO instructor (instructorName, email, department, passwordHash, isActive) " +
                        "VALUES (?, ?, ?, ?, ?)";

                String hashedPassword = new AuthenticationManager().hashPassword(password);

                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setString(3, department);
                    pstmt.setString(4, hashedPassword);
                    pstmt.setBoolean(5, isActive);

                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(addInstructorDialog,
                        "Instructor added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                addInstructorDialog.dispose();
                tabbedPane.setComponentAt(1, createInstructorsPanel());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addInstructorDialog,
                        "Error adding instructor: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        dialogPanel.add(saveButton);

        addInstructorDialog.add(dialogPanel);
        addInstructorDialog.setVisible(true);
    }

    private JPanel createCoursesPanel() {
        JPanel coursesPanel = new JPanel(new BorderLayout());

        String[] columnNames = {
                "Course ID", "Course Name", "Credits", "Instructor", "Department"
        };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable coursesTable = new JTable(tableModel);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT c.courseId, c.courseName, c.credits, " +
                    "i.instructorName, i.department " +
                    "FROM course c " +
                    "LEFT JOIN instructor i ON c.instructorId = i.instructorId";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("courseId"));
                    row.add(rs.getString("courseName"));
                    row.add(rs.getInt("credits"));
                    row.add(rs.getString("instructorName"));
                    row.add(rs.getString("department"));

                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving courses: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JPanel buttonPanel = new JPanel();
        JButton addCourseButton = new JButton("Add Course");
        addCourseButton.addActionListener(e -> openAddCourseDialog());
        buttonPanel.add(addCourseButton);

        coursesPanel.add(new JScrollPane(coursesTable), BorderLayout.CENTER);
        coursesPanel.add(buttonPanel, BorderLayout.SOUTH);

        return coursesPanel;
    }

    private void openAddCourseDialog() {
        JDialog addCourseDialog = new JDialog(this, "Add New Course", true);
        addCourseDialog.setSize(400, 300);
        addCourseDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("Course Name:"));
        JTextField courseNameField = new JTextField();
        dialogPanel.add(courseNameField);

        dialogPanel.add(new JLabel("Credits:"));
        JTextField creditsField = new JTextField();
        dialogPanel.add(creditsField);

        dialogPanel.add(new JLabel("Instructor:"));
        JComboBox<String> instructorComboBox = new JComboBox<>();
        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT instructorId, instructorName FROM instructor";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    instructorComboBox.addItem(
                            rs.getString("instructorId") + " - " +
                                    rs.getString("instructorName")
                    );
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving instructors: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        dialogPanel.add(instructorComboBox);

        JButton saveButton = new JButton("Save Course");
        saveButton.addActionListener(e -> {
            String courseName = courseNameField.getText().trim();
            String creditsStr = creditsField.getText().trim();
            String selectedInstructor = (String) instructorComboBox.getSelectedItem();

            if (courseName.isEmpty() || creditsStr.isEmpty() ||
                    selectedInstructor == null) {
                JOptionPane.showMessageDialog(addCourseDialog,
                        "Please fill in all fields.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int credits = Integer.parseInt(creditsStr);
                String instructorId = selectedInstructor.split(" - ")[0];

                Connection conn = dbManager.getConnection();
                String insertQuery = "INSERT INTO course (courseName, credits, instructorId) " +
                        "VALUES (?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, courseName);
                    pstmt.setInt(2, credits);
                    pstmt.setString(3, instructorId);

                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(addCourseDialog,
                        "Course added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                addCourseDialog.dispose();
                tabbedPane.setComponentAt(2, createCoursesPanel());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addCourseDialog,
                        "Invalid credits. Please enter a number.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addCourseDialog,
                        "Error adding course: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        dialogPanel.add(saveButton);

        addCourseDialog.add(dialogPanel);
        addCourseDialog.setVisible(true);
    }

    private JPanel createEnrollmentsPanel() {
        JPanel enrollmentsPanel = new JPanel(new BorderLayout());

        String[] columnNames = {
                "Enrollment ID", "Student Name", "Course Name", "Start Date", "End Date"
        };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable enrollmentsTable = new JTable(tableModel);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT e.enrollmentId, s.studentName, c.courseName, e.startDate, e.endDate " +
                    "FROM enrollment e " +
                    "JOIN student s ON e.studentId = s.studentId " +
                    "JOIN course c ON e.courseId = c.courseId";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("enrollmentId"));
                    row.add(rs.getString("studentName"));
                    row.add(rs.getString("courseName"));
                    row.add(rs.getDate("startDate"));
                    row.add(rs.getDate("endDate"));

                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving enrollments: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JPanel buttonPanel = new JPanel();
        JButton addEnrollmentButton = new JButton("Enroll Student");
        addEnrollmentButton.addActionListener(e -> openEnrollStudentDialog());
        buttonPanel.add(addEnrollmentButton);

        enrollmentsPanel.add(new JScrollPane(enrollmentsTable), BorderLayout.CENTER);
        enrollmentsPanel.add(buttonPanel, BorderLayout.SOUTH);

        return enrollmentsPanel;
    }

    private void openEnrollStudentDialog() {
        JDialog enrollStudentDialog = new JDialog(this, "Enroll Student", true);
        enrollStudentDialog.setSize(400, 300);
        enrollStudentDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("Student:"));
        JComboBox<String> studentComboBox = new JComboBox<>();
        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT studentId, studentName FROM student";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    studentComboBox.addItem(
                            rs.getString("studentId") + " - " +
                                    rs.getString("studentName")
                    );
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving students: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        dialogPanel.add(studentComboBox);

        dialogPanel.add(new JLabel("Course:"));
        JComboBox<String> courseComboBox = new JComboBox<>();
        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT courseId, courseName FROM course";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    courseComboBox.addItem(
                            rs.getString("courseId") + " - " +
                                    rs.getString("courseName")
                    );
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving courses: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        dialogPanel.add(courseComboBox);

        dialogPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        JTextField startDateField = new JTextField();
        dialogPanel.add(startDateField);

        dialogPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        JTextField endDateField = new JTextField();
        dialogPanel.add(endDateField);

        JButton enrollButton = new JButton("Enroll");
        enrollButton.addActionListener(e -> {
            String selectedStudent = (String) studentComboBox.getSelectedItem();
            String selectedCourse = (String) courseComboBox.getSelectedItem();
            String startDate = startDateField.getText().trim();
            String endDate = endDateField.getText().trim();

            if (selectedStudent == null || selectedCourse == null ||
                    startDate.isEmpty() || endDate.isEmpty()) {
                JOptionPane.showMessageDialog(enrollStudentDialog,
                        "Please fill in all fields.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String studentId = selectedStudent.split(" - ")[0];
                String courseId = selectedCourse.split(" - ")[0];

                Connection conn = dbManager.getConnection();
                String insertQuery = "INSERT INTO enrollment (studentId, courseId, startDate, endDate) " +
                        "VALUES (?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, studentId);
                    pstmt.setString(2, courseId);
                    pstmt.setDate(3, Date.valueOf(startDate));
                    pstmt.setDate(4, Date.valueOf(endDate));

                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(enrollStudentDialog,
                        "Student enrolled successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                enrollStudentDialog.dispose();
                tabbedPane.setComponentAt(3, createEnrollmentsPanel());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(enrollStudentDialog,
                        "Error enrolling student: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        dialogPanel.add(enrollButton);

        enrollStudentDialog.add(dialogPanel);
        enrollStudentDialog.setVisible(true);
    }

    private JPanel createAnalyticsPanel() {
        JPanel analyticsPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton comprehensiveReportButton = new JButton("Comprehensive Analytics Report");
        comprehensiveReportButton.addActionListener(e -> {
            ReportGenerator reportGenerator = new ReportGeneratorImpl();
            reportGenerator.generateDetailedReport();
        });

        buttonPanel.add(comprehensiveReportButton);

        analyticsPanel.add(buttonPanel, BorderLayout.NORTH);

        return analyticsPanel;
    }

    private void logout() {
        this.dispose();

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminDashboard().setVisible(true);
        });
    }
}