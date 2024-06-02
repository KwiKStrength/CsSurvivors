package Interface.InterfaceAdmin.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    Circle userProfile;
    @FXML
    private StackPane stackPane;
    @FXML
    private SplitPane splitPaneId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/AdminInterface/user-profile.png")));
        if (img.isError()) {
            System.err.println("Error loading image: " + img.getException().getMessage());
        } else {
            userProfile.setFill(new ImagePattern(img));
        }

        // Fixing the height of the top pane by setting a constant divider position
         // Adjust the value as needed
        loadUserDashboardPane();
    }


    @FXML
    private void loadUserDashboardPane() {
        loadPane("/FX/panes/DashboardPane.fxml");
    }


    @FXML
    private void loadUserProfilePane() {
        loadPane("/FX/panes/UserPane.fxml");
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
    private void loadAllUserPane() {
        loadPane("/FX/panes/UserPane.fxml");
    }

    @FXML
    private void loadAllCustomersPane() {
        loadPane("/FX/panes/CustomersPane.fxml");
    }

    private void loadPane(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            stackPane.getChildren().clear(); // Clear existing content
            stackPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception properly in your application
        }
    }
}
