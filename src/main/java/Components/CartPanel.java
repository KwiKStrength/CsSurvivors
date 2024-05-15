package Components;

import Interface.InterfaceUser.CartForm;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import javax.imageio.ImageIO;
import Class.Connexion;
import net.miginfocom.swing.MigLayout;

public class CartPanel extends JPanel {

    private JLabel nameLabel;
    private JLabel descriptionLabel;
    private JTextField quantityField;

    public CartPanel(int id, byte[] imageBytes, String name, String description, int quantity, CartForm parentPanel) {
        setLayout(new MigLayout("insets 10", "[200][grow]", "[][]push[]"));
        setOpaque(false);
        setBorder(new RoundBorder(10));
        setMinimumSize(new Dimension(960, getHeight()));

        Image image = resizeImage(byteArrayToImage(imageBytes), 150, 150);
        if (image != null) {
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            add(imageLabel, "aligny top, spany 3");
        }

        nameLabel = new JLabel("Name: " + name);
        add(nameLabel, "wrap");

        descriptionLabel = new JLabel("Description: " + description);
        add(descriptionLabel, "wrap");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        JButton minusButton = new JButton("-");
        minusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity > 0) {
                    quantity--;
                    quantityField.setText(Integer.toString(quantity));
                    if (quantity == 0) {
                        Connection conn = null;
                        PreparedStatement stmt = null;
                        try {
                            conn = Connexion.etablirConnexion();
                            stmt = conn.prepareStatement("DELETE FROM CARTITEM WHERE CARTITEMID = ?");
                            stmt.setInt(1, id);
                            stmt.executeUpdate();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        } finally {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            if (conn != null) {
                                try {
                                    conn.close();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        parentPanel.loadCartItems();
                        parentPanel.updateTotal();
                    } else {
                        Connection conn = null;
                        PreparedStatement stmt = null;
                        try {
                            conn = Connexion.etablirConnexion();
                            stmt = conn.prepareStatement("UPDATE CARTITEM SET QUANTITY = ? WHERE CARTITEMID = ?");
                            stmt.setInt(1, quantity);
                            stmt.setInt(2, id);
                            stmt.executeUpdate();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        } finally {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            if (conn != null) {
                                try {
                                    conn.close();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        parentPanel.updateTotal();
                    }
                }
            }
        });
        buttonPanel.add(minusButton);

        quantityField = new JTextField(Integer.toString(quantity));
        quantityField.setHorizontalAlignment(JTextField.CENTER);
        quantityField.setPreferredSize(new Dimension(50, 25));
        quantityField.setEditable(false);
        buttonPanel.add(quantityField);

        JButton plusButton = new JButton("+");
        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int quantity = Integer.parseInt(quantityField.getText());
                quantity++;
                quantityField.setText(Integer.toString(quantity));

                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = Connexion.etablirConnexion();
                    stmt = conn.prepareStatement("UPDATE CARTITEM SET QUANTITY = ? WHERE CARTITEMID = ?");
                    stmt.setInt(1, quantity);
                    stmt.setInt(2, id);
                    stmt.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                parentPanel.updateTotal();
            }
        });
        buttonPanel.add(plusButton);

        add(buttonPanel, "alignx center, aligny bottom");
    }

    public static Image byteArrayToImage(byte[] byteArray) {
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
            ImageIcon imageIcon = new ImageIcon(bufferedImage);
            Image image = imageIcon.getImage();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Image resizeImage(Image img, int width, int height) {
        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImg;
    }

    public static class RoundBorder extends AbstractBorder {
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
