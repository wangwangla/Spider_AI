package com.siondream.superjumper.desktop;

import java.util.Random;

public class App {
    public static void main(String[] args) {
        Random random = new Random();
        random.setSeed(1);
        for (int i = 10; i > 0; i--) {
            System.out.println(random.nextInt(i));
        }
    }
}
