package Models;

import Utilities.DatabaseConnectionManager;

import java.time.LocalDateTime;

public abstract class Person {
    protected int id;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected boolean isActive;
    protected LocalDateTime dateCreated;
    protected LocalDateTime lastUpdated;

    protected DatabaseConnectionManager dbManager;

    public Person(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.isActive = true;
        this.dateCreated = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.dbManager = new DatabaseConnectionManager();
    }

    public abstract void updateProfile();

    public void displayDetails() {
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Active Status: " + (isActive ? "Active" : "Inactive"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    protected void setPassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
    }

    private String hashPassword(String password) {
        return password; // Simplified for example
    }

    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}