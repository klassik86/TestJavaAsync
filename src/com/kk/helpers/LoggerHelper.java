package com.kk.helpers;

import java.util.Date;

public class LoggerHelper {

    public static void print(String value) {
        System.out.println(new Date() + ": [" + Thread.currentThread().getName() + "] " + value);
    }

}
