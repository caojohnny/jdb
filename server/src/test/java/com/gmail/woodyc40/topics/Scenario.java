package com.gmail.woodyc40.topics;

public class Scenario {
    private static int d = 33;

    public static void main(String[] args) throws InterruptedException {
        int a = 3;
        int b = 4;
        int c = a + b;
        Object nil = null;

        // Pause
        Object o = new Object();
        Integer.valueOf(o.hashCode()).hashCode();

        Thread.sleep(7000);

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