package Interface.LoginInterfaceForms;

import Interface.LoginInterface;
import asyc.hwid.HWID;
import asyc.hwid.exception.UnsupportedOSException;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;
import Class.EmailSender;
import Class.Connexion;

public class Register extends JPanel {

    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private LoginInterface.Overlay.PanelOverlay parentOverlay;

    public Register(LoginInterface.Overlay.PanelOverlay parentOverlay) {
        init();
    }

    public void init() {
        setLayout(new MigLayout("wrap,fillx,insets 45 45 50 45", "[fill]"));
        addMouseListener(new MouseAdapter() {
        });
        setOpaque(false);
        txtUsername = new JTextField();
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();
        txtConfirmPassword = new JPasswordField();

        JLabel title = new JLabel("Register", SwingConstants.CENTER);
        JLabel lblUsername = new JLabel("Username");
        JLabel lblEmail = new JLabel("Email");
        JLabel lblPassword = new JLabel("Password");
        JLabel lblConfirmPassword = new JLabel("Confirm Password");

        JButton cmdRegister = new JButton("Register");

        title.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +10");
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" +
                "focusWidth:1;" +
                "innerFocusWidth:0");
        txtEmail.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" +
                "focusWidth:1;" +
                "innerFocusWidth:0");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" +
                "focusWidth:1;" +
                "innerFocusWidth:0;" +
                "showRevealButton:true");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" +
                "focusWidth:1;" +
                "innerFocusWidth:0;" +
                "showRevealButton:true");

        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Username");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Password");
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Email");
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Confirm your Password");


        cmdRegister.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:$Component.accentColor;" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0");

        cmdRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText();
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                String confirmPassword = new String(txtConfirmPassword.getPassword());
                String hwid = "0";
                try {
                    hwid = HWID.getHWID();
                } catch (UnsupportedOSException | IOException | NoSuchAlgorithmException eb) {
                    eb.printStackTrace();
                }


                if (!email.endsWith("@uir.ac.ma")) {
                    JOptionPane.showMessageDialog(null, "Invalid email format.");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match");
                    return;
                }

                try {
                    Connection connection = Connexion.etablirConnexion();

                    PreparedStatement usernameCheckStmt = connection.prepareStatement("SELECT * FROM APPUSER WHERE username = ?");
                    usernameCheckStmt.setString(1, username);
                    ResultSet usernameResult = usernameCheckStmt.executeQuery();
                    if (usernameResult.next()) {
                        JOptionPane.showMessageDialog(null, "Username already exists.");
                        return;
                    }

                    PreparedStatement emailCheckStmt = connection.prepareStatement("SELECT * FROM APPUSER WHERE email = ?");
                    emailCheckStmt.setString(1, email);
                    ResultSet emailResult = emailCheckStmt.executeQuery();
                    if (emailResult.next()) {
                        JOptionPane.showMessageDialog(null, "Email already exists.");
                        txtPassword.setText("");
                        txtConfirmPassword.setText("");
                        return;
                    }

                    if (hwid != null) {
                        PreparedStatement hwidCheckStmt = connection.prepareStatement("SELECT * FROM APPUSER WHERE HWID = ?");
                        hwidCheckStmt.setString(1, hwid);
                        ResultSet hwidResult = hwidCheckStmt.executeQuery();
                        if (hwidResult.next()) {
                            JOptionPane.showMessageDialog(null, "You already have an account");
                            return;
                        }
                    }

                    String confirmationCode = generateConfirmationCode();
                    EmailSender.sendEmail(email, "Confirmation Code", "Your confirmation code is: " + confirmationCode);

                    int attemptCount = 0;
                    while (attemptCount < 3) {
                        String inputCode = JOptionPane.showInputDialog(null, "Enter the confirmation code sent to your email:");
                        if (inputCode == null) {
                            return;
                        }
                        if (confirmationCode.equals(inputCode)) {
                            String query = "INSERT INTO APPUSER (username, password, email, role, HWID) VALUES (?, ?, ?, ?, ?)";
                            PreparedStatement statement = connection.prepareStatement(query);
                            statement.setString(1, username);
                            String hashedPassword = hashPassword(password);
                            statement.setString(2, hashedPassword);
                            statement.setString(3, email);
                            statement.setString(4, "student");
                            statement.setString(5, hwid);
                            statement.executeUpdate();

                            JOptionPane.showMessageDialog(null, "Registration successful!");
                            txtUsername.setText("");
                            txtEmail.setText("");
                            txtPassword.setText("");
                            txtConfirmPassword.setText("");
                            connection.close();
                            return;
                        } else {
                            shakeDialog();
                            attemptCount++;
                            txtUsername.setText("");
                            txtEmail.setText("");
                            txtPassword.setText("");
                            txtConfirmPassword.setText("");
                        }
                    }

                    JOptionPane.showMessageDialog(null, "Too many incorrect attempts. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                    connection.close();

                } catch (SQLException | NoSuchAlgorithmException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        });
        add(title);
        add(lblUsername);
        add(txtUsername, "growx, wrap");
        add(lblEmail);
        add(txtEmail, "growx, wrap");
        add(lblPassword);
        add(txtPassword, "growx, wrap");
        add(lblConfirmPassword);
        add(txtConfirmPassword, "growx, wrap");
        add(cmdRegister, "gapy 30");
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest hasher = MessageDigest.getInstance("SHA-256");
        byte[] hash = hasher.digest(password.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hash) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }

    private String generateConfirmationCode() {
        return String.format("%06d", (int) (Math.random() * 999999));
    }


    private void shakeDialog() {
        final int SHAKE_DISTANCE = 15;
        final int SHAKE_DURATION = 100;
        final int SHAKE_CYCLES = 8;

        Point originalLocation = getLocation();

        for (int i = 0; i < SHAKE_CYCLES; i++) {
            try {
                setLocation(originalLocation.x + ThreadLocalRandom.current().nextInt(-SHAKE_DISTANCE, SHAKE_DISTANCE + 1),
                        originalLocation.y + ThreadLocalRandom.current().nextInt(-SHAKE_DISTANCE, SHAKE_DISTANCE + 1));
                Thread.sleep(SHAKE_DURATION);
                setLocation(originalLocation);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = UIScale.scale(20);
        g2.setColor(getBackground());
        g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
        g2.dispose();
        super.paintComponent(g);
    }
}
