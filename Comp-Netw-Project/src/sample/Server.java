package sample;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    @FXML
    Label label;
    @FXML
    TextField filePath;
    @FXML
    AnchorPane anchorPane;
    @FXML
    Button browserFile;
    private static File selectedFile;
    public final static int SOCKET_PORT = 13267; // you may change this
    public  static String FILE_TO_SEND; // you may change this

    FileInputStream fis = null;
    BufferedInputStream bis = null;
    OutputStream os = null;
    ServerSocket servsock = null;
    Socket sock = null;


    public void send() {
        {
            new Thread(this).start();
        }
    }

    public void selectFile(){
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {

            filePath.setText(selectedFile.getName());
            FILE_TO_SEND=selectedFile.getPath();
        }
        else {
            filePath.setText("File selection cancelled.");
        }
    }

    @Override
    public void run() {
        try {
            servsock = new ServerSocket(SOCKET_PORT);
            while (true) {
                System.out.println("Waiting...");
                try {
                    sock = servsock.accept();
                    System.out.println("Accepted connection : " + sock);
                    // send file
                    File myFile = new File (selectedFile.getPath());
                    byte [] mybytearray = new byte [(int)myFile.length()];
                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);
                    bis.read(mybytearray,0,mybytearray.length);
                    os = sock.getOutputStream();
                    //   System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");

                    os.write(mybytearray,0,mybytearray.length);
                    os.flush();
                    System.out.println("Done.");
                }
                finally {
                    if (bis != null) bis.close();
                    if (os != null) os.close();
                    if (sock!=null) sock.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (servsock != null) {
                try {
                    servsock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void back() throws IOException {
        Controller controller = new Controller();
        controller.changeMenu(anchorPane, "MainScreen.fxml");
    }
}