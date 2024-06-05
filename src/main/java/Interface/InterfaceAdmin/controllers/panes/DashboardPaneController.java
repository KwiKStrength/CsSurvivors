package Interface.InterfaceAdmin.controllers.panes;

import Class.Connexion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardPaneController implements Initializable {

    @FXML
    private Label totalMenusLabel;
    @FXML
    private Label totalRevenuesLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label totalClientsLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DashboardPaneController initialized");
        System.out.println("totalMenusLabel: " + totalMenusLabel);
        System.out.println("totalRevenuesLabel: " + totalRevenuesLabel);
        System.out.println("totalOrdersLabel: " + totalOrdersLabel);
        System.out.println("totalClientsLabel: " + totalClientsLabel);
        loadDashboardData();
    }

    private void loadDashboardData() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = Connexion.etablirConnexion();
            if (connection != null) {
                System.out.println("Database connection established.");
            } else {
                System.out.println("Failed to establish database connection.");
                return;
            }

            // Total Menus
            String totalMenusQuery = "SELECT COUNT(*) AS totalMenus FROM ITEM";
            statement = connection.prepareStatement(totalMenusQuery);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int totalMenus = resultSet.getInt("totalMenus");
                totalMenusLabel.setText(String.valueOf(totalMenus));
                System.out.println("Total Menus: " + totalMenus);
            } else {
                System.out.println("Total Menus query did not return any results.");
            }
            resultSet.close();
            statement.close();

            // Total Revenues
            String totalRevenuesQuery = "SELECT SUM(orderprice) AS totalRevenues FROM ORDER_TABLE WHERE orderstatus = 'Delivered'";
            statement = connection.prepareStatement(totalRevenuesQuery);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int totalRevenues = resultSet.getInt("totalRevenues");
                totalRevenuesLabel.setText(String.valueOf(totalRevenues));
                System.out.println("Total Revenues: " + totalRevenues);
            } else {
                System.out.println("Total Revenues query did not return any results.");
            }
            resultSet.close();
            statement.close();

            // Total Orders
            String totalOrdersQuery = "SELECT COUNT(*) AS totalOrders FROM ORDER_TABLE";
            statement = connection.prepareStatement(totalOrdersQuery);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int totalOrders = resultSet.getInt("totalOrders");
                totalOrdersLabel.setText(String.valueOf(totalOrders));
                System.out.println("Total Orders: " + totalOrders);
            } else {
                System.out.println("Total Orders query did not return any results.");
            }
            resultSet.close();
            statement.close();

            // Total Clients
            String totalClientsQuery = "SELECT COUNT(*) AS totalClients FROM APPUSER";
            statement = connection.prepareStatement(totalClientsQuery);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int totalClients = resultSet.getInt("totalClients");
                totalClientsLabel.setText(String.valueOf(totalClients));
                System.out.println("Total Clients: " + totalClients);
            } else {
                System.out.println("Total Clients query did not return any results.");
            }
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
