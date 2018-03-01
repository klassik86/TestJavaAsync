package com.kk.helpers;

import static com.kk.helpers.LoggerHelper.print;

public class ThreadHelper {

    public static void sleep(String threadName, String label, Long time) {
        try {
            print(">> " + label + " > before sleep - " + threadName);
            long millis = time;
            Thread.sleep(millis);
            print("<< " + label + " < after sleep - " + threadName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
