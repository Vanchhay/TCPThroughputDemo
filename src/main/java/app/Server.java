package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    private final MessageGenerator generator;

    private Server() {
        generator = new MessageGenerator();
        generator.start();
        final ClientListener listener = new ClientListener(generator);
        listener.start();
    }

    public static void main(String[] args) {
        Server obj = new Server();
        float maxEntryPerSec;
        Scanner input = new Scanner(System.in);
        System.out.print("Number of entry to generate every second: ");

        //noinspection InfiniteLoopStatement
        while (true) {
            maxEntryPerSec = input.nextFloat();
            obj.setTarget(maxEntryPerSec);
        }
    }

    private void setTarget(float target) {
        generator.setTarget(target);
    }

    class ClientWorker extends Thread implements Observer<String>{

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

    class ClientListener extends Thread {

        private final Logger LOGGER = LoggerFactory.getLogger(ClientListener.class);
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

    class MessageWriter extends Thread {

        private final OutputStream os;

        private final Queue<String> queue = new LinkedList<>();
        private volatile boolean stop;

        MessageWriter(OutputStream outputStream) {
            this.os = outputStream;
        }

        void enqueue(String updatedMsg) {
            synchronized (queue) {
                this.queue.add(updatedMsg);
            }
        }

        void stopThread() {
            stop = true;
        }

        @Override
        public void run() {
            long totalBytes = 0;
            long totalMsg = 0;
            long start = System.nanoTime();
            final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            ////noinspection InfiniteLoopStatement
            while (!stop) {
                try {
                    String respond;
                    synchronized (queue) {
                        respond = queue.poll();
                    }
                    if (respond != null) {
                        synchronized (os) {
//                            os.write(respond.getBytes());
                            for (byte b : respond.getBytes())
                                os.write(b);
                        }
                        totalBytes += respond.length();
                        totalMsg ++;
                    } else {
                        Thread.sleep(100);
                    }
                    if (System.nanoTime() > (start + 1_000_000_000)) {
                        System.out.println("\t" + dateFormat.format(new Date()) + " " + this.getName() +": MSG/Sec: " + totalMsg);
                        totalMsg = 0;
                        start = System.nanoTime();
                        System.out.println();
                    }
                } catch (InterruptedException ignored) { }
                catch (IOException e) {
                    break;
                }
            }
            System.out.println("Bytes sent: " + totalBytes + " " + " Total msg:" +totalMsg);
        }
    }

    class MessageGenerator extends Thread implements Publisher<String>{

        private static final long NANOSECOND = 1_000_000_000L;
        private static final int ONE_MILLISECOND = 1_000_000;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        private float targetMsg = 0;
        private final List<Observer<String>> observers = new ArrayList<>();

        void setTarget(float targetMsg) {
            this.targetMsg = targetMsg;
        }

        @Override
        public void run() {
            long nextGen = System.nanoTime() + NANOSECOND;
            float sentMsg = 0f;
            int counter = 0;

            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    while (System.nanoTime() <= nextGen) {
                        if(targetMsg <= 0){
                            Thread.sleep(100);
                            continue;
                        }

                        notifyObservers(dummyString());
                        sentMsg++;
                        long remainTime = nextGen - System.nanoTime();
                        if (sentMsg >= targetMsg && remainTime > 0) {
                            if (remainTime > ONE_MILLISECOND) {
                                Thread.sleep(remainTime / ONE_MILLISECOND);
                            }
                            break;
                        }
                    }
                    counter += sentMsg;
                    if(sentMsg > 0)
                        System.out.println((counter) + " --- " + sentMsg + "   ---\t" + dateFormat.format(new Date()) + "\t");
                    sentMsg = 0f;
                    nextGen = System.nanoTime() + NANOSECOND;
                } catch (Exception ignored) {
                }
            }
        }

        private void notifyObservers(String msg) {
            List<Observer<String>> localSubscribers = new ArrayList<>(observers);
            if (localSubscribers.size() > 0) {
                for (Observer<String> localSubscriber : localSubscribers) {
                    localSubscriber.update(msg);
                }
            }
        }

        private String dummyString() {
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 100;
            Random random = new Random();
            StringBuilder buffer = new StringBuilder(targetStringLength);
            for (int i = 0; i < targetStringLength -1; i++) {
                int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
                buffer.append((char) randomLimitedInt);
            }
            buffer.append("A");
            return buffer.toString();
        }

        @Override
        public void register(Observer<String> client) {
            if (client == null) throw new NullPointerException();
            synchronized (observers) {
                if (!observers.contains(client))
                    observers.add(client);
            }
        }

        @Override
        public void unregister(Observer<String> closedSocket) {
            if (closedSocket == null) throw new NullPointerException();
            synchronized (observers) {
                observers.remove(closedSocket);
            }
        }
    }

}

interface Publisher<T> {

    void register(Observer<T> subscriber);

    void unregister(Observer<T> subscriber);

}

interface Observer<T> {

    void update(T t);
}
