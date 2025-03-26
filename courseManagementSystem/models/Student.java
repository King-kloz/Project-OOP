package courseManagementSystem.models;

public class Student extends Person {
    public Student(String name, String email) {
        super(name, email);
    }
    
    @Override
    public void displayDetails() {
        System.out.println("Student: " + name + ", Email: " + email);
    }
}