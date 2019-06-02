package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class Sender {
    @FXML
    AnchorPane anchorPane;
    @FXML
    Label label;
    @FXML
    TextField filePath, receiverIp;

    private static String filename;

    Controller controller=new Controller();

    File selectedFile;


    public void back() throws IOException {
        controller.changeMenu(anchorPane,"MainScreen.fxml");
    }
    public void selectFile(){
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            filePath.setText(selectedFile.getName());
            filename=selectedFile.getPath();
        }
        else {
            filePath.setText("File selection cancelled.");
        }
    }
    public void sendFile() throws IOException {

        FileSender fs = new FileSender(receiverIp.getText(),selectedFile);
        fs.send();
        fs.setFileSenderListener(new FileSender.FileSenderListener() {
            @Override
            public void fileSent(int var1) {

            }

            @Override
            public void errorOccurred(int var1) {

            }
        });
    }
}
