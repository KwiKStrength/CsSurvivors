package Interface.InterfaceAdmin.interfaces.panes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class UserPane extends AnchorPane {

    @FXML
    private Label usernameLabel;

    public UserPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FX/panes/UserPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
}
}