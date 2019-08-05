package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private final static int PORT = 465;
    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private void start() {
        final MessageGenerator generator = new MessageGenerator(5000);
        generator.start();
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                final ServerSocket server = new ServerSocket(PORT);
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        Socket client = server.accept();
                        LOGGER.info(client.toString());
                        ClientWorker worker = new ClientWorker(client.getOutputStream());
                        generator.addObserver(worker);
                        worker.start();
                    } catch (IOException e) {
                        LOGGER.warn(e.toString());
                    }
                }
            } catch (IOException e1) {
                LOGGER.warn(e1.toString());
            }
        }
    }

    class ClientWorker extends Thread {

        private final OutputStream os;
        private final Queue<String> queue = new LinkedList<>();

        ClientWorker(OutputStream outputStream) {
            this.os = outputStream;
        }

        void update(String updatedMsg) {
            synchronized (this) {
                queue.add(updatedMsg);
            }
        }

        @Override
        public void run() {
            try {
                String respond;
                //noinspection InfiniteLoopStatement
                while (true) {
                    synchronized (this) {
                        respond = queue.poll();
                    }
                    if (respond != null) {
                        synchronized (os) {
                            //os.write(respond.getBytes());
                            for (byte b : respond.getBytes())
                                os.write(b);
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    class MessageGenerator extends Thread {

        private static final long ONE_NANOSECOND_AS_LONG = 1_000_000_000L;
        private static final long ONE_MILLISECOND_As_LONG = 1_000_000;
        private final List<ClientWorker> observers = new ArrayList<>();
        private float target;

        MessageGenerator(float msgPerSec) {
            this.target = msgPerSec;
        }

        void addObserver(ClientWorker observer) {
            observers.add(observer);
        }

        @Override
        public void run() {
            long nextGen = System.nanoTime() + ONE_NANOSECOND_AS_LONG;
            float sentMsg = 0f;

            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    while (System.nanoTime() <= nextGen) {
                        String msg = dummyString();
                        sentMsg += notifyObservers(msg);

                        long remainTime = nextGen - System.nanoTime();
                        if (sentMsg >= target && remainTime > 0) {
                            if (remainTime > ONE_MILLISECOND_As_LONG) {
                                Thread.sleep(remainTime / ONE_MILLISECOND_As_LONG);
                            }
                            break;
                        }
                    }
                    sentMsg = 0f;
                    nextGen = System.nanoTime() + ONE_NANOSECOND_AS_LONG;
                } catch (Exception ignored) {
                }
            }
        }

        private float notifyObservers(String msg) {
            List<ClientWorker> localSubscribers = new ArrayList<>(observers);
            if (localSubscribers.size() > 0) {
                for (ClientWorker localSubscriber : localSubscribers) {
                    localSubscriber.update(msg);
                }
            }
            return 1f;
        }

        private String dummyString() {
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 100;
            Random random = new Random();
            StringBuilder buffer = new StringBuilder(targetStringLength);
            for (int i = 0; i < targetStringLength - 1; i++) {
                int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
                buffer.append((char) randomLimitedInt);
            }
            buffer.append("A");
            return buffer.toString();
        }
    }
}