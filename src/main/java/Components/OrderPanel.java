package Components;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import java.util.List;

import Class.Connexion;

public class OrderPanel extends JPanel {

    private int count;

    public OrderPanel(int USERID, Date dt, String orderstatus, float orderprice, List<Integer> ITEMID, List<Integer> quantity, JScrollPane parent) {
        setLayout(new MigLayout("fillx"));
        setBorder(new RoundBorder(10));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateString = sdf.format(dt);

        try {
            Connection conn = Connexion.etablirConnexion();
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM ORDER_TABLE WHERE dt = CURDATE()");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String title = "SH" + dateString + count;
        JLabel titleLabel = new JLabel(title);
        titleLabel.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "font:bold 20px;");
        add(titleLabel, "span, gapbottom 10");

        JLabel statusLabel = new JLabel("Order Status: " + orderstatus);
        statusLabel.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "font:bold;");
        add(statusLabel, "span, gapbottom 10");

        JButton detailsButton = new JButton("...");
        detailsButton.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "font:bold 16px;");
        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OrderDetails orderDetailsPanel = new OrderDetails(title, ITEMID, quantity, USERID, dt, orderprice, orderstatus);

                JViewport viewport = parent.getViewport();

                viewport.setView(orderDetailsPanel);
            }
        });
        add(detailsButton, "split, aligny center, gapleft 5");

        add(Box.createHorizontalGlue(), "growx");

        setPreferredSize(new Dimension(1000, 300));
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
