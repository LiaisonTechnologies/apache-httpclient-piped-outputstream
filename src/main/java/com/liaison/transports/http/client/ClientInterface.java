package com.liaison.transports.http.client;

import java.io.OutputStream;
import java.io.PipedOutputStream;

/**
 * Created by Rob on 3/2/16.
 */
public class ClientInterface {

    public static OutputStream getOutputStream(String url) throws Exception {
        PipedOutputStream os = new PipedOutputStream();
        ApacheRequestExecutor requestExecutor = new ApacheRequestExecutor(os, url);
        new Thread(requestExecutor).start(); // TODO move into write for performance (minimizes thread alive time)
        return os;
    }

}