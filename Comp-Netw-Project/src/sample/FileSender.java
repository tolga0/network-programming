package sample;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class FileSender
        implements Runnable {
    private String receiverIP;
    private DatagramSocket socket;
    private File file;
    private byte[] fileData;
    private int seqNo;
    private int numOfPackets;
    private FileSenderListener listener;
    private boolean sending;

    public FileSender(String receiverIP, File file) throws IOException {
        this.receiverIP = receiverIP;
        this.file = file;
        this.fileData = TransferUtils.fileToBytes(file);
        this.seqNo = 0;
        this.numOfPackets = (int)Math.ceil((double)this.fileData.length / 1024.0);
        this.sending = false;
        this.socket = new DatagramSocket(26000);
    }

    public void send() {
        new Thread(this).start();
    }

    public boolean isSending() {
        return this.sending;
    }

/*    public void abort() {
        if (!this.sending) {
            return;
        }
        this.seqNo = -1;
        this.sendPacket(new byte[0]);
        this.sending = false;
    }*/

    public void close() {
        this.sending = false;
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
    }

    @Override
    public void run() {
        this.sendInit();
        int response = this.waitForInit();
        if (response == 1) {
            return;
        }
        this.seqNo = 0;
        this.sending = true;
        //this.listener.fileSent(0);
        try {
            this.socket.setSoTimeout(5000);
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[1024];
        while (this.seqNo < this.numOfPackets && this.sending) {
            int length = data.length;
            if (this.seqNo == this.numOfPackets - 1) {
                length = this.fileData.length - this.seqNo * 1024;
                data = new byte[length];
            }
            System.arraycopy(this.fileData, this.seqNo * 1024, data, 0, length);
            this.sendPacket(data);
            this.waitForAck();
        }
        this.close();
    }

    private void sendInit() {
        String initMsg = this.file.getName() + "|" + this.fileData.length;
        byte[] data = initMsg.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.receiverIP), 26100);
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int waitForInit() {
        byte[] buffer = new byte[64];
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
            }
            catch (SocketException ste) {

            }
//            catch (SocketTimeoutException ste) {
//                this.listener.errorOccurred(3);
//                return 3;
//            }
//            catch (SocketException se) {
//                return -1;
//            }
//            String msg = new String(packet.getData()).trim();
//            if (msg.equals("x_ACCEPT_x")) {
//                return 0;
//            }
//            if (msg.equals("x_REFUSE_x")) {
//                this.listener.errorOccurred(1);
//                return 1;
//            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private synchronized void sendPacket(byte[] bytes) {
        byte[] seqBytes = TransferUtils.intToBytes(this.seqNo);
        byte[] lengthBytes = TransferUtils.intToBytes(bytes.length);
        byte[] data = TransferUtils.createPacket(seqBytes, lengthBytes, bytes);
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.receiverIP), 26100);
            System.out.println("Sending packet...");
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean waitForAck() {
        try {
            int receivedSeqNo;
            do {
                byte[] seqBytes = new byte[4];
                DatagramPacket packet = new DatagramPacket(seqBytes, seqBytes.length);
                try {
                    System.out.println("Waiting for Ack...");
                    this.socket.receive(packet);
                }
                catch (SocketTimeoutException ste) {
                    return false;
                }
                catch (SocketException se) {
                    return false;
                }
                receivedSeqNo = TransferUtils.bytesToInt(packet.getData());
                if (receivedSeqNo != this.seqNo) continue;
                ++this.seqNo;
                this.listener.fileSent((int)(100.0 * ((double)this.seqNo / (double)this.numOfPackets)));
                return true;
            } while (receivedSeqNo != -1);
            this.sending = false;
            this.listener.errorOccurred(2);
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setFileSenderListener(FileSenderListener l) {
        this.listener = l;
    }

    public static interface FileSenderListener {
        public void fileSent(int var1);

        public void errorOccurred(int var1);
    }

}

