package Components;
import com.formdev.flatlaf.FlatClientProperties;
import Class.Connexion;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class ItemPanel extends JPanel {

    private final int userID5;
    private final int itemID;

    public ItemPanel(int id, byte[] imageBytes, String name, float price, String description, boolean pdj, int availability, int userID) {
        this.userID5 = userID;
        this.itemID = id;
        System.out.println(userID5 + itemID);

        setLayout(null);
        setBorder(new CartPanel.RoundBorder(20));
        setPreferredSize(new Dimension(300, 420));

        Image icon = createImageIcon(imageBytes, 200, 200); // Change this line
        JLabel imageLabel = new JLabel(new ImageIcon(icon));
        imageLabel.setBounds(50, 30, 200, 200);
        imageLabel.setBorder(new CartPanel.RoundBorder(10));
        add(imageLabel);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setBounds(10, 250, 290, 40);
        nameLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +16");
        add(nameLabel);

        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBounds(5, 290, 260, 50);
        add(descriptionArea);

        JLabel priceLabel = new JLabel(price + " DH");
        priceLabel.setBounds(20, 370, 200, 20);
        priceLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FFAC1C;font:bold +11;");
        add(priceLabel);

        JButton addButton = new JButton("Add");
        addButton.setBounds(160, 365, 100, 30);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(userID5 + itemID);
                addToCart(userID5, itemID);
            }
        });
        add(addButton);
    }

    private Image createImageIcon(byte[] byteArray, int targetWidth, int targetHeight) {
        if (byteArray == null || byteArray.length == 0) {
            return null;
        }
        try {
            byteArray = Base64.getDecoder().decode(byteArray);
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            BufferedImage bufferedImage = ImageIO.read(bis);
            bis.close();
            if (bufferedImage == null) {
                return null;
            }

            // Calculate the new dimensions while maintaining aspect ratio
            int originalWidth = bufferedImage.getWidth();
            int originalHeight = bufferedImage.getHeight();
            float aspectRatio = (float) originalWidth / originalHeight;

            int newWidth = targetWidth;
            int newHeight = (int) (targetWidth / aspectRatio);
            if (newHeight > targetHeight) {
                newHeight = targetHeight;
                newWidth = (int) (targetHeight * aspectRatio);
            }

            // Resize the image
            Image resizedImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedBufferedImage.createGraphics();
            g2d.drawImage(resizedImage, 0, 0, null);
            g2d.dispose();

            return resizedBufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addToCart(int user, int item) {
        try {
            Connection connection = Connexion.etablirConnexion();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM CARTITEM WHERE ITEMID = ? AND USERID = ?");
            statement.setInt(1, item);
            statement.setInt(2, user);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int quantity = resultSet.getInt("QUANTITY") + 1;
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE CARTITEM SET QUANTITY = ? WHERE ITEMID = ? AND USERID = ?");
                updateStatement.setInt(1, quantity);
                updateStatement.setInt(2, item);
                updateStatement.setInt(3, user);
                updateStatement.executeUpdate();
            } else {
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO CARTITEM (ITEMID, USERID, QUANTITY) VALUES (?, ?, 1)");
                insertStatement.setInt(1, item);
                insertStatement.setInt(2, user);
                insertStatement.executeUpdate();
            }
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

}
