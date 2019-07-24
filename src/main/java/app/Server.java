package app;

import java.util.Scanner;

public class Server {

    public static void main(String[] args) {

        MessageGenerator generator = new MessageGenerator();
        generator.start();
        ClientListener listener = new ClientListener(generator);
        listener.start();


        float maxEntryPerSec;
        Scanner input = new Scanner(System.in);
        System.out.print("Number of entry to generate every second: ");

        //noinspection InfiniteLoopStatement
        while (true) {
            maxEntryPerSec = input.nextFloat();
            generator.setTarget(maxEntryPerSec);
        }
    }


}
