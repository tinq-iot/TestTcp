package com.example.tom.testtcp;


import java.io.IOException;

public class MyLog {

    private static MyLogIntf logger = null;

    public static void setLogger(MyLogIntf log) {
        logger = log;
    }

    public static void d(String tag, String s) {
        if (logger != null) {
            logger.d(tag, s);
        }
    }

    public static void e(String tag, String s, Exception e) {
        if (logger != null) {
            logger.e(tag, s, e);
        }
    }

    public static void e(String tag, String s) {
        if (logger != null) {
            logger.e(tag, s, null);
        }
    }

    public static void d(String tag, String s, IOException e) {
        if (logger != null) {
            logger.d(tag, s,e);
        }

    }
}