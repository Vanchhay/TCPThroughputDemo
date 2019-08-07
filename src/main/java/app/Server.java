package app;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    private final static int PORT = 465;
    private OutputStream os;

    private Server(OutputStream os) {
        this.os = os;
    }

    public static void main(String[] args) throws IOException {
        final ServerSocket server = new ServerSocket(PORT);
        //noinspection InfiniteLoopStatement
        while (true) {
            Socket client = server.accept();
            System.out.println(client.toString());
            Thread worker = new Thread(new Server(client.getOutputStream()));
            worker.start();
        }
    }

    @Override
    public void run() {
        try {
            byte[] response = "dummy message".getBytes();
            //noinspection InfiniteLoopStatement
            while (true) {
                os.write(response);
            }
        } catch (IOException ignored) {
        }
    }
}