package app;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientWorker extends Thread implements Observer<String>{

    private final MessageGenerator generator;
    private final Socket client;
    private MessageWriter messageWriter;

    ClientWorker(MessageGenerator generator, Socket client) {
        this.generator = generator;
        this.client = client;
    }

    @Override
    public void run() {
        generator.register(this);
        try {
            messageWriter = new MessageWriter(client.getOutputStream());
            messageWriter.start();

            //noinspection InfiniteLoopStatement
            while (true){
                // below block of code: purpose is just to unregister socket when we close its program
                DataInputStream dis = new DataInputStream(client.getInputStream());
                byte[] input = new byte[5];
                dis.readFully(input);
            }
        } catch (IOException ignored) {
        }finally {
            generator.unregister(this);
            messageWriter.stopThread();
        }
    }

    @Override
    public void update(String s) {
        messageWriter.enqueue(s);
    }
}
