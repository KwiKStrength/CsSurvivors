package Interface.InterfaceAdmin.controllers.panes;

import Class.Customers;
import Class.Order;
import Class.Connexion;
import Class.Product;
import io.github.palexdev.materialfx.controls.MFXButton;
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class OrdersPaneController implements Initializable {

    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    @FXML
    private VBox tableContent;
    @FXML
    private ComboBox<Order.StatusOrder> statusComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadOrdersFromDatabase();
        setupTable();

    }

    private void loadOrdersFromDatabase() {
        Map<String, Order> orderMap = new HashMap<>();

        String query = "SELECT OT.ORDERID, OT.dt, OT.orderprice, OT.orderstatus, AU.username, AU.USERID, AU.email FROM ORDER_TABLE OT JOIN APPUSER AU ON OT.USERID = AU.USERID;";
        try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String orderId = rs.getString("ORDERID");
                Order order = orderMap.get(orderId);

                if (order == null) {
                    Customers customer = new Customers(rs.getInt("USERID"), rs.getString("username"), rs.getString("email"));
                    order = new Order(orderId, customer, rs.getTimestamp("dt"), rs.getString("orderstatus"), rs.getDouble("orderprice"));
                    orderMap.put(orderId, order);
                }
            }
            orders.setAll(orderMap.values()); // Update the observable list for JavaFX UI
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        tableContent.getChildren().clear();
        HBox headers = new HBox(20);  // Added spacing between header elements
        headers.getChildren().addAll(
                createCellLabel("Order ID"),
                createCellLabel("Date"),
                createCellLabel("Username"),
                createCellLabel("Price"),
                createCellLabel("View Details"),
                createCellLabel("Status")
        );
        tableContent.getChildren().add(headers);

        for (Order order : orders) {
            HBox row = new HBox(20);  // Added spacing between row elements
            MFXButton viewDetailsBtn = new MFXButton("View Details");

            viewDetailsBtn.setId("custom");
            viewDetailsBtn.setOnAction(event -> showOrderDetailsDialog(order));

            MFXComboBox<String> statusComboBox = new MFXComboBox<>();

            statusComboBox.setFloatingText("Status");
            statusComboBox.setText(order.getOrderStatus());
            statusComboBox.getItems().addAll("Pending", "Being Prepared", "Ready", "Delivered");
            statusComboBox.setValue(order.getOrderStatus()); // Set current status
            statusComboBox.setPrefWidth(100);  // Ensure the width accommodates the text
            statusComboBox.setOnAction(e -> {
                updateOrderStatus(order, statusComboBox.getSelectionModel().getSelectedItem());
                loadOrdersFromDatabase();  // Reload data to reflect changes
            });

            row.getChildren().addAll(
                    createCellLabel(order.getOrderId()),
                    createCellLabel(String.valueOf(order.getDate())),
                    createCellLabel(order.getCustomer().getUsername()),
                    createCellLabel(String.format("%.2f", order.getOrderPrice())),
                    viewDetailsBtn,
                    statusComboBox
            );
            tableContent.getChildren().add(row);
        }
    }


    private void updateOrderStatus(Order order, String newStatus) {
        order.setOrderStatus(newStatus);
        // Here you would call a method to update the database
        updateOrderStatusInDB(order.getOrderId(), newStatus);
    }
    private void updateOrderStatusInDB(String orderId, String newStatus) {
        String query = "UPDATE ORDER_TABLE SET orderstatus = ? WHERE ORDERID = ?";
        try (Connection conn = Connexion.etablirConnexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newStatus);
            ps.setString(2, orderId);
            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("Order status updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showOrderDetailsDialog(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Détails de la commande");
        dialog.setHeaderText("Détails pour la commande : " + order.getOrderId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Détails de base de la commande
        grid.add(new Label("ID de commande :"), 0, 0);
        grid.add(new Label(String.valueOf(order.getOrderId())), 1, 0);
        grid.add(new Label("Date :"), 0, 1);
        grid.add(new Label(order.getDate().toString()), 1, 1);
        grid.add(new Label("Nom du client :"), 0, 2);
        grid.add(new Label(order.getCustomer().getUsername()), 1, 2);
        grid.add(new Label("Email du client :"), 0, 3);
        grid.add(new Label(order.getCustomer().getEmail()), 1, 3);
        grid.add(new Label("Statut de la commande :"), 0, 4);
        grid.add(new Label(order.getOrderStatus()), 1, 4);

        // En-têtes pour les détails des produits
        grid.add(new Label("Nom du produit"), 0, 5);
        grid.add(new Label("Quantité"), 1, 5);
        grid.add(new Label("Prix unitaire"), 2, 5);
        grid.add(new Label("Description"), 3, 5);

        // Ajouter les informations de chaque produit dans les lignes du GridPane
        int row = 6;
        for (Product product : order.getProducts()) {
            grid.add(new Label(product.getProductName()), 0, row);
            grid.add(new Label(String.valueOf(product.getQuantity())), 1, row);
            grid.add(new Label(String.format("DH %.2f", product.getPrice())), 2, row);
            grid.add(new Label(product.getDescription()), 3, row);
            row++;
        }

        // Ajouter le prix total à la fin
        grid.add(new Label("Prix total :"), 0, row);
        Label totalPriceLabel = new Label(String.format("DH %.2f", order.getOrderPrice()));
        totalPriceLabel.setStyle("-fx-font-weight: bold");
        grid.add(totalPriceLabel, 1, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        dialog.showAndWait();
    }


    private void showUserDetailsDialog(Order order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText("Information for Order ID: " + order.getOrderId());

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Username: " + order.getCustomer().getUsername()), new Label("Email: " + order.getCustomer().getEmail()), new Label("Order Status: " + order.getOrderStatus()), new Label("Order Price: DH " + String.format("%.2f", order.getOrderPrice())), new Label("Item ID: " + order.getProducts().getFirst().getProductId()), new Label("Quantity: " + order.getProducts().getFirst().getQuantity()));

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }


    private MFXComboBox<Order.StatusOrder> createStatusComboBox(Order.StatusOrder initialStatus) {
        MFXComboBox<Order.StatusOrder> comboBox = new MFXComboBox<>();
        comboBox.getItems().addAll(Order.StatusOrder.values());
        comboBox.setPrefWidth(170);
        comboBox.setFloatingText("status");
        comboBox.setText(String.valueOf(initialStatus));
//        comboBox.setValue(initialStatus);
        comboBox.setOnAction(event -> {
            Order.StatusOrder selectedStatus = comboBox.getValue() != null ? comboBox.getValue() : Order.StatusOrder.PENDING;
            String selectedStatusText = selectedStatus != null ? selectedStatus.toString() : "PENDING";

            // Perform actions based on the selected status
            // You can modify this based on your application logic
        });
        return comboBox;
    }

    private Label createCellLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-padding: 5px;   -fx-border-width: 0.5; -fx-border-insets: 5; -fx-border-radius: 5;");
        label.setPrefWidth(120); // Adjusted width for better fit
        label.setMinWidth(Control.USE_PREF_SIZE);
        label.setMaxWidth(Control.USE_PREF_SIZE);
        return label;
    }



}
