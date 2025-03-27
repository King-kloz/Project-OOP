package Views;

import Utilities.AuthenticationManager;
import Utilities.DatabaseConnectionManager;
import Utilities.UserSession;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.table.TableCellRenderer;

public class StudentDashboard extends JFrame {
    private DatabaseConnectionManager dbManager;
    private UserSession userSession;
    private JTable enrolledCoursesTable;
    private DefaultTableModel tableModel;
    private DefaultTableModel assignmentsModel;
    private JTable assignmentsTable;

    public StudentDashboard() {
        this.dbManager = DatabaseConnectionManager.getInstance();
        this.userSession = UserSession.getInstance();
        setTitle("Student Dashboard - " + userSession.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(createProfilePanel(), BorderLayout.NORTH);
        mainPanel.add(createEnrolledCoursesPanel(), BorderLayout.CENTER);
        mainPanel.add(createActionPanel(), BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Student Profile"));

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT studentName, email, studentId, dateOfBirth " +
                    "FROM student WHERE studentId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        profilePanel.add(new JLabel("Name: " + rs.getString("studentName")));
                        profilePanel.add(new JLabel("Email: " + rs.getString("email")));
                        profilePanel.add(new JLabel("Student ID: " + rs.getString("studentId")));
                        profilePanel.add(new JLabel("Date of Birth: " + rs.getDate("dateOfBirth")));
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving student profile: " + e.getMessage(),
                    "Profile Error", JOptionPane.ERROR_MESSAGE);
        }
        return profilePanel;
    }

    private JScrollPane createEnrolledCoursesPanel() {
        String[] columnNames = {"Course ID", "Course Name", "Instructor", "Credits", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0);
        enrolledCoursesTable = new JTable(tableModel);
        populateEnrolledCourses();

        JScrollPane scrollPane = new JScrollPane(enrolledCoursesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Enrolled Courses"));
        return scrollPane;
    }

    private void populateEnrolledCourses() {
        tableModel.setRowCount(0);
        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT c.courseId, c.courseName, i.instructorName, c.credits, e.grade " +
                    "FROM enrollment e " +
                    "JOIN course c ON e.courseId = c.courseId " +
                    "JOIN instructor i ON c.instructorId = i.instructorId " +
                    "WHERE e.studentId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("courseId"));  // Ensure correct data type
                        row.add(rs.getString("courseName"));
                        row.add(rs.getString("instructorName"));
                        row.add(rs.getInt("credits"));
                        row.add(rs.getString("grade") != null ? rs.getString("grade") : "Not Graded"); // Handle null case
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving enrolled courses: " + e.getMessage(),
                    "Courses Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton viewGpaButton = new JButton("View GPA");
        JButton browseCoursesButton = new JButton("Browse Courses");
        JButton manageAssignmentsButton = new JButton("Manage Assignments");
        JButton logoutButton = new JButton("Logout");

        viewGpaButton.addActionListener(e -> calculateAndDisplayGPA());
        browseCoursesButton.addActionListener(e -> openCourseBrowser());
        manageAssignmentsButton.addActionListener(e -> openAssignmentManagementDialog());
        logoutButton.addActionListener(e -> logout());

        actionPanel.add(viewGpaButton);
        actionPanel.add(browseCoursesButton);
        actionPanel.add(manageAssignmentsButton);
        actionPanel.add(logoutButton);

        return actionPanel;
    }

    private void calculateAndDisplayGPA() {
        // Note: The current schema doesn't have an easy way to calculate GPA
        // as the enrollment grade is an enum, not a numeric value
        JOptionPane.showMessageDialog(this,
                "GPA calculation is not supported with current database schema.",
                "GPA Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openCourseBrowser() {
        JDialog courseBrowserDialog = new JDialog(this, "Available Courses", true);
        courseBrowserDialog.setSize(600, 400);
        courseBrowserDialog.setLocationRelativeTo(this);

        String[] columnNames = {"Course ID", "Course Name", "Instructor", "Credits", "Availability"};
        DefaultTableModel browserModel = new DefaultTableModel(columnNames, 0);
        JTable coursesTable = new JTable(browserModel);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT c.courseId, c.courseName, i.instructorName, c.credits, " +
                    "(SELECT COUNT(*) FROM enrollment e WHERE e.courseId = c.courseId) AS enrolled_count " +
                    "FROM course c " +
                    "JOIN instructor i ON c.instructorId = i.instructorId " +
                    "WHERE c.courseId NOT IN " +
                    "(SELECT courseId FROM enrollment WHERE studentId = ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getString("courseId"));
                        row.add(rs.getString("courseName"));
                        row.add(rs.getString("instructorName"));
                        row.add(rs.getInt("credits"));
                        row.add("Available");
                        browserModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(courseBrowserDialog,
                    "Error retrieving courses: " + e.getMessage(),
                    "Courses Error", JOptionPane.ERROR_MESSAGE);
        }

        courseBrowserDialog.add(new JScrollPane(coursesTable));
        courseBrowserDialog.setVisible(true);
    }

    private void logout() {
        new AuthenticationManager().logout();
        dispose();
        new LoginFrame().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentDashboard().setVisible(true);
        });
    }

    private void openAssignmentManagementDialog() {
        JDialog assignmentDialog = new JDialog(this, "Assignment Management", true);
        assignmentDialog.setSize(800, 600);
        assignmentDialog.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Available Assignments", createAvailableAssignmentsPanel());
        tabbedPane.addTab("Submitted Assignments", createSubmittedAssignmentsPanel());

        assignmentDialog.add(tabbedPane);
        assignmentDialog.setVisible(true);
    }

    private JPanel createAvailableAssignmentsPanel() {
        JPanel availableAssignmentsPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"Assignment ID", "Course", "Title", "Description", "Due Date", "Actions"};
        assignmentsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        assignmentsTable = new JTable(assignmentsModel);
        assignmentsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        assignmentsTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        populateAvailableAssignments();

        JScrollPane scrollPane = new JScrollPane(assignmentsTable);
        availableAssignmentsPanel.add(scrollPane, BorderLayout.CENTER);

        return availableAssignmentsPanel;
    }

    private void populateAvailableAssignments() {
        assignmentsModel.setRowCount(0);
        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT a.assignmentId, c.courseId, c.courseName, a.title, a.description, a.dueDate " +
                    "FROM assignment a " +
                    "JOIN assignmentdistribution ad ON a.assignmentId = ad.assignmentId " +
                    "JOIN enrollment e ON ad.enrollmentId = e.enrollmentId " +
                    "JOIN course c ON e.courseId = c.courseId " +
                    "WHERE e.studentId = ? " +
                    "AND a.isPublished = 1 " +
                    "AND ad.submittedAt IS NULL";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("assignmentId"));
                        row.add(rs.getString("courseName"));
                        row.add(rs.getString("title"));
                        row.add(rs.getString("description"));
                        row.add(rs.getDate("dueDate"));
                        row.add("Submit");
                        assignmentsModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving assignments: " + e.getMessage(),
                    "Assignments Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel createSubmittedAssignmentsPanel() {
        JPanel submittedAssignmentsPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"Assignment ID", "Course", "Title", "Submitted At", "Score", "Feedback"};
        DefaultTableModel submittedModel = new DefaultTableModel(columnNames, 0);
        JTable submittedTable = new JTable(submittedModel);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT a.assignmentId, c.courseName, a.title, " +
                    "ad.submittedAt, ad.score, ad.feedback " +
                    "FROM assignmentdistribution ad " +
                    "JOIN assignment a ON ad.assignmentId = a.assignmentId " +
                    "JOIN enrollment e ON ad.enrollmentId = e.enrollmentId " +
                    "JOIN course c ON e.courseId = c.courseId " +
                    "WHERE e.studentId = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userSession.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("assignmentId"));
                        row.add(rs.getString("courseName"));
                        row.add(rs.getString("title"));
                        row.add(rs.getTimestamp("submittedAt"));
                        row.add(rs.getBigDecimal("score"));
                        row.add(rs.getString("feedback"));
                        submittedModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving submitted assignments: " + e.getMessage(),
                    "Assignments Error", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(submittedTable);
        submittedAssignmentsPanel.add(scrollPane, BorderLayout.CENTER);

        return submittedAssignmentsPanel;
    }

    private void openAssignmentSubmissionDialog(int selectedRow) {
        int assignmentId = (int) assignmentsModel.getValueAt(selectedRow, 0);
        String courseName = (String) assignmentsModel.getValueAt(selectedRow, 2);
        String assignmentTitle = (String) assignmentsModel.getValueAt(selectedRow, 3);

        JDialog submissionDialog = new JDialog(this, "Submit Assignment", true);
        submissionDialog.setSize(500, 400);
        submissionDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel detailsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        detailsPanel.add(new JLabel("Course:"));
        detailsPanel.add(new JLabel(courseName));
        detailsPanel.add(new JLabel("Assignment:"));
        detailsPanel.add(new JLabel(assignmentTitle));

        JTextArea submissionArea = new JTextArea(10, 40);
        submissionArea.setLineWrap(true);
        submissionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(submissionArea);

        JButton submitButton = new JButton("Submit Assignment");
        submitButton.addActionListener(e -> {
            if (submitAssignment(assignmentId, submissionArea.getText())) {
                JOptionPane.showMessageDialog(submissionDialog,
                        "Assignment submitted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                submissionDialog.dispose();
                populateAvailableAssignments();
            }
        });

        panel.add(detailsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.SOUTH);

        submissionDialog.add(panel);
        submissionDialog.setVisible(true);
    }

    private boolean submitAssignment(int assignmentId, String submissionText) {
        try {
            Connection conn = dbManager.getConnection();

            // Find the corresponding enrollment ID
            String findEnrollmentQuery = "SELECT enrollmentId FROM enrollment " +
                    "WHERE studentId = ? AND courseId = (" +
                    "    SELECT courseId FROM assignment WHERE assignmentId = ?" +
                    ")";
            int enrollmentId;

            try (PreparedStatement findStmt = conn.prepareStatement(findEnrollmentQuery)) {
                findStmt.setInt(1, userSession.getUserId());
                findStmt.setInt(2, assignmentId);

                try (ResultSet rs = findStmt.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "No valid enrollment found for this assignment.",
                                "Submission Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    enrollmentId = rs.getInt("enrollmentId");
                }
            }

            // Insert submission into assignmentdistribution
            String insertQuery = "INSERT INTO assignmentdistribution " +
                    "(assignmentId, enrollmentId, submissionText, submittedAt, isGraded) " +
                    "VALUES (?, ?, ?, CURRENT_TIMESTAMP, 0)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setInt(1, assignmentId);
                pstmt.setInt(2, enrollmentId);
                pstmt.setString(3, submissionText);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error submitting assignment: " + ex.getMessage(),
                    "Submission Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Button Renderer and Editor classes remain the same as in the original code
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> openAssignmentSubmissionDialog(selectedRow));
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedRow = row;
            button.setText((value == null) ? "" : value.toString());
            return button;
        }

        public Object getCellEditorValue() {
            return "Submit";
        }
    }
}