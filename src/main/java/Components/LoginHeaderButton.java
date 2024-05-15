package Components;

import Effect.RippleEffect;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.lang.foreign.MemoryLayout;

public class LoginHeaderButton extends JButton {

    RippleEffect effect;

    public LoginHeaderButton(String string) {
        super(string);
        init();
    }
    private void init() {
        effect = new RippleEffect(this);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        putClientProperty(FlatClientProperties.STYLE,""+"font:bold +3");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int arc = UIScale.scale(20);
        effect.render(g, new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
    }
}
