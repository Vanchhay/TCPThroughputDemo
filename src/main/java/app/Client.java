package app;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client extends Thread {

    private final static String HOST = "localhost";
    private final static int PORT = 465;
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(HOST, PORT);
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        byte[] msg = new byte[13]; // "dummy message".length
        int msgCount = 0;
        long timeLimit = System.currentTimeMillis();

        System.out.println("-----------: #_msg");
        //noinspection InfiniteLoopStatement
        while (true) {
            dis.readFully(msg);
            msgCount++;

            boolean elapsed = System.currentTimeMillis() >= timeLimit;
            if (elapsed) {
                timeLimit = System.currentTimeMillis() + 1000;
                System.out.println(dateFormat.format(new Date()) + ": " + msgCount);
                msgCount = 0;
            }
        }
    }
}

