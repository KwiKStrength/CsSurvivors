package Components;

import Effect.RippleEffect;
import Interface.InterfaceUser.ItemForm;
import Interface.InterfaceUser.MenuForm;
import Interface.InterfaceUser.MenuInterface;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class MenuButton extends JButton {

    private int id;
    private JLabel titre;
    private byte[] image;
    private RippleEffect effect;

    public MenuButton(int id,byte[] image, String text,int userID) {
        setOpaque(false);
        this.image = image;
        this.id = id;
        titre = new JLabel(text);
        titre.putClientProperty(FlatClientProperties.STYLE, "font:bold +30");
        setLayout(new MigLayout("center, wrap"));
        add(titre);

        effect = new RippleEffect(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MenuInterface.showForm(new ItemForm(id,userID));
            }
        });

        setBorder(new RoundedBorder(60));
        setPreferredSize(new Dimension(1000, 50));
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


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int arc = UIScale.scale(20);
        effect.render(g, new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.clip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 4 * 40, 4 * 40));
        super.paintComponent(g);

        Graphics2D g2d1 = (Graphics2D) g;
        g2d1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d1.setColor(Color.BLACK);
        g2d1.setStroke(new BasicStroke(5));
        g2d1.draw(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 4 * 40, 4 * 40));

        if (image != null) {
            Image img = byteArrayToImage(image);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }





    private class RoundedBorder extends AbstractBorder {
        private final int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = radius;
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }
}
