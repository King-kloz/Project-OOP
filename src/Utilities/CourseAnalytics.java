package Utilities;

import java.util.List;
import java.util.Map;

public interface CourseAnalytics {
    /**
     * Generates comprehensive enrollment statistics for courses
     * @return Map containing enrollment metrics
     */
    Map<String, Object> generateEnrollmentStatistics();

    /**
     * Generates grade distribution across all courses
     * @return Map with grade distribution details
     */
    Map<String, Object> generateGradeDistribution();

    /**
     * Retrieves average grades for each course
     * @return List of Maps containing course-wise grade information
     */
    List<Map<String, Object>> getAverageGradesByCourse();
}