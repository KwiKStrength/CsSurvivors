package Interface.InterfaceAdmin.controllers.panes;

import Class.Customers;
import Class.Connexion;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomersPaneController implements Initializable {
    private final ObservableList<Customers> users = FXCollections.observableArrayList();

    @FXML
    private VBox tableContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reloadData();
    }

    private void reloadData() {
        loadUsersFromDatabase();
        setupTable();
    }

    private void loadUsersFromDatabase() {
        users.clear();  // Clear the list to avoid duplication
        String query = "SELECT USERID, username, email, role FROM APPUSER";
        try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("USERID");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String role = rs.getString("role");
                Customers customer = new Customers(userId, username, email, role);
                users.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        tableContent.getChildren().clear(); // Clear the table content first
        HBox headers = new HBox();
//        headers.getChildren().addAll(
//                createHeaderLabel("UserID"),
//                createHeaderLabel("Username"),
//                createHeaderLabel("Email"),
//                createHeaderLabel("Role"),
//                createHeaderLabel("Edit")
//        );
        tableContent.getChildren().add(headers);

        for (Customers user : users) {
            HBox row = new HBox();
            Button editButton = new Button("Edit");
            editButton.setOnAction(event -> {
                showEditDialog(user);
            });

            row.getChildren().addAll(
                    createCellLabel(String.valueOf(user.getUserID())),
                    createCellLabel(user.getUsername()),
                    createCellLabel(user.getEmail()),
                    createCellLabel(user.getRole()),
                    editButton
            );
            tableContent.getChildren().add(row);
        }
        tableContent.setSpacing(10);
    }

    private void showEditDialog(Customers user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Editing details for " + user.getUsername());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField(user.getUsername());
        TextField emailField = new TextField(user.getEmail());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter new password");
        MFXComboBox<String> roleComboBox = createRoleComboBox(user.getRole());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateUserInDatabase(user.getUserID(), usernameField.getText(), emailField.getText(), passwordField.getText(), roleComboBox.getValue());
            reloadData();  // Reload data after update
        }
    }


    private void updateUserInDatabase(int userId, String username, String email, String password, String role) {
        String query = "UPDATE APPUSER SET username = ?, email = ?, password = ?, role = ? WHERE USERID = ?";
        try (Connection conn = Connexion.etablirConnexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);  // Assume hashing the password
            ps.setString(4, role);
            ps.setInt(5, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private MFXComboBox<String> createRoleComboBox(String initialRole) {
        MFXComboBox<String> comboBox = new MFXComboBox<>();
        comboBox.setFloatingText("Role");
        comboBox.getItems().addAll("student", "admin", "cuisinier");  // Add more roles as necessary
        comboBox.setValue(initialRole);  // Set the initial selected item
        return comboBox;
    }


    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-padding: 5px;");
        label.setPrefWidth(170);
        label.setMinWidth(170);
        label.setMaxWidth(170);
        return label;
    }

    private Label createCellLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-padding: 5px;");
        label.setPrefWidth(170);
        label.setMinWidth(170);
        label.setMaxWidth(170);
        return label;
    }
}
