package Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Initialize {
    private static final Logger LOGGER = Logger.getLogger(Initialize.class.getName());
    private AuthenticationManager authManager = new AuthenticationManager();

    public void addAdministrator(String adminName, String email, String password) {
        String checkUserSql = "SELECT COUNT(*) FROM Administrator WHERE email = ?";
        String insertSql = "INSERT INTO Administrator (adminName, email, passwordHash, isActive) VALUES (?, ?, ?, 1)";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {

            checkStmt.setString(1, email);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("User already exists.");
                    return;
                }
            }

            String hashedPassword = authManager.hashPassword(password);

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, adminName);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Administrator added successfully.");
                } else {
                    System.out.println("Failed to add administrator.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding administrator", e);
            throw new RuntimeException("Administrator creation failed", e);
        }
    }

    public static void main(String[] args) {
        Initialize initializer = new Initialize();
        initializer.addAdministrator("Ronnie Nguru", "ronnie@gmail.com", "securepassword123");
    }
}