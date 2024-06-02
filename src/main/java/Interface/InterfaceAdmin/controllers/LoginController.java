package Interface.InterfaceAdmin.controllers;

import Class.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {


    @FXML
    private TextField usernameField;

    private User user;

    @FXML
    private Button loginButton;
    private TextField text;
    private Stage stage;

    // Initialize method that is called after the FXML is loaded
    public void initialize() {
        // Do something with root and textField


        System.out.println(user);

    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return usernameField.getText();
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {

        if (user != null) {
            System.out.println("Logged in user: " + user.getEmail());
        } else {
            System.out.println("User is null.");
        }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            // Load the FXML file for the second scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FX/dashboard.fxml"));
            Parent root = loader.load();

            // Create a new scene with the loaded root
            Scene scene = new Scene(root);

            // Get the Stage from the ActionEvent
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the scene in the stage
            stage.setScene(scene);
            stage.setTitle("Dashboard");

            // Show the stage
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
