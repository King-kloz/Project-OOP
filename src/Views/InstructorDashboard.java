package Views;

import Utilities.AuthenticationManager;
import Utilities.DatabaseConnectionManager;
import Utilities.UserSession;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class InstructorDashboard extends JFrame {
    private DatabaseConnectionManager dbManager;
    private UserSession userSession;
    private JTable coursesTable;
    private DefaultTableModel tableModel;
    private JTable assignmentsTable;
    private DefaultTableModel assignmentsTableModel;
    private static final String[] GRADE_OPTIONS = {
            "", "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"
    };

    public InstructorDashboard() {
        this.dbManager = DatabaseConnectionManager.getInstance();
        this.userSession = UserSession.getInstance();
        setTitle("Instructor Dashboard - " + userSession.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(createProfilePanel(), BorderLayout.NORTH);
        mainPanel.add(createManagedCoursesPanel(), BorderLayout.CENTER);
        mainPanel.add(createActionPanel(), BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Instructor Profile"));

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT instructorName, email, instructorId, department " +
                    "FROM instructor WHERE instructorId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        profilePanel.add(new JLabel("Name: " + rs.getString("instructorName")));
                        profilePanel.add(new JLabel("Email: " + rs.getString("email")));
                        profilePanel.add(new JLabel("Instructor ID: " + rs.getString("instructorId")));
                        profilePanel.add(new JLabel("Department: " + rs.getString("department")));
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving instructor profile: " + e.getMessage(),
                    "Profile Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return profilePanel;
    }

    private JScrollPane createManagedCoursesPanel() {
        String[] columnNames = { "Course ID", "Course Name", "Credits", "Enrolled Students", "Average Grade" };
        tableModel = new DefaultTableModel(columnNames, 0);
        coursesTable = new JTable(tableModel);
        populateManagedCourses();

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Managed Courses"));
        return scrollPane;
    }

    private void populateManagedCourses() {
        tableModel.setRowCount(0);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT courseId, courseName, credits, " +
                    "(SELECT COUNT(*) FROM enrollment e WHERE e.courseId = course.courseId) AS enrolled_students, " +
                    "(SELECT AVG(CASE " +
                    "    WHEN grade = 'A+' THEN 4.0 " +
                    "    WHEN grade = 'A' THEN 4.0 " +
                    "    WHEN grade = 'A-' THEN 3.7 " +
                    "    WHEN grade = 'B+' THEN 3.3 " +
                    "    WHEN grade = 'B' THEN 3.0 " +
                    "    WHEN grade = 'B-' THEN 2.7 " +
                    "    WHEN grade = 'C+' THEN 2.3 " +
                    "    WHEN grade = 'C' THEN 2.0 " +
                    "    WHEN grade = 'C-' THEN 1.7 " +
                    "    WHEN grade = 'D+' THEN 1.3 " +
                    "    WHEN grade = 'D' THEN 1.0 " +
                    "    WHEN grade = 'D-' THEN 0.7 " +
                    "    WHEN grade = 'F' THEN 0.0 " +
                    "    ELSE NULL END) FROM enrollment e WHERE e.courseId = course.courseId) AS average_grade " +
                    "FROM course " +
                    "WHERE instructorId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getString("courseId"));
                        row.add(rs.getString("courseName"));
                        row.add(rs.getInt("credits"));
                        row.add(rs.getInt("enrolled_students"));

                        Double avgGrade = rs.getDouble("average_grade");
                        row.add(avgGrade != null ? String.format("%.2f", avgGrade) : "N/A");

                        tableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving managed courses: " + e.getMessage(),
                    "Courses Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openGradeManagementDialog() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to manage grades.",
                    "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) tableModel.getValueAt(selectedRow, 0);
        String courseName = (String) tableModel.getValueAt(selectedRow, 1);

        JDialog gradeManagementDialog = new JDialog(this, "Manage Grades - " + courseName, true);
        gradeManagementDialog.setSize(600, 400);
        gradeManagementDialog.setLocationRelativeTo(this);

        String[] columnNames = { "Student ID", "Student Name", "Current Grade" };
        DefaultTableModel gradeModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return String.class;
                return super.getColumnClass(columnIndex);
            }
        };

        JTable gradesTable = new JTable(gradeModel);
        JComboBox<String> gradeComboBox = new JComboBox<>(GRADE_OPTIONS);

        TableColumn gradeColumn = gradesTable.getColumnModel().getColumn(2);
        gradeColumn.setCellEditor(new DefaultCellEditor(gradeComboBox));

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT s.studentId, s.studentName, e.grade " +
                    "FROM student s " +
                    "JOIN enrollment e ON s.studentId = e.studentId " +
                    "WHERE e.courseId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, courseId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getString("studentId"));
                        row.add(rs.getString("studentName"));

                        String grade = rs.getString("grade");
                        row.add(grade != null ? grade : "");

                        gradeModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(gradeManagementDialog,
                    "Error retrieving student grades: " + e.getMessage(),
                    "Grades Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JButton saveGradesButton = new JButton("Save Grades");
        saveGradesButton.addActionListener(e -> saveStudentGrades(courseId, gradeModel));

        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(new JScrollPane(gradesTable), BorderLayout.CENTER);
        dialogPanel.add(saveGradesButton, BorderLayout.SOUTH);

        gradeManagementDialog.add(dialogPanel);
        gradeManagementDialog.setVisible(true);
    }

    private void saveStudentGrades(String courseId, DefaultTableModel gradeModel) {
        try {
            Connection conn = dbManager.getConnection();
            String updateQuery = "UPDATE enrollment SET grade = ?, gradeUpdatedAt = CURRENT_TIMESTAMP " +
                    "WHERE studentId = ? AND courseId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                for (int row = 0; row < gradeModel.getRowCount(); row++) {
                    String studentId = (String) gradeModel.getValueAt(row, 0);
                    String grade = (String) gradeModel.getValueAt(row, 2);

                    if (grade != null && !grade.isEmpty()) {
                        pstmt.setString(1, grade);
                        pstmt.setString(2, studentId);
                        pstmt.setString(3, courseId);
                        pstmt.executeUpdate();
                    }
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Grades saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving grades: " + e.getMessage(),
                    "Grades Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCreateCourseDialog() {
        JDialog createCourseDialog = new JDialog(this, "Create New Course", true);
        createCourseDialog.setSize(400, 300);
        createCourseDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialogPanel.add(new JLabel("Course Name:"));
        JTextField courseNameField = new JTextField();
        dialogPanel.add(courseNameField);

        dialogPanel.add(new JLabel("Credits:"));
        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        dialogPanel.add(creditsSpinner);

        JButton createButton = new JButton("Create Course");
        createButton.addActionListener(e -> {
            String courseName = courseNameField.getText().trim();
            int credits = (Integer) creditsSpinner.getValue();

            if (courseName.isEmpty()) {
                JOptionPane.showMessageDialog(createCourseDialog,
                        "Please fill in course name.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Connection conn = dbManager.getConnection();
                String insertQuery = "INSERT INTO course (courseName, credits, instructorId) " +
                        "VALUES (?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, courseName);
                    pstmt.setInt(2, credits);
                    pstmt.setInt(3, userSession.getUserId());
                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(createCourseDialog,
                        "Course created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                populateManagedCourses();
                createCourseDialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(createCourseDialog,
                        "Error creating course: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogPanel.add(createButton);
        createCourseDialog.add(dialogPanel);
        createCourseDialog.setVisible(true);
    }

    private void openAssignmentManagementDialog() {
        JDialog assignmentManagementDialog = new JDialog(this, "Manage Assignments", true);
        assignmentManagementDialog.setSize(800, 600);
        assignmentManagementDialog.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Assignments List", createAssignmentsListPanel());
        tabbedPane.addTab("Create New Assignment", createNewAssignmentPanel(assignmentManagementDialog));

        assignmentManagementDialog.add(tabbedPane);
        assignmentManagementDialog.setVisible(true);
    }

    private JPanel createAssignmentsListPanel() {
        JPanel assignmentsPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"Assignment ID", "Title", "Description", "Due Date", "Published"};
        assignmentsTableModel = new DefaultTableModel(columnNames, 0);
        assignmentsTable = new JTable(assignmentsTableModel);

        populateAssignmentsList();

        JScrollPane scrollPane = new JScrollPane(assignmentsTable);
        assignmentsPanel.add(scrollPane, BorderLayout.CENTER);

        return assignmentsPanel;
    }

    private void populateAssignmentsList() {
        assignmentsTableModel.setRowCount(0);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT a.assignmentId, a.title, a.description, a.dueDate, a.isPublished " +
                    "FROM assignment a " +
                    "JOIN course c ON a.courseId = c.courseId " +  // Directly join with course
                    "WHERE c.instructorId = ?";  // Filter by instructor

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("assignmentId"));
                        row.add(rs.getString("title"));
                        row.add(rs.getString("description"));
                        row.add(rs.getDate("dueDate"));
                        row.add(rs.getBoolean("isPublished") ? "Yes" : "No");

                        assignmentsTableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving assignments: " + e.getMessage(),
                    "Assignments Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createNewAssignmentPanel(JDialog parentDialog) {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Select Course:"));
        JComboBox<String> courseComboBox = new JComboBox<>();
        populateCourseComboBox(courseComboBox);
        panel.add(courseComboBox);

        panel.add(new JLabel("Assignment Title:"));
        JTextField titleField = new JTextField();
        panel.add(titleField);

        panel.add(new JLabel("Description:"));
        JTextArea descriptionArea = new JTextArea();
        panel.add(new JScrollPane(descriptionArea));

        panel.add(new JLabel("Due Date:"));
        JTextField dueDateField = new JTextField("YYYY-MM-DD");
        panel.add(dueDateField);

        panel.add(new JLabel("Publish Assignment:"));
        JCheckBox publishCheckBox = new JCheckBox();
        panel.add(publishCheckBox);

        JButton createButton = new JButton("Create Assignment");
        createButton.addActionListener(e -> {
            Integer selectedCourseId = (Integer) courseComboBox.getClientProperty(courseComboBox.getSelectedItem());
            if (selectedCourseId != null && createAssignment(
                    selectedCourseId,  // Pass as an integer
                    titleField.getText(),
                    descriptionArea.getText(),
                    dueDateField.getText(),
                    publishCheckBox.isSelected())) {

                JOptionPane.showMessageDialog(parentDialog,
                        "Assignment created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                populateAssignmentsList();
                titleField.setText("");
                descriptionArea.setText("");
                dueDateField.setText("YYYY-MM-DD");
                publishCheckBox.setSelected(false);
            }
        });

        panel.add(createButton);
        return panel;
    }

    private void populateCourseComboBox(JComboBox<String> courseComboBox) {
        courseComboBox.removeAllItems();

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT courseId, courseName FROM course WHERE instructorId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int courseId = rs.getInt("courseId");
                        String courseName = rs.getString("courseName");

                        // Create a custom item that displays the course name but stores the courseId
                        courseComboBox.addItem(courseName);

                        // You might want to store the courseId as a client property
                        courseComboBox.putClientProperty(courseName, courseId);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving courses: " + e.getMessage(),
                    "Courses Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean createAssignment(int courseId, String title, String description, String dueDate, boolean isPublished) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int assignmentId = -1;

        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Insert the new assignment
            String insertAssignmentQuery = "INSERT INTO assignment (courseId, title, description, dueDate, isPublished) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertAssignmentQuery, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, courseId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setDate(4, Date.valueOf(dueDate));
            pstmt.setBoolean(5, isPublished);
            pstmt.executeUpdate();

            // Get the generated assignmentId
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                assignmentId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated assignmentId.");
            }

            // Step 2: Assign this assignment to all enrolled students
            String enrollmentsQuery = "SELECT enrollmentId FROM enrollment WHERE courseId = ?";
            pstmt = conn.prepareStatement(enrollmentsQuery);
            pstmt.setInt(1, courseId);
            rs = pstmt.executeQuery();

            String insertAssignmentDistQuery = "INSERT INTO assignmentdistribution (assignmentId, enrollmentId) VALUES (?, ?)";
            pstmt = conn.prepareStatement(insertAssignmentDistQuery);

            while (rs.next()) {
                int enrollmentId = rs.getInt("enrollmentId");
                pstmt.setInt(1, assignmentId);
                pstmt.setInt(2, enrollmentId);
                pstmt.executeUpdate();
            }

            // Step 3: Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback if there's an error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(null,
                    "Error creating assignment: " + e.getMessage(),
                    "Assignment Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.setAutoCommit(true); // Reset auto-commit mode
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void logout() {
        new AuthenticationManager().logout();
        dispose();
        new LoginFrame().setVisible(true);
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton manageGradesButton = new JButton("Manage Grades");
        JButton createCourseButton = new JButton("Create Course");
        JButton manageAssignmentsButton = new JButton("Manage Assignments");
        JButton logoutButton = new JButton("Logout");

        manageGradesButton.addActionListener(e -> openGradeManagementDialog());
        createCourseButton.addActionListener(e -> openCreateCourseDialog());
        manageAssignmentsButton.addActionListener(e -> openAssignmentManagementDialog());
        logoutButton.addActionListener(e -> logout());

        actionPanel.add(manageGradesButton);
        actionPanel.add(createCourseButton);
        actionPanel.add(manageAssignmentsButton);
        actionPanel.add(logoutButton);

        return actionPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InstructorDashboard().setVisible(true);
        });
    }
}