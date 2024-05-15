package Interface.InterfaceUser;

import Components.CartPanel;
import Components.ItemPanel;
import Class.Connexion;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemForm extends JScrollPane {

    private List<ItemPanel> pdjItems;
    private List<ItemPanel> nonPdjItems;

    public ItemForm(int id,int userID) {
        super();
        FlatInterFont.install();
        pdjItems = new ArrayList<>();
        nonPdjItems = new ArrayList<>();

        fItems(id,userID);
        setupLayout();
    }

    private void fItems(int categoryId, int userID) {
        try (Connection connection = Connexion.etablirConnexion();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM ITEM WHERE CATEGORYID = ?")) {
            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int itemId = resultSet.getInt("ITEMID");
                byte[] imageBytes = resultSet.getBytes("image");
                String name = resultSet.getString("name");
                float price = resultSet.getFloat("unitprice");
                String description = resultSet.getString("description");
                boolean pdj = resultSet.getBoolean("pdj");
                int availability = resultSet.getInt("availability");

                ItemPanel itemPanel = new ItemPanel(itemId, imageBytes, name, price, description, pdj, availability,userID);
                if (pdj) {
                    pdjItems.add(itemPanel);
                } else {
                    nonPdjItems.add(itemPanel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupLayout() {
        JPanel contentPanel = new JPanel(new MigLayout("wrap 1","[grow,fill]","10[]5[]10[]5[]"));
        contentPanel.setBorder(new CartPanel.RoundBorder(10));
        if (!pdjItems.isEmpty()) {
            JLabel titleLabel = new JLabel("Plats du jour");
            titleLabel.putClientProperty(FlatClientProperties.STYLE,""+"font:bold +16");
            contentPanel.add(titleLabel, "wrap");

            JScrollPane pdjScrollPane = createHorizontalScrollPane(pdjItems);
            contentPanel.add(pdjScrollPane, "wrap");
        }

        if (!nonPdjItems.isEmpty()) {
            JLabel titleLabel = new JLabel("Plat");
            titleLabel.putClientProperty(FlatClientProperties.STYLE,""+"font:bold +16");
            contentPanel.add(titleLabel, "wrap,growx");

            JScrollPane nonPdjScrollPane = createVerticalScrollPane(nonPdjItems);
            contentPanel.add(nonPdjScrollPane, "wrap,growx");
        }

        setViewportView(contentPanel);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JScrollPane createHorizontalScrollPane(List<ItemPanel> items) {
        JPanel panel = new JPanel(new MigLayout("wrap"));
        for (ItemPanel itemPanel : items) {
            panel.add(itemPanel);
        }
        JScrollPane scrollPane = new RoundScrollPane(panel, 15);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JScrollPane createVerticalScrollPane(List<ItemPanel> items) {
        JPanel panel = new JPanel(new MigLayout("wrap 1"));
        for (ItemPanel itemPanel : items) {
            panel.add(itemPanel, "wrap");
        }
        JScrollPane scrollPane = new RoundScrollPane(panel, 15);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    public class RoundScrollPane extends JScrollPane {

        private int cornerRadius;

        public RoundScrollPane(Component view, int cornerRadius) {
            super(view);
            this.cornerRadius = cornerRadius;
            setOpaque(false);
            getViewport().setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            int arc = cornerRadius * 2;
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public void setBorder(Border border) {
        }
    }


}

