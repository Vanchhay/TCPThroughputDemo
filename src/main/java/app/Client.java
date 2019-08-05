package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final static String HOST = "localhost";
    private final static int PORT = 465;
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void main(String[] args) {

        try {
            Socket socket = new Socket(HOST, PORT);
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            byte[] msg = new byte[100];
            long msgCount = 0;
            long timeLimit = System.nanoTime();

            System.out.println("-----------: #_msg");
            //noinspection InfiniteLoopStatement
            while (true) {
                dis.readFully(msg);
                msgCount++;

                boolean elapsed = System.nanoTime() >= timeLimit;
                if (elapsed) {
                    timeLimit = System.nanoTime() + 1_000_000_000;
                    System.out.println(dateFormat.format(new Date()) + ": " + msgCount);
                    msgCount = 0;
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.toString());
        }
    }
}

