package com.kk.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadHelper {

    private static final Logger logger = LoggerFactory.getLogger(ThreadHelper.class);

    public static void sleep(String threadName, String label, Long time) {
        try {
            logger.debug(">> " + label + " > before sleep - " + threadName);
            long millis = time;
            Thread.sleep(millis);
            logger.debug("<< " + label + " < after sleep - " + threadName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
