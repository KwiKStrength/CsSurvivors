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
    private JPanel cartPanel;
    private JButton checkoutButton;
    private JLabel totalLabel;

    public CartForm(int userID) {
        FlatInterFont.install();
        this.userID = userID;
        init();
    }

    private void init() {
        setLayout(new MigLayout("insets 10", "[grow]", "[grow]10[]"));
        cartPanel = this;
        scrollPane = new JScrollPane();
        scrollPane.setMaximumSize(new Dimension(1000,600));
        scrollPane.setViewportView(new JPanel(new MigLayout("wrap 1, gap 10", "[]", "[grow][]")));

        checkoutButton = new JButton("Checkout");
        checkoutButton.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:#00C14F;" +
                "borderWidth:0;" +
                "foreground:#FFFFFF;" +
                "font:bold +16;");

        checkoutButton.addActionListener(e -> {
            removeAll();
            checkout();
            revalidate();
            repaint();
                }
        );

        totalLabel = new JLabel();
        totalLabel.putClientProperty(FlatClientProperties.STYLE,""+
                "font:light +16");
        updateTotal();

        loadCartItems();
    }

    public void loadCartItems() {
        JPanel panel = (JPanel) scrollPane.getViewport().getView();
        panel.removeAll();
        List<CartPanel> cartItems = fetchCartItems();

        if (cartItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("EMPTY CART");
            emptyLabel.putClientProperty(FlatClientProperties.STYLE,"font:light +28");
            cartPanel.add(emptyLabel, "align center");
        } else {
            cartPanel.add(scrollPane, "grow, wrap");
            for (CartPanel cartItem : cartItems) {
                panel.add(cartItem, "wrap, alignx left, top");
            }
            JPanel totalPanel = new JPanel(new MigLayout("insets 0"));
            totalPanel.add(totalLabel);
            cartPanel.add(totalPanel, "split");
            JPanel buttonPanel = new JPanel(new MigLayout("insets 0"));
            buttonPanel.add(checkoutButton);
            cartPanel.add(buttonPanel);
        }
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
        try (Connection conn = Connexion.etablirConnexion()) {
            List<CartPanel> cartItems = fetchCartItems();

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

            List<CartItemInfo> cartItemsInfo = fetchCartItemInfo();

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
