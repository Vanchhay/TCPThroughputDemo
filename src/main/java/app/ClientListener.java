package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener extends Thread{

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientListener.class);
    private final static int PORT = 465;
    private final MessageGenerator generator;

    ClientListener(MessageGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run() {
        try {
            final ServerSocket server = new ServerSocket(PORT);
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    Socket client = server.accept();
                    ClientWorker worker = new ClientWorker(generator, client);
                    worker.start();
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        } catch (IOException e1) {
            LOGGER.warn(e1.getMessage());
        }
    }
}
