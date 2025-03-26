package Utilities;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AuthenticationManager {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationManager.class.getName());

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Password hashing error", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public boolean authenticate(String email, String password) {
        String[] userTypes = {"student", "instructor", "administrator"};

        for (String userType : userTypes) {
            String sql = "SELECT " + userType.toLowerCase() + "Id AS userId, email, passwordHash FROM " + userType + " WHERE email = ?";

            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, email);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedPasswordHash = rs.getString("passwordHash");
                        String hashedInputPassword = hashPassword(password);

                        if (storedPasswordHash.equals(hashedInputPassword)) {
                            UserSession.getInstance().createSession(
                                    rs.getInt("userId"),
                                    rs.getString("email"),
                                    userType.toUpperCase()
                            );

                            logLoginAttempt(email, true);
                            return true;
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Authentication error", e);
                throw new RuntimeException("Authentication process failed", e);
            }
        }

        logLoginAttempt(email, false);
        return false;
    }

    public void logout() {
        logLogoutAttempt(UserSession.getInstance().getUsername());

        UserSession.getInstance().clearSession();
    }

    private void logLoginAttempt(String username, boolean success) {
        String sql = "INSERT INTO LoginAudit (username, login_time, success) VALUES (?, NOW(), ?)";

        try {
            DatabaseConnectionManager.getInstance().executeUpdate(
                    sql, username, success
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log login attempt", e);
        }
    }

    private void logLogoutAttempt(String username) {
        String sql = "UPDATE LoginAudit SET logout_time = NOW() WHERE username = ? AND logout_time IS NULL";

        try {
            DatabaseConnectionManager.getInstance().executeUpdate(
                    sql, username
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log logout attempt", e);
        }
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        if (!authenticate(email, oldPassword)) {
            return false;
        }

        for (String userType : new String[]{"student", "instructor", "administrator"}) {
            String sql = "UPDATE " + userType + " SET passwordHash = ? WHERE email = ?";
            String hashedNewPassword = hashPassword(newPassword);

            try {
                int updatedRows = DatabaseConnectionManager.getInstance().executeUpdate(
                        sql, hashedNewPassword, email
                );
                if (updatedRows > 0) {
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Password change error for " + userType, e);
            }
        }
        return false;
    }
}