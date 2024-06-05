package Interface.InterfaceUser;

import Class.Connexion;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsWindow extends JFrame {
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField confirmPasswordField;
    private JToggleButton musicToggleButton;
    private boolean isMusicOn;
    private Clip clip;
    private int userID;

    public SettingsWindow(int userID) {
        this.userID = userID;
        setTitle("User Settings");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(4, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("New Password:");
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");

        usernameField = new JTextField();
        passwordField = new JTextField();
        confirmPasswordField = new JTextField();

        loadUserDetails();

        settingsPanel.add(usernameLabel);
        settingsPanel.add(usernameField);
        settingsPanel.add(passwordLabel);
        settingsPanel.add(passwordField);
        settingsPanel.add(confirmPasswordLabel);
        settingsPanel.add(confirmPasswordField);

        musicToggleButton = new JToggleButton("Music On");
        isMusicOn = true;
        musicToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleMusic();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(settingsPanel, BorderLayout.CENTER);
        add(musicToggleButton, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUserDetails() {
        try {
            Connection conn = Connexion.etablirConnexion();
            String query = "SELECT username FROM APPUSER WHERE USERID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                usernameField.setText(username);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void toggleMusic() {
        isMusicOn = !isMusicOn;
        musicToggleButton.setText(isMusicOn ? "Music On" : "Music Off");
        if (isMusicOn) {
            playMusic();
        } else {
            stopMusic();
        }
    }

    private void playMusic() {

    }

    private void stopMusic() {

    }

    private void saveSettings() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            password = null;
        }

        try {
            Connection conn = Connexion.etablirConnexion();
            conn.setAutoCommit(false);

            String checkUsernameQuery = "SELECT COUNT(*) AS count FROM APPUSER WHERE username = ? AND USERID != ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkUsernameQuery);
            checkStmt.setString(1, username);
            checkStmt.setInt(2, userID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                rs.close();
                checkStmt.close();
                conn.rollback();
                conn.close();
                return;
            }

            rs.close();
            checkStmt.close();

            String updateQuery;
            if (password != null) {
                updateQuery = "UPDATE APPUSER SET username = ?, password = ? WHERE USERID = ?";
            } else {
                updateQuery = "UPDATE APPUSER SET username = ? WHERE USERID = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, username);
            if (password != null) {
                stmt.setString(2, password);
                stmt.setInt(3, userID);
            } else {
                stmt.setInt(2, userID);
            }
            stmt.executeUpdate();

            stmt.close();
            conn.commit();
            conn.close();

            JOptionPane.showMessageDialog(this, "Settings saved successfully!");
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving settings!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
