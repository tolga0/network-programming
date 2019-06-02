package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainScreen implements Initializable {

    @FXML
    AnchorPane anchorPane;
    @FXML
    Button sendFile, receiveFile;
    @FXML
    ChoiceBox  choiceBox;

    Controller controller = new Controller();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox.getItems().add("TCP");
        choiceBox.getItems().add("UDP");
        choiceBox.getSelectionModel().selectFirst();
    }
    public void sendFile() throws IOException {
        if (choiceBox.getValue() == "UDP"){
            controller.changeMenu(anchorPane,"sendScreen.fxml");
        }else {
            controller.changeMenu(anchorPane,"Server.fxml");
        }

    }
    public void receiveFile() throws IOException {
        if(choiceBox.getValue()== "UDP")
            controller.changeMenu(anchorPane,"receiveScreen.fxml");
        else
            controller.changeMenu(anchorPane,"Client.fxml");

    }
}
