package app;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

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
                        os.write(respond.getBytes());
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
