package Interface.InterfaceUser;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import Class.Connexion;
import Components.OrderDetails;


public class PendingOrderForm extends JPanel {

    private final int userID;

    public PendingOrderForm(int userID) {
        this.userID = userID;
        setLayout(new BorderLayout());
        displayOrderDetails();
    }

    private void displayOrderDetails() {
        List<Integer> itemIDs = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        Date orderDate = null;
        float orderPrice = 0.0f;
        String orderID = "";
        String orderStatus = "";

        try (Connection conn = Connexion.etablirConnexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ORDER_TABLE WHERE (orderstatus = ? or orderstatus = ?) and USERID = ?")) {
            stmt.setString(1, "Pending");
            stmt.setString(2, "Being Prepared");
            stmt.setInt(3, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                orderID = rs.getString("ORDERID");
                orderDate = rs.getDate("dt");
                orderPrice = rs.getFloat("orderprice");
                orderStatus = rs.getString("orderstatus");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching order details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!orderID.isEmpty() && orderDate != null) {
            try (Connection conn = Connexion.etablirConnexion();
                 PreparedStatement stmt = conn.prepareStatement("SELECT ITEMID, quantity FROM ORDERITEM WHERE ORDERID = ?")) {
                stmt.setString(1, orderID);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    itemIDs.add(rs.getInt("ITEMID"));
                    quantities.add(rs.getInt("quantity"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error fetching order items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            OrderDetails orderDetails = new OrderDetails(orderID, itemIDs, quantities,this.userID, orderDate, orderPrice, orderStatus);
            JScrollPane scrollPane = new JScrollPane(orderDetails);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
            scrollPane.setPreferredSize(new Dimension(1200, 680));
            scrollPane.setMaximumSize(new Dimension(400, 300));
            scrollPane.setMinimumSize(new Dimension(400, 300));
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setOpaque(false);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

            removeAll();
            add(scrollPane, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }
}
