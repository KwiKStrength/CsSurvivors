package Interface.InterfaceUser;

import Components.OrderPanel;
import Class.Connexion;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderHistoryForm extends JPanel {

    private JPanel contentPanel;
    private JScrollPane scrollPane;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> orderComboBox;
    private int userId;

    public OrderHistoryForm(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        JLabel sortLabel = new JLabel("Sort by:");
        String[] sortOptions = {"Date", "Price"};
        sortComboBox = new JComboBox<>(sortOptions);

        JLabel orderLabel = new JLabel("Order:");
        String[] orderOptions = {"ASC", "DESC"};
        orderComboBox = new JComboBox<>(orderOptions);

        ActionListener sortActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadOrderHistory();
            }
        };

        sortComboBox.addActionListener(sortActionListener);
        orderComboBox.addActionListener(sortActionListener);

        controlPanel.add(sortLabel);
        controlPanel.add(sortComboBox);
        controlPanel.add(orderLabel);
        controlPanel.add(orderComboBox);

        scrollPane = new JScrollPane();
        contentPanel = new JPanel(new MigLayout("wrap 1", "[grow, fill]", "10[]10"));
        scrollPane.setViewportView(contentPanel);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        contentPanel.removeAll();

        String selectedSortOption = (String) sortComboBox.getSelectedItem();
        String selectedOrderOption = (String) orderComboBox.getSelectedItem();
        String orderByClause = "dt";

        if ("Price".equals(selectedSortOption)) {
            orderByClause = "orderprice";
        }

        String orderClause = "ASC".equals(selectedOrderOption) ? "ASC" : "DESC";

        try {
            Connection conn = Connexion.etablirConnexion();
            String query = "SELECT * FROM ORDER_TABLE WHERE USERID = ? AND orderstatus = 'Delivered' ORDER BY " + orderByClause + " " + orderClause;
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

                OrderPanel orderPanel = new OrderPanel(userId, date, orderStatus, orderPrice, itemIds, quantities, scrollPane, orderId,"student");
                contentPanel.add(orderPanel, "grow, wrap");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        contentPanel.revalidate();
        contentPanel.repaint();
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
