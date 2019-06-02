package sample;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FileReceiver implements Runnable {
    private String senderIP;
    private File file;
    private byte[] fileData;
    private DatagramSocket socket = new DatagramSocket(26100);
    private int seqNo;
    private String fileName;
    private int numOfPackets;
    private boolean receiving;
    private FileReceiverListener listener;

    public FileReceiver() throws SocketException {

    }

    public void listen() {
        new Thread(this).start();
    }

    public boolean isReceiving() {
        return this.receiving;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void abort() {
        this.seqNo = -1;
        this.sendAck();
        this.close();
    }

    public void close() {
        this.receiving = false;
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
    }

    @Override
    public void run() {
        if (!this.waitForInit()) {
            return;
        }
        this.seqNo = 0;
        this.receiving = true;
        this.listener.fileReceived(0);
        do {
            this.waitForPacket();
            this.sendAck();
        } while (this.seqNo < this.numOfPackets - 1 && this.receiving);
        if (this.receiving) {
            TransferUtils.bytesToFile(this.fileData, this.file);
        }
        this.close();
    }

    private boolean waitForInit() {
        byte[] buffer = new byte[128];
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
            }
            catch (SocketException se) {
                return false;
            }
            String msg = new String(packet.getData()).trim();
            this.fileName = msg.substring(0, msg.indexOf(124));
            int fileSize = Integer.parseInt(msg.substring(msg.indexOf(124) + 1));
            this.senderIP = packet.getAddress().getHostAddress();
            this.numOfPackets = (int)Math.ceil((double)fileSize / 1024.0);
            this.fileData = new byte[fileSize];
            /****************/

            /******************/

            boolean choice = this.listener.fileInfoReceived(this.fileName, fileSize);
            if (choice) {
                this.responseInit("x_ACCEPT_x");
                return true;
            }
            this.responseInit("x_REFUSE_x");
            this.close();
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void responseInit(String message) {
        byte[] data = message.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.senderIP), 26000);
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForPacket() {
        byte[] buffer = new byte[1032];
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("Waiting for packet...");
            try {
                this.socket.receive(packet);
            }
            catch (SocketException se) {
                return;
            }
            byte[] seqBytes = new byte[4];
            System.arraycopy(packet.getData(), 0, seqBytes, 0, seqBytes.length);
            byte[] lengthBytes = new byte[4];
            System.arraycopy(packet.getData(), 4, lengthBytes, 0, lengthBytes.length);
            this.seqNo = TransferUtils.bytesToInt(seqBytes);
            int bytesLength = TransferUtils.bytesToInt(lengthBytes);
            byte[] bytes = new byte[bytesLength];
            System.arraycopy(packet.getData(), 8, bytes, 0, bytes.length);
            if (this.seqNo == -1 && bytesLength == 0) {
                this.receiving = false;
                this.listener.errorOccurred(2);
                return;
            }
            System.arraycopy(bytes, 0, this.fileData, this.seqNo * 1024, bytesLength);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendAck() {
        byte[] seqBytes = TransferUtils.intToBytes(this.seqNo);
        try {
            DatagramPacket packet = new DatagramPacket(seqBytes, seqBytes.length, InetAddress.getByName(this.senderIP), 26000);
            System.out.println("Ack Sending...");
            try {
                this.socket.send(packet);
            }
            catch (SocketException se) {}
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.listener.fileReceived((int)(100.0 * ((double)(this.seqNo + 1) / (double)this.numOfPackets)));
    }

    public void setFileReceiverListener(FileReceiverListener l) {
        this.listener = l;
    }

    public static interface FileReceiverListener {
        public boolean fileInfoReceived(String var1, int var2);

        public void fileReceived(int var1);

        public void errorOccurred(int var1);
    }

}

