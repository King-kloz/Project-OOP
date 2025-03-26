package Utilities;

import java.util.Map;

public interface ReportGenerator {
    /**
     * Generates a bar chart from the provided data
     * @param data Map containing data for bar chart
     */
    void generateBarChart(Map<String, Object> data);

    /**
     * Generates a pie chart from the provided data
     * @param data Map containing data for pie chart
     */
    void generatePieChart(Map<String, Object> data);

    /**
     * Generates a comprehensive detailed report
     * Combines multiple types of analytics and visualizations
     */
    void generateDetailedReport();
}