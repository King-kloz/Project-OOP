package Views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Map;

import Utilities.DatabaseConnectionManager;
import Utilities.ReportGenerator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class AnalyticsPanel extends JPanel implements ReportGenerator {
    private DatabaseConnectionManager dbManager;
    private JTable gradeTable;
    private DefaultTableModel tableModel;
    private JTabbedPane analyticsPane;

    public AnalyticsPanel(DatabaseConnectionManager dbManager) {
        this.dbManager = dbManager;

        setLayout(new BorderLayout());

        analyticsPane = new JTabbedPane();

        initializeGradeTable();

        add(createButtonPanel(), BorderLayout.SOUTH);
        add(analyticsPane, BorderLayout.CENTER);
    }

    private void initializeGradeTable() {
        String[] columnNames = {
                "Course Name", "Average Score", "Grade",
                "Models.Student Count", "Models.Instructor"
        };

        tableModel = new DefaultTableModel(columnNames, 0);

        gradeTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(gradeTable);
        analyticsPane.addTab("Grade Summary", scrollPane);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();

        JButton barChartButton = new JButton("Generate Bar Chart");
        JButton pieChartButton = new JButton("Generate Pie Chart");
        JButton refreshButton = new JButton("Refresh Data");

        barChartButton.addActionListener(e -> generateBarChart(null));
        pieChartButton.addActionListener(e -> generatePieChart(null));
        refreshButton.addActionListener(e -> displayGradeTable());

        buttonPanel.add(barChartButton);
        buttonPanel.add(pieChartButton);
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    public void displayGradeTable() {
        tableModel.setRowCount(0);

        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT " +
                    "c.course_name, " +
                    "AVG(e.grade) as average_score, " +
                    "CASE " +
                    "    WHEN AVG(e.grade) >= 90 THEN 'A' " +
                    "    WHEN AVG(e.grade) >= 80 THEN 'B' " +
                    "    WHEN AVG(e.grade) >= 70 THEN 'C' " +
                    "    WHEN AVG(e.grade) >= 60 THEN 'D' " +
                    "    ELSE 'F' " +
                    "END as grade_letter, " +
                    "COUNT(DISTINCT e.student_id) as student_count, " +
                    "i.instructor_name " +
                    "FROM courses c " +
                    "JOIN enrollments e ON c.course_id = e.course_id " +
                    "JOIN instructors i ON c.instructor_id = i.instructor_id " +
                    "GROUP BY c.course_id, c.course_name, i.instructor_name";

            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Object[] row = {
                            rs.getString("course_name"),
                            String.format("%.2f", rs.getDouble("average_score")),
                            rs.getString("grade_letter"),
                            rs.getInt("student_count"),
                            rs.getString("instructor_name")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving grade data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void generateBarChart(Map<String, Object> data) {
        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT c.course_name, COUNT(DISTINCT e.student_id) as enrollment_count " +
                    "FROM courses c " +
                    "LEFT JOIN enrollments e ON c.course_id = e.course_id " +
                    "GROUP BY c.course_id, c.course_name";

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    dataset.addValue(
                            rs.getInt("enrollment_count"),
                            "Enrollments",
                            rs.getString("course_name")
                    );
                }
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Course Enrollment Statistics",  // Chart title
                    "Courses",                       // X-Axis Label
                    "Number of Students",            // Y-Axis Label
                    dataset,                         // Dataset
                    PlotOrientation.VERTICAL,        // Plot Orientation
                    false, true, false              // Show Legend, Tooltips, URLs
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(560, 370));

            int barChartTabIndex = analyticsPane.indexOfTab("Enrollment Bar Chart");
            if (barChartTabIndex != -1) {
                analyticsPane.removeTabAt(barChartTabIndex);
            }

            analyticsPane.addTab("Enrollment Bar Chart", chartPanel);
            analyticsPane.setSelectedComponent(chartPanel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error generating bar chart: " + e.getMessage(),
                    "Chart Generation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void generatePieChart(Map<String, Object> data) {
        try {
            Connection conn = dbManager.getConnection();
            String query = "SELECT " +
                    "CASE " +
                    "    WHEN grade >= 90 THEN 'A' " +
                    "    WHEN grade >= 80 THEN 'B' " +
                    "    WHEN grade >= 70 THEN 'C' " +
                    "    WHEN grade >= 60 THEN 'D' " +
                    "    ELSE 'F' " +
                    "END as grade_letter, " +
                    "COUNT(*) as grade_count " +
                    "FROM enrollments " +
                    "GROUP BY grade_letter";

            DefaultPieDataset dataset = new DefaultPieDataset();

            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    dataset.setValue(
                            rs.getString("grade_letter"),
                            rs.getInt("grade_count")
                    );
                }
            }

            JFreeChart chart = ChartFactory.createPieChart(
                    "Grade Distribution",    // Chart title
                    dataset,                 // Dataset
                    true,                    // Include legend
                    true,                    // Tooltips
                    false                    // URLs
            );

            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setCircular(true);
            plot.setLabelGenerator(null);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(560, 370));

            int pieChartTabIndex = analyticsPane.indexOfTab("Grade Distribution Pie Chart");
            if (pieChartTabIndex != -1) {
                analyticsPane.removeTabAt(pieChartTabIndex);
            }

            analyticsPane.addTab("Grade Distribution Pie Chart", chartPanel);
            analyticsPane.setSelectedComponent(chartPanel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error generating pie chart: " + e.getMessage(),
                    "Chart Generation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void generateDetailedReport() {
        JPanel reportPanel = new JPanel(new BorderLayout());

        displayGradeTable();

        generateBarChart(null);

        generatePieChart(null);

        JTextArea summaryTextArea = new JTextArea();
        summaryTextArea.setEditable(false);

        try {
            Connection conn = dbManager.getConnection();
            String summaryQuery = "SELECT " +
                    "COUNT(DISTINCT course_id) as total_courses, " +
                    "COUNT(DISTINCT student_id) as total_students, " +
                    "AVG(grade) as overall_average_grade " +
                    "FROM courses c " +
                    "JOIN enrollments e ON c.course_id = e.course_id";

            try (PreparedStatement pstmt = conn.prepareStatement(summaryQuery);
                 ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    summaryTextArea.append("Comprehensive Course Analytics Report\n\n");
                    summaryTextArea.append(String.format("Total Courses: %d\n", rs.getInt("total_courses")));
                    summaryTextArea.append(String.format("Total Students: %d\n", rs.getInt("total_students")));
                    summaryTextArea.append(String.format("Overall Average Grade: %.2f\n", rs.getDouble("overall_average_grade")));
                }
            }
        } catch (SQLException e) {
            summaryTextArea.append("Error generating summary: " + e.getMessage());
        }

        reportPanel.add(new JScrollPane(summaryTextArea), BorderLayout.NORTH);

        analyticsPane.addTab("Detailed Report", reportPanel);
        analyticsPane.setSelectedComponent(reportPanel);
    }
}