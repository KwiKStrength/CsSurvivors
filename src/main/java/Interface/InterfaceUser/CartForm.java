package Interface.InterfaceUser;

import Components.CartPanel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import Class.Connexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CartForm extends JPanel {

    private int userID;
    private JScrollPane scrollPane;
    private JPanel scrollContentPanel;
    private JButton checkoutButton;
    private JLabel totalLabel;

    public CartForm(int userID) {
        FlatInterFont.install();
        this.userID = userID;
        init();
    }

    private void init() {
        setLayout(new MigLayout("insets 10", "[grow]", "[grow]10[]"));
        scrollContentPanel = new JPanel(new MigLayout("wrap 1, gap 10", "[]", "[]"));
        scrollPane = new JScrollPane(scrollContentPanel);
        scrollPane.setPreferredSize(new Dimension(1000, 600));

        checkoutButton = new JButton("Checkout");
        checkoutButton.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:#00C14F;" +
                "borderWidth:0;" +
                "foreground:#FFFFFF;" +
                "font:bold +16;");

        checkoutButton.addActionListener(e -> {
            checkout();
        });

        totalLabel = new JLabel();
        totalLabel.putClientProperty(FlatClientProperties.STYLE,""+
                "font:light +16");
        updateTotal();

        loadCartItems();

        add(scrollPane, "grow, wrap");
        add(totalLabel, "split 2, align left");
        add(checkoutButton, "align right");
    }

    public void loadCartItems() {
        scrollContentPanel.removeAll();
        List<CartPanel> cartItems = fetchCartItems();

        if (cartItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("EMPTY CART");
            emptyLabel.putClientProperty(FlatClientProperties.STYLE,"font:light +28");
            scrollContentPanel.add(emptyLabel, "align center");
        } else {
            for (CartPanel cartItem : cartItems) {
                scrollContentPanel.add(cartItem, "wrap");
            }
        }
        scrollContentPanel.revalidate();
        scrollContentPanel.repaint();
    }

    private List<CartPanel> fetchCartItems() {
        List<CartPanel> cartItems = new ArrayList<>();

        try (Connection conn = Connexion.etablirConnexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM CARTITEM WHERE USERID = ?")) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int cartItemId = rs.getInt("CARTITEMID");
                int itemId = rs.getInt("ITEMID");
                int quantity = rs.getInt("quantity");

                try (PreparedStatement itemStmt = conn.prepareStatement("SELECT * FROM ITEM WHERE ITEMID = ?")) {
                    itemStmt.setInt(1, itemId);
                    ResultSet itemRs = itemStmt.executeQuery();
                    if (itemRs.next()) {
                        byte[] imageBytes = itemRs.getBytes("image");
                        String name = itemRs.getString("name");
                        String description = itemRs.getString("description");

                        CartPanel cartPanel = new CartPanel(cartItemId, imageBytes, name, description, quantity, this);
                        cartItems.add(cartPanel);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading cart items.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return cartItems;
    }

    private List<CartItemInfo> fetchCartItemInfo() {
        List<CartItemInfo> cartItems = new ArrayList<>();

        try (Connection conn = Connexion.etablirConnexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT ITEMID, quantity FROM CARTITEM WHERE USERID = ?")) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int itemId = rs.getInt("ITEMID");
                int quantity = rs.getInt("quantity");
                cartItems.add(new CartItemInfo(itemId, quantity));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading cart items.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return cartItems;
    }

    class CartItemInfo {
        private int itemID;
        private int quantity;

        public CartItemInfo(int itemID, int quantity) {
            this.itemID = itemID;
            this.quantity = quantity;
        }

        public int getItemID() {
            return itemID;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    private void checkout() {
        List<CartItemInfo> cartItemsInfo = fetchCartItemInfo();

        if (cartItemsInfo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Your cart is empty. Add items to the cart before checkout.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Connexion.etablirConnexion()) {
            if (hasPendingOrBeingPreparedOrder(conn)) {
                JOptionPane.showMessageDialog(null, "You already have an order in progress. Please wait until it is completed before placing a new order.", "Order In Progress", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateString = sdf.format(new Date());
            int count = fetchOrderCount(dateString);
            String ORDERID = "SH" + dateString + count;

            String insertOrderQuery = "INSERT INTO ORDER_TABLE (ORDERID, USERID, dt, orderstatus, orderprice) VALUES (?, ?, CURDATE(), ?, ?)";
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderQuery)) {
                orderStmt.setString(1, ORDERID);
                orderStmt.setInt(2, userID);
                orderStmt.setString(3, "Pending");
                orderStmt.setBigDecimal(4, calculateTotal());
                orderStmt.executeUpdate();
            }

            String insertOrderItemQuery = "INSERT INTO ORDERITEM (ORDERID, ITEMID, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement orderItemStmt = conn.prepareStatement(insertOrderItemQuery)) {
                for (CartItemInfo cartItemInfo : cartItemsInfo) {
                    orderItemStmt.setString(1, ORDERID);
                    orderItemStmt.setInt(2, cartItemInfo.getItemID());
                    orderItemStmt.setInt(3, cartItemInfo.getQuantity());
                    orderItemStmt.executeUpdate();
                }
            }

            String deleteCartItemsQuery = "DELETE FROM CARTITEM WHERE USERID = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteCartItemsQuery)) {
                deleteStmt.setInt(1, userID);
                deleteStmt.executeUpdate();
            }

            loadCartItems();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error validating order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasPendingOrBeingPreparedOrder(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM ORDER_TABLE WHERE USERID = ? AND (orderstatus = 'Pending' OR orderstatus = 'Being prepared' OR orderstatus = 'Ready')";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }


    private int fetchOrderCount(String dateString) throws SQLException {
        try (Connection conn = Connexion.etablirConnexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM ORDER_TABLE WHERE dt = CURDATE()")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void updateTotal() {
        BigDecimal total = calculateTotal();
        if (total != null) {
            totalLabel.setText("Total: " + total.setScale(2, RoundingMode.HALF_UP) + " DH");
        }
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;

        try (Connection conn = Connexion.etablirConnexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT SUM(ITEM.unitprice * CARTITEM.quantity) AS total FROM CARTITEM JOIN ITEM ON CARTITEM.ITEMID = ITEM.ITEMID WHERE USERID = ?")) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getBigDecimal("total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return total;
    }
}
