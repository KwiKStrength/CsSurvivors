package Interface.InterfaceAdmin.interfaces;

import Interface.InterfaceAdmin.controllers.DashboardController;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Dashboard extends Application {

    private static int userID;

    public static void main(String[] args) {
        launch(args);
    }

    public static void launchDashboard(int userID) {
        Dashboard.userID = userID;
        Application.launch(Dashboard.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FX/dashboard.fxml"));
        Parent root = loader.load();
        DashboardController controller = loader.getController();
        controller.setUserID(userID);

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Dashboard");
        primaryStage.setScene(new Scene(root, 1200, 600));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();

        // Set up MaterialFX theme at the top level of your application
        UserAgentBuilder.builder().themes(JavaFXThemes.MODENA).themes(MaterialFXStylesheets.forAssemble(true)).setDeploy(true).setResolveAssets(true).build().setGlobal();
    }
}