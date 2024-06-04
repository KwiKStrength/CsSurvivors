package Interface.InterfaceUser;

import Components.OrderPanel;
import Class.Connexion;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderHistoryForm extends JPanel {

    public OrderHistoryForm(int userId) {
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();

        JPanel contentPanel = new JPanel(new MigLayout("wrap 1", "[grow, fill]", "10[]10")); // 10px gap before and after each component
        scrollPane.setViewportView(contentPanel);

        try {
            Connection conn = Connexion.etablirConnexion();
            String query = "SELECT * FROM ORDER_TABLE WHERE USERID = ? AND orderstatus = 'Delivered'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderId = rs.getString("ORDERID");
                Date date = rs.getDate("dt");
                String orderStatus = rs.getString("orderstatus");
                float orderPrice = rs.getFloat("orderprice");

                List<Integer> itemIds = getItemIds(orderId, conn);
                List<Integer> quantities = getItemQuantities(orderId, conn);

                OrderPanel orderPanel = new OrderPanel(userId, date, orderStatus, orderPrice, itemIds, quantities, scrollPane, orderId);
                contentPanel.add(orderPanel, "grow, wrap");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    private List<Integer> getItemIds(String orderId, Connection conn) throws SQLException {
        List<Integer> itemIds = new ArrayList<>();
        String query = "SELECT ITEMID FROM ORDERITEM WHERE ORDERID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, orderId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            itemIds.add(rs.getInt("ITEMID"));
        }

        rs.close();
        stmt.close();
        return itemIds;
    }

    private List<Integer> getItemQuantities(String orderId, Connection conn) throws SQLException {
        List<Integer> quantities = new ArrayList<>();
        String query = "SELECT quantity FROM ORDERITEM WHERE ORDERID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, orderId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            quantities.add(rs.getInt("quantity"));
        }

        rs.close();
        stmt.close();
        return quantities;
    }
}
