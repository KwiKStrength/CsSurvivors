package Interface.InterfaceUser;

import Interface.LoginInterface;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;

public class MenuInterface extends JFrame {
    private static MenuInterface app;
    private final MainForm mainForm;
    private MenuInterface parent;

    public MenuInterface(int USERID, LoginInterface loginInterface) {
        app = this;
        this.parent = this;
        init();
        FlatInterFont.install();
        FlatLaf.registerCustomDefaultsSource("Theme");
        UIManager.put("defaultFont", new Font(FlatInterFont.FAMILY, Font.PLAIN, 13));
        FlatMacDarkLaf.setup();
        mainForm = new MainForm(USERID,loginInterface,parent);
        add(mainForm);
    }

    private void init() {
        setTitle("Menu Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setUndecorated(true);
        setSize(UIScale.scale(new Dimension(1280,720)));
        setLocationRelativeTo(null);
    }

    public static void showForm(Component component) {
        component.applyComponentOrientation(app.getComponentOrientation());
        app.mainForm.showForm(component);
    }

}
