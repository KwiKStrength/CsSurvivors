package Components;

import javax.swing.*;
import javax.swing.border.AbstractBorder;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import Class.Connexion;

public class OrderChef extends JPanel {

    private String orderId;
    private JLabel statusLabel;
    private String orderStatus;
    private JLabel dateLabel;

    public OrderChef(String ORDERID, List<Integer> ITEMIDList, List<Integer> quantityList, Date dt, float orderprice, String orderstatus, JViewport viewport) {
        this.orderId = ORDERID;
        this.orderStatus = orderstatus;
        setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 15", "[left]push[right]", "[]10[]10[]20[][]20[]"));
        contentPanel.setPreferredSize(new Dimension(400, 200));

        setBorder(new RoundBorder(15));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = sdf.format(dt);

        JLabel titleLabel = new JLabel(ORDERID);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +18");
        contentPanel.add(titleLabel, "span, wrap");

        statusLabel = new JLabel(orderstatus);
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "font:italic +13");
        contentPanel.add(statusLabel, "span, wrap");

        dateLabel = new JLabel(dateString);
        dateLabel.putClientProperty(FlatClientProperties.STYLE, "font:plain +13");
        contentPanel.add(dateLabel, "span, wrap");

        try {
            Connection conn = Connexion.etablirConnexion();
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM ITEM WHERE ITEMID = ?");
            for (int i = 0; i < ITEMIDList.size(); i++) {
                int ITEMID = ITEMIDList.get(i);
                int quantity = quantityList.get(i);
                stmt.setInt(1, ITEMID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String itemName = rs.getString("name");
                    String itemDescription = quantity + "x " + itemName;
                    JLabel itemLabel = new JLabel(itemDescription);
                    itemLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +12");
                    contentPanel.add(itemLabel, "span, wrap");
                }
                rs.close();
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton changeStatusButton1 = new JButton("<");
        changeStatusButton1.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        changeStatusButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeOrderStatus(1);
            }
        });
        bottomPanel.add(changeStatusButton1);

        JButton changeStatusButton2 = new JButton(">");
        changeStatusButton2.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        changeStatusButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeOrderStatus(0);
            }
        });
        bottomPanel.add(changeStatusButton2);

        JLabel totalLabel = new JLabel("TOTAL PRICE : ");
        totalLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +13");
        bottomPanel.add(totalLabel);

        JLabel totalPriceLabel = new JLabel(Float.toString(orderprice) + " DH");
        totalPriceLabel.putClientProperty(FlatClientProperties.STYLE, "font:plain +13");
        bottomPanel.add(totalPriceLabel);

        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void changeOrderStatus(int direction) {
        String newStatus = "";
        if (orderStatus.equals("Pending")) {
            if (direction == 0) {
                newStatus = "Being Prepared";
            } else {
                newStatus = "Pending";
            }
        } else if (orderStatus.equals("Being Prepared")) {
            if (direction == 0) {
                newStatus = "Ready";
            } else {
                newStatus = "Pending";
            }
        } else if (orderStatus.equals("Ready")) {
            if (direction == 0) {
                newStatus = "Delivered";
            } else {
                newStatus = "Being Prepared";
            }
        } else if (orderStatus.equals("Delivered")) {
            if (direction == 0) {
                newStatus = "Delivered";
            } else {
                newStatus = "Ready";
            }
        }
        updateOrderStatus(newStatus);
        statusLabel.setText(newStatus);
        orderStatus = newStatus;
    }

    private void updateOrderStatus(String newStatus) {
        try {
            Connection conn = Connexion.etablirConnexion();
            PreparedStatement stmt = conn.prepareStatement("UPDATE ORDER_TABLE SET orderstatus = ? WHERE ORDERID = ?");
            stmt.setString(1, newStatus);
            stmt.setString(2, orderId);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public class RoundBorder extends AbstractBorder {
        private int radius;

        public RoundBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius, this.radius, this.radius, this.radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = this.radius;
            return insets;
        }
    }
}
