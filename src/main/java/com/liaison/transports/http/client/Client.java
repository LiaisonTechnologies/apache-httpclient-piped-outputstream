package com.liaison.transports.http.client;

/**
 * Created by Rob on 2/19/16.
 */
public class Client {

    public static void main(String args[]) throws Exception {

        HttpPipedOutputStream os = new HttpPipedOutputStream("http://localhost:3000");
        os.init();
        os.write("Hello world".getBytes());
        // how to get "is finished reading ??
        os.close(); // OVERRIDE the 1000 ms wait (not a big deal), ALWAYS close in finally, failure to close causes thread lock -- need to supply thread pool and
        // also monitor the pool for this

    }

}
