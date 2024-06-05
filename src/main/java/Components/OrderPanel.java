package Components;

import Class.Connexion;
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

public class OrderPanel extends JPanel {

    public static String role;

    public OrderPanel(int USERID, Date dt, String orderstatus, float orderprice, List<Integer> ITEMID, List<Integer> quantity, JScrollPane parent, String orderID, String role) {
        setLayout(new MigLayout("fillx, insets 9", "[grow][]", "[]10[][]"));
        setBorder(new RoundBorder(15));

        this.role = role;

        JLabel titleLabel = new JLabel(orderID);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +14");
        add(titleLabel, "span, gapbottom 10, wrap");

        JLabel dateLabel = new JLabel("Order Date: " + new SimpleDateFormat("dd-MM-yyyy").format(dt));
        dateLabel.putClientProperty(FlatClientProperties.STYLE, "font: regular +12");
        add(dateLabel, "span, gapbottom 10, wrap");

        JLabel statusLabel = new JLabel("Order Status: " + orderstatus);
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "font: italic +11");
        add(statusLabel, "span, gapbottom 10, wrap");

        JButton detailsButton = new JButton("Details");
        detailsButton.putClientProperty(FlatClientProperties.STYLE, "font: bold +13");
        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String role = OrderPanel.role;
                if (role.equals("student")) {
                    OrderDetails orderDetailsPanel = new OrderDetails(orderID, ITEMID, quantity, USERID, dt, orderprice, orderstatus);
                    JViewport viewport = parent.getViewport();
                    viewport.setView(orderDetailsPanel);
                } else if (role.equals("chef")) {
                    JViewport viewport = parent.getViewport();
                    OrderChef orderChefPanel = new OrderChef(orderID, ITEMID, quantity, dt, orderprice, orderstatus,viewport);
                    viewport.setView(orderChefPanel);
                }
            }
        });
        add(detailsButton, "align right");
    }

    public class RoundBorder extends AbstractBorder {
        private int radius;

        public RoundBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
