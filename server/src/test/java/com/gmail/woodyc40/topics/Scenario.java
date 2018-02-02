package com.gmail.woodyc40.topics;

import java.util.Scanner;

public class Scenario {
    private static int d = 33;

    public static void main(String[] args) {
        int a = 3;
        int b = 4;
        int c = a + b;
        Object nil = null;

        // Pause
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        System.out.println("ok");
        System.out.println("let's try again");

        changeStack();
        pop2();

        nil.hashCode();
    }

    private static void changeStack() {
        int a = 10;
        System.out.println(a);
    }

    private static double pop2() {
        return 0.0;
    }
}