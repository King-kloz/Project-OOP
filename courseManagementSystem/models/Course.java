package courseManagementSystem.models;

public class Course {
    private int id;
    private String name;
    private int lecturerId;
    
    public Course(int id, String name, int lecturerId) {
        this.id = id;
        this.name = name;
        this.lecturerId = lecturerId;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public int getLecturerId() { return lecturerId; }
}