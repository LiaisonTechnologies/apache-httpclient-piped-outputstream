package com.liaison.transports.http.client;

import java.io.OutputStream;

/**
 * Created by Rob on 3/2/16.
 */
public class NonBlockingTest {


    public static void main(String[] args) throws Exception {

        // get output stream for an endpoint
        OutputStream os = ClientInterface.getOutputStream("http://localhost:3000");

        // write
        os.write("fdfd".getBytes());

        // apache executor thread is killed on close (must enforce close in finalize)
        os.close();

    }

}
