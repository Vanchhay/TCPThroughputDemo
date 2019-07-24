package app;

import java.text.SimpleDateFormat;
import java.util.*;

public class MessageGenerator extends Thread implements Publisher<String>{

    private static final long NANOSECOND = 1_000_000_000L;
    private static final int ONE_MILLISECOND = 1_000_000;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private float targetMsg = 0;
    private static int observerCount;
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
        int countCheck = 0;
        if (localSubscribers.size() > 0) {
            for (Observer<String> localSubscriber : localSubscribers) {
                localSubscriber.update(msg);
                countCheck++;
            }
        }
        if (observerCount > 0){
            if(countCheck != observerCount) {
                System.out.println("NOT MATCH");
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
            observerCount = observers.size();
        }
        System.out.println("///////  observerCount :" + observerCount);
    }

    @Override
    public void unregister(Observer<String> closedSocket) {
        if (closedSocket == null) throw new NullPointerException();
        synchronized (observers) {
            observers.remove(closedSocket);
            observerCount = observers.size();
        }
        System.out.println("///////  observerCount Remain :" + observerCount);
    }
}
