package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Client2 extends Thread{

    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final static String HOST = "localhost";
    private final static int PORT = 465;

    private final Object lock = new Object();
    private int msgCount = 0;
    private Socket socket;
    private long receivedBytes;

    public static void main(String[] args) {

        final Client2 obj = new Client2();
        try {
            obj.socket = new Socket(HOST, PORT);
            DataInputStream dis = new DataInputStream(obj.socket.getInputStream());

            byte[] msg = new byte[100];
            obj.start();
            //noinspection InfiniteLoopStatement
            while (true) {
                dis.readFully(msg);
                synchronized (obj.lock) {
                    obj.receivedBytes += 100;
                    obj.msgCount++;
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    @Override
    public void run() {
        long totalBytes;
        long localMsgCount;
        //noinspection InfiniteLoopStatement
        while (true) {
            synchronized (lock) {
                totalBytes = receivedBytes;
                localMsgCount = msgCount;
                msgCount = 0;
            }

            try {
                long elapsed = System.nanoTime() + 1_000_000_000;
                if (totalBytes > 0)
                    System.out.println("TotalBytes: " + totalBytes + " -- #_msg: " + localMsgCount);
                while (System.nanoTime() < elapsed) {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
