package Interface.InterfaceAdmin.controllers.panes;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import Class.Connexion;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserPaneController implements Initializable {

    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;

    private int userID;

    public void setUserID(int userID) {
        this.userID = userID;
        System.out.println("Setting User ID: " + userID);
        loadUserInfo(userID);
    }

    private void loadUserInfo(int userID) {
        System.out.println("Loading user info for User ID: " + userID);
        try (Connection connection = Connexion.etablirConnexion()) {
            String query = "SELECT username, email, role FROM APPUSER WHERE USERID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String role = resultSet.getString("role");

                // Update the labels
                nameLabel.setText("Username: " + username);
                emailLabel.setText("Email: " + email);
                roleLabel.setText("Role: " + role);
            } else {
                System.err.println("No user found with User ID: " + userID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Optionally initialize some UI components if needed
    }
}