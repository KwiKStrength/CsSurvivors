package Interface.InterfaceAdmin.controllers;

import Interface.InterfaceAdmin.controllers.panes.UserPaneController;
import Interface.LoginInterface;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    private SplitPane splitPaneId;
    @FXML
    private AnchorPane userPane;

    private int userID;

    public void setUserID(int userID) {
        this.userID = userID;
        System.out.println("User ID set to: " + userID);
        loadUserDashboardPane(); // Load the Dashboard pane by default
    }

    private void loadUserPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FX/panes/UserPane.fxml"));
            Parent root = loader.load();
            UserPaneController controller = loader.getController();
            controller.setUserID(userID);
            stackPane.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Any initialization if needed
    }

    @FXML
    private void loadUserDashboardPane() {
        loadPane("/FX/panes/DashboardPane.fxml");
    }

    @FXML
    private void loadUserProfilePane() {
        loadUserPane();
    }

    @FXML
    private void loadProductPane() {
        loadPane("/FX/panes/ProductPane.fxml");
    }

    @FXML
    private void loadCategoryPane() {
        loadPane("/FX/panes/CategoryPane.fxml");
    }

    @FXML
    private void loadAddProductPane() {
        loadPane("/FX/panes/AddProductPane.fxml");
    }

    @FXML
    private void loadOrdersPane() {
        loadPane("/FX/panes/OrdersPane.fxml");
    }

    @FXML
    private void loadAllCustomersPane() {
        loadPane("/FX/panes/CustomersPane.fxml");
    }

    private void loadPane(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent pane = loader.load();
            stackPane.getChildren().clear(); // Clear existing content
            stackPane.getChildren().add(pane);
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception properly in your application
        }
    }

    @FXML
    private void handleLogout() {
        if (stackPane.getScene() != null) {
            Stage stage = (Stage) stackPane.getScene().getWindow();
            stage.close();
            SwingUtilities.invokeLater(() -> new LoginInterface().setVisible(true));
        } else {
            System.err.println("Scene is null, cannot log out.");
        }
    }
}
