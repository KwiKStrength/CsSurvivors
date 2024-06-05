package Interface.LoginInterfaceForms;

import Class.Connexion;
import Interface.InterfaceAdmin.interfaces.Dashboard;
import Interface.InterfaceUser.MenuInterface;
import Interface.LoginInterface;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.geom.RoundRectangle2D;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends JPanel {

    private LoginInterface loginInterface;
    private MenuInterface app;
    private int USERID;
    private String role;

    public Login(LoginInterface loginInterface, LoginInterface.Overlay.PanelOverlay parentOverlay) {
        this.loginInterface = loginInterface;
        init();
    }

    private void init() {
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
        });
        setLayout(new MigLayout("wrap,fillx,insets 45 45 50 45", "[fill]"));
        JLabel title = new JLabel("Login to your account", SwingConstants.CENTER);
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JCheckBox chRememberMe = new JCheckBox("Remember me");
        JButton cmdLogin = new JButton("Login");
        title.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +10");
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" +
                "focusWidth:1;" +
                "innerFocusWidth:0");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" +
                "focusWidth:1;" +
                "innerFocusWidth:0;" +
                "showRevealButton:true");
        cmdLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:$Component.accentColor;" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0");
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Username");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Password");

        add(title);
        add(new JLabel("Username"), "gapy 20");
        add(txtUsername);
        add(new JLabel("Password"), "gapy 10");
        add(txtPassword);
        add(chRememberMe);
        add(cmdLogin, "gapy 30");

        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText();
                String password = new String(txtPassword.getPassword());

                try {
                    String hashedPassword = hashPassword(password);
                    Connection connection = Connexion.etablirConnexion();
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM APPUSER WHERE username = ? AND password = ?");
                    statement.setString(1, username);
                    statement.setString(2, hashedPassword);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        role = resultSet.getString("role");
                        USERID = resultSet.getInt("USERID");

                        if (role.equals("student")) {
                            loginInterface.setVisible(false);
                            app = new MenuInterface(USERID, loginInterface);
                            txtUsername.setText("");
                            txtPassword.setText("");
                            app.setVisible(true);
                        } else if (role.equals("admin")) {
                            loginInterface.setVisible(false);
                            Dashboard.launchDashboard(USERID);
                        } else if (role.equals("chef")) {
                            // Implement chef logic here
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Wrong Informations");
                    }
                    connection.close();
                } catch (SQLException | NoSuchAlgorithmException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        };

        cmdLogin.addActionListener(loginAction);
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

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(password.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hashedBytes) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }
}
