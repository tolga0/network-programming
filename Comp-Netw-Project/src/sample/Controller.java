package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class Controller {
    @FXML
    AnchorPane anchorPane;

    public void changeMenu (AnchorPane scene, String fxml) throws IOException, IOException {
        AnchorPane pane= FXMLLoader.load(getClass().getResource(fxml));
        scene.getChildren().setAll(pane);
    }
}
