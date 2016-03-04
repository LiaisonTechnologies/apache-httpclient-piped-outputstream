package com.liaison.transports.http.client;

import org.apache.http.Header;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rob on 3/2/16.
 */
public class NonBlockingTest {

    public static void main(String[] args) throws Exception {

        ExecutorService es = Executors.newCachedThreadPool();

        // get output stream for an endpoint

        OutputStream os = ApachePostRequestExecutor.getOutputStream(es, "http://localhost:3000", null);

        // write
        os.write("hello world 2".getBytes());

        // apache executor thread is killed on close (must enforce close in finalize)
        os.close();

        es.shutdown();

    }

  /*  1. Are we heading into a bad zone here with too many threads being spawned and managed? - Need executor service manager

      2. We need to simulate the case where the WriteToSocket() is slow — >> the PipeInputStream.read() will be slow — > begs the question:  does it stall the writer ? Or does the buffer of the pipe blow up?

      3. We should test performance of Pipes under load + under concurrency + under slow PipeInputStream read to make sure we don’t have overflow pipes or broken pipes etc..
   */

}