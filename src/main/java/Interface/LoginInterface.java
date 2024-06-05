package Interface;

import Components.LoginHeaderButton;
import Interface.LoginInterfaceForms.Login;
import Interface.LoginInterfaceForms.Register;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.imageio.ImageIO;
import javax.management.relation.Relation;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LoginInterface extends JFrame {

    Background background;
    private LoginInterface loginInterface;

    public LoginInterface(){
        this.loginInterface = this;
        init();
    }

    private void init(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(UIScale.scale(new Dimension(1280,720)));
        setLocationRelativeTo(null);
        background = new Background(this.loginInterface);
        setContentPane(background);
        addWindowListener(new LoginInterfaceAdapter());
    }

    public static void main(String[] args) {
        //vlc on mac
        System.setProperty("jna.library.path", "/Applications/VLC.app/Contents/MacOS/lib");
        FlatInterFont.install();
        FlatMacDarkLaf.setup();
        UIManager.put("defaultFont", new Font(FlatInterFont.FAMILY, Font.PLAIN, 13));
        EventQueue.invokeLater(() -> new LoginInterface().setVisible(true));
    }

    class LoginInterfaceAdapter extends WindowAdapter {

        @Override
        public void windowOpened(WindowEvent e) {
            background.Overlayin(LoginInterface.this);
            background.play();
        }
        @Override
        public void windowClosing(WindowEvent e) {
            background.pause();
        }

    }

    public class Background extends JPanel {

        private Overlay overlay;
        private MediaPlayerFactory factory;
        private EmbeddedMediaPlayer mediaPlayer;
        private LoginInterface loginInterface;

        public Background(LoginInterface loginInterface){
            this.loginInterface = loginInterface;
            init();
        }

        private void init(){
            factory = new MediaPlayerFactory();
            mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
            Canvas canvas = new Canvas();
            mediaPlayer.videoSurface().set(factory.videoSurfaces().newVideoSurface(canvas));
            setLayout(new BorderLayout());
            add(canvas, BorderLayout.CENTER);
            mediaPlayer.audio().setMute(true);
            mediaPlayer.controls().setRepeat(true);
        }

        public void play(){
            if(mediaPlayer.status().isPlaying()){
                mediaPlayer.controls().stop();
            }
            //version mac
            //mediaPlayer.media().play("Video/BackgroundLogin.mp4");
            mediaPlayer.media().play("Video/BackgroundLogin.mp4");
        }
        public void pause(){
            mediaPlayer.release();
            factory.release();
        }

        public void Overlayin(JFrame frame){
            overlay = new Overlay(frame,this.loginInterface);
            mediaPlayer.overlay().set(overlay);
            mediaPlayer.overlay().enable(true);
        }
    }

    public class Overlay extends JWindow{

        private PanelOverlay overlay;
        private LoginInterface loginInterface;

        public Overlay(JFrame frame,LoginInterface loginInterface){
            super(frame);
            this.loginInterface = loginInterface;
            init();
        }

        private void init(){
            setBackground(new Color(67,80,88,100));
            setLayout(new BorderLayout());
            overlay = new PanelOverlay(this.loginInterface);
            add(overlay);
        }

        public class PanelOverlay extends JPanel{

            private JPanel header;
            private JPanel information;
            private Login loginPanel;
            private Register registerPanel;
            private Animator registerAnimator;
            private Animator loginAnimator;
            private boolean showLogin;
            private boolean showRegister;
            private MigLayout migLayout;
            private LoginInterface loginInterface;
            private PanelOverlay parentOverlay;

            public PanelOverlay(LoginInterface loginInterface){
                this.loginInterface = loginInterface;
                parentOverlay = this;
                init();
            }

            private void init(){
                setOpaque(false);
                migLayout = new MigLayout("fill,insets 10 10 10 10","[]push[]","[grow 0]20[][20]");
                setLayout(migLayout);
                image();
                header();
                information();
                loginButton();
                registerButton();

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        runLoginAnimation(false);
                        runRegisterAnimation(false);
                    }
                });

                loginAnimator = new Animator(500, new Animator.TimingTarget() {
                    @Override
                    public void timingEvent(float v) {
                        float f = showLogin ? v : 1f - v;
                        int x = (int) ((350+50) * f);
                        migLayout.setComponentConstraints(loginPanel, "pos 100%-" + x + " 0.5al, w 350");
                        revalidate();
                    }
                });
                loginAnimator.setInterpolator(CubicBezierEasing.EASE);

                registerAnimator = new Animator(500, new Animator.TimingTarget() {
                    @Override
                    public void timingEvent(float v) {
                        float f = showRegister ? v : 1f - v;
                        int x = (int) ((350+50) * f);
                        migLayout.setComponentConstraints(registerPanel, "pos 100%-" + x + " 0.5al, w 350");
                        revalidate();
                    }
                });


            }

            private void image() {
                BufferedImage logoImage = null;
                try {
                    logoImage = ImageIO.read(new File("src/main/resources/Image/Logo.png"));
                    //version mac
                    //logoImage = ImageIO.read(new File("Image/Logo.png"));
                    int width = 128;
                    int height = 128;
                    BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics2D = scaledImage.createGraphics();
                    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics2D.drawImage(logoImage, 0, 0, width, height, null);
                    graphics2D.dispose();
                    ImageIcon resizedIcon = new ImageIcon(scaledImage);
                    JLabel logoLabel = new JLabel(resizedIcon);
                    add(logoLabel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



            private void loginButton(){
                loginPanel = new Login(this.loginInterface,parentOverlay);
                add(loginPanel, "pos 100% 0.5al,w 350");
            }

            private void registerButton(){
                registerPanel = new Register(parentOverlay);
                add(registerPanel, "pos 100% 0.5al,w 350");
            }

            private void information(){
                information = new JPanel(new MigLayout("wrap","","[]30[]"));
                information.setOpaque(false);
                JLabel bigTitle = new JLabel("Campus grill !");
                bigTitle.putClientProperty(FlatClientProperties.STYLE,""+"font:bold +40");
                JLabel description = new JLabel("Notre Application !");
                description.putClientProperty(FlatClientProperties.STYLE,""+"Font:bold +3");
                information.add(bigTitle);
                information.add(description);
                add(information,"width 40%");
            }

            private void header(){
                header = new JPanel(new MigLayout("fill","push[][][]"));
                header.setOpaque(false);
                LoginHeaderButton HomeMenu = new LoginHeaderButton("Home");
                LoginHeaderButton LoginMenu = new LoginHeaderButton("Login");

                LoginMenu.addActionListener(e -> {
                    runLoginAnimation(true);
                    runRegisterAnimation(false);
                });

                LoginHeaderButton RegisterMenu = new LoginHeaderButton("Register");

                RegisterMenu.addActionListener(e -> {
                    runRegisterAnimation(true);
                    runLoginAnimation(false);
                });

                HomeMenu.addActionListener(e -> {
                    runLoginAnimation(false);
                    runRegisterAnimation(false);
                });

                header.add(HomeMenu);
                header.add(LoginMenu);
                header.add(RegisterMenu);
                add(header,"wrap");
            }

            private void runLoginAnimation(boolean show) {
                if (showLogin != show) {
                    if (!loginAnimator.isRunning()) {
                        showLogin = show;
                        loginAnimator.start();
                    }
                }
            }

            private void runRegisterAnimation(boolean show) {
                if (showRegister != show) {
                    if (!registerAnimator.isRunning()) {
                        showRegister = show;
                        registerAnimator.start();
                    }
                }
            }


        }
    }
}
