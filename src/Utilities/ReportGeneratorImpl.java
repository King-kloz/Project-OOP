package Utilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportGeneratorImpl implements ReportGenerator {
    private CourseAnalytics courseAnalytics;

    public ReportGeneratorImpl() {
        this.courseAnalytics = new CourseAnalyticsImpl();
    }

    @Override
    public void generateBarChart(Map<String, Object> data) {
        Map<String, Integer> courseEnrollments = (Map<String, Integer>) data.get("courseEnrollments");

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        courseEnrollments.forEach((courseName, enrollments) ->
                dataset.addValue(enrollments, "Enrollments", courseName)
        );

        JFreeChart barChart = ChartFactory.createBarChart(
                "Course Enrollments",
                "Courses",
                "Number of Students",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(560, 370));

        JFrame chartFrame = new JFrame("Course Enrollment Chart");
        chartFrame.setContentPane(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setVisible(true);
    }

    @Override
    public void generatePieChart(Map<String, Object> data) {
        Map<String, Integer> gradeDistribution = (Map<String, Integer>) data.get("gradeDistribution");

        DefaultPieDataset dataset = new DefaultPieDataset();
        gradeDistribution.forEach(dataset::setValue);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Grade Distribution",
                dataset,
                true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(560, 370));

        JFrame chartFrame = new JFrame("Grade Distribution Chart");
        chartFrame.setContentPane(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setVisible(true);
    }

    @Override
    public void generateDetailedReport() {
        // Create a tabbed pane to hold different analytics views
        JFrame reportFrame = new JFrame("Comprehensive University Analytics");
        JTabbedPane tabbedPane = new JTabbedPane();

        // Enrollment Bar Chart
        Map<String, Object> enrollmentStats = courseAnalytics.generateEnrollmentStatistics();
        generateBarChart(enrollmentStats);

        // Grade Distribution Pie Chart
        Map<String, Object> gradeDistribution = courseAnalytics.generateGradeDistribution();
        generatePieChart(gradeDistribution);

        // Average Grades by Course Table
        List<Map<String, Object>> averageGrades = courseAnalytics.getAverageGradesByCourse();
        String[] averageGradeColumns = {"Course Name", "Average GPA", "Total Students"};
        DefaultTableModel averageGradeModel = new DefaultTableModel(averageGradeColumns, 0);
        for (Map<String, Object> courseGrade : averageGrades) {
            averageGradeModel.addRow(new Object[]{
                    courseGrade.get("courseName"),
                    String.format("%.2f", (Double) courseGrade.get("averageGPA")),
                    courseGrade.get("totalStudents")
            });
        }
        JTable averageGradeTable = new JTable(averageGradeModel);
        tabbedPane.addTab("Average Grades", new JScrollPane(averageGradeTable));

        // Student Course Marks Table
        List<Map<String, Object>> studentMarks = ((CourseAnalyticsImpl)courseAnalytics).getStudentCourseMarks();
        String[] studentMarksColumns = {"Student Name", "Course Name", "Grade"};
        DefaultTableModel studentMarksModel = new DefaultTableModel(studentMarksColumns, 0);
        for (Map<String, Object> studentCourseGrade : studentMarks) {
            studentMarksModel.addRow(new Object[]{
                    studentCourseGrade.get("studentName"),
                    studentCourseGrade.get("courseName"),
                    studentCourseGrade.get("grade")
            });
        }
        JTable studentMarksTable = new JTable(studentMarksModel);
        tabbedPane.addTab("Student Course Marks", new JScrollPane(studentMarksTable));

        reportFrame.add(tabbedPane);
        reportFrame.setSize(800, 600);
        reportFrame.setLocationRelativeTo(null);
        reportFrame.setVisible(true);
    }
}