package Views;

import Utilities.AuthenticationManager;
import Utilities.UserSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton resetPasswordButton;
    private AuthenticationManager authManager;

    public LoginFrame() {
        this.authManager = new AuthenticationManager();

        setTitle("University Course Management System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel logoLabel = new JLabel("University Course Management", JLabel.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(logoLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        loginPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        loginPanel.add(loginButton, gbc);

        gbc.gridy = 4;
        resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPasswordResetDialog();
            }
        });
        loginPanel.add(resetPasswordButton, gbc);

        mainPanel.add(loginPanel, BorderLayout.CENTER);

        add(mainPanel);

        getRootPane().setDefaultButton(loginButton);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (authManager.authenticate(email, password)) {
                openDashboard();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid email or password. Please try again.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);

                passwordField.setText("");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login error", e);
            JOptionPane.showMessageDialog(this,
                    "An error occurred during login. Please try again.",
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openPasswordResetDialog() {
        JDialog resetDialog = new JDialog(this, "Reset Password", true);
        resetDialog.setSize(350, 250);
        resetDialog.setLocationRelativeTo(this);

        JPanel resetPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        resetPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        JTextField resetEmailField = new JTextField(20);
        resetPanel.add(resetEmailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        resetPanel.add(new JLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField oldPasswordField = new JPasswordField(20);
        resetPanel.add(oldPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        resetPanel.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(20);
        resetPanel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton confirmResetButton = new JButton("Change Password");
        confirmResetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = resetEmailField.getText().trim();
                String oldPassword = new String(oldPasswordField.getPassword());
                String newPassword = new String(newPasswordField.getPassword());

                if (email.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(resetDialog,
                            "Please fill in all fields.",
                            "Reset Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean changed = authManager.changePassword(email, oldPassword, newPassword);

                if (changed) {
                    JOptionPane.showMessageDialog(resetDialog,
                            "Password successfully changed.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    resetDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(resetDialog,
                            "Failed to change password. Check your current password.",
                            "Reset Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        resetPanel.add(confirmResetButton, gbc);

        resetDialog.add(resetPanel);
        resetDialog.setVisible(true);
    }

    private void openDashboard() {
        String userType = UserSession.getInstance().getUserType();

        this.dispose();

        switch (userType) {
            case "STUDENT":
                new StudentDashboard().setVisible(true);
                break;
            case "INSTRUCTOR":
                new InstructorDashboard().setVisible(true);
                break;
            case "ADMINISTRATOR":
                new AdminDashboard().setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(null,
                        "Unable to determine user type.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}