package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class Receiver {
    @FXML
    AnchorPane anchorPane;

    Controller controller=new Controller();
    private FileReceiver fileReceiver;
    public void back() throws IOException {
        controller.changeMenu(anchorPane,"MainScreen.fxml");
    }
    public void receiveFile() throws SocketException {

        System.out.println("waiting!!");
        FileReceiver fr = new FileReceiver();
        File file = new File("C:\\Users\\Tolga\\Desktop\\BIM302_2019_file-UDP.jpg");
        fr.setFile(file);
        fr.setFileReceiverListener(new FileReceiver.FileReceiverListener() {
            @Override
            public boolean fileInfoReceived(String var1, int var2) {
                return true;
            }

            @Override
            public void fileReceived(int var1) { }

            @Override
            public void errorOccurred(int var1) { }
        });
        fr.listen();

    }

}
