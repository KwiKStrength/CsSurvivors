package Interface.InterfaceChef;

import Components.OrderPanel;
import Interface.LoginInterface;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import Class.Connexion;

public class OrderList extends JPanel {

    private JPanel contentPanel;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> orderComboBox;
    private JScrollPane scrollPane;
    private LoginInterface parents;

    public OrderList(String role, LoginInterface parent) {
        this.parents = parent;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));
        JPanel innerPanel = new JPanel(new BorderLayout());
        contentPanel = new JPanel(new MigLayout("wrap 1", "[grow, fill]", "10[]10"));
        scrollPane = new JScrollPane(contentPanel);
        innerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JLabel sortLabel = new JLabel("Sort by:");
        String[] sortOptions = {"Date", "Price"};
        sortComboBox = new JComboBox<>(sortOptions);

        JLabel orderLabel = new JLabel("Order:");
        String[] orderOptions = {"ASC", "DESC"};
        orderComboBox = new JComboBox<>(orderOptions);

        JButton allOrdersButton = new JButton("All Orders");
        allOrdersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                innerPanel.removeAll();
                scrollPane = new JScrollPane(contentPanel);
                innerPanel.add(scrollPane, BorderLayout.CENTER);
                contentPanel.removeAll();
                loadOrderHistory();
                innerPanel.revalidate();
                innerPanel.repaint();
            }
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        ActionListener sortActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                innerPanel.removeAll();
                scrollPane = new JScrollPane(contentPanel);
                innerPanel.add(scrollPane, BorderLayout.CENTER);
                contentPanel.removeAll();
                loadOrderHistory();
                innerPanel.revalidate();
                innerPanel.repaint();
            }
        };

        sortComboBox.addActionListener(sortActionListener);
        orderComboBox.addActionListener(sortActionListener);

        controlPanel.add(sortLabel);
        controlPanel.add(sortComboBox);
        controlPanel.add(orderLabel);
        controlPanel.add(orderComboBox);
        controlPanel.add(allOrdersButton);
        controlPanel.add(logoutButton);

        add(controlPanel, BorderLayout.NORTH);
        add(innerPanel, BorderLayout.CENTER);

        loadOrderHistory();
    }

    private void logout(){
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.dispose();
        parents.setVisible(true);
    }
    private void loadOrderHistory() {

        try {
            Connection conn = Connexion.etablirConnexion();
            String sortBy = sortComboBox.getSelectedItem().toString().toLowerCase();
            String order = orderComboBox.getSelectedItem().toString();

            if (sortBy.equals("date")){
                sortBy = "dt";
            } else {
                sortBy = "orderprice";
            }

            String query = "SELECT * FROM ORDER_TABLE ORDER BY CASE " +
                    "WHEN orderstatus='Pending' THEN 1 " +
                    "WHEN orderstatus='Being Prepared' THEN 2 " +
                    "WHEN orderstatus='Ready' THEN 3 " +
                    "ELSE 4 " +
                    "END, " + sortBy + " " + order;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderId = rs.getString("ORDERID");
                Date date = rs.getDate("dt");
                String orderStatus = rs.getString("orderstatus");
                float orderPrice = rs.getFloat("orderprice");

                List<Integer> itemIds = getItemIds(orderId, conn);
                List<Integer> quantities = getItemQuantities(orderId, conn);

                OrderPanel orderPanel = new OrderPanel(1, date, orderStatus, orderPrice, itemIds, quantities, scrollPane, orderId, "chef");
                contentPanel.add(orderPanel, "grow, wrap");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
