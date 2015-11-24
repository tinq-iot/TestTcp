package com.example.tom.testtcp;


import java.io.IOException;

public interface MyLogIntf {

    void d(String tag, String msg);

    void e(String tag, String s, Exception e);

    void d(String tag, String s, IOException e);
}
