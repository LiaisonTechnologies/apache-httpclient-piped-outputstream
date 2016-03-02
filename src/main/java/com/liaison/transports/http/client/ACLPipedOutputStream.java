package com.liaison.transports.http.client;

import java.io.PipedOutputStream;

/**
 * Created by Rob on 3/2/16.
 */
public class ACLPipedOutputStream extends PipedOutputStream {


    public ACLPipedOutputStream(String url) throws Exception {
        ApacheRequestExecutor requestExecutor = new ApacheRequestExecutor(this, url);
        new Thread(requestExecutor).start(); // TODO move into write for performance (minimizes thread alive time)
    }


}
