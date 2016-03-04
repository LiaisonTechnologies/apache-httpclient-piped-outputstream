package com.liaison.transports.http.client;

import com.codahale.metrics.annotation.Timed;
import org.apache.http.Header;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rob on 3/2/16.
 */
public class NonBlockingTest {


    public static void doPost(int payloadSize, String url) throws Exception {
        ExecutorService es = Executors.newCachedThreadPool();

        // get output stream for an endpoint
        Header[] headers = null;

        PipedOutputStream os = new PipedOutputStream();
        HttpPostExecutionCoordinator hpec = new HttpPostExecutionCoordinator(es, os, url, headers);

        hpec.init();


        // write
        for (int x=0; x<payloadSize; x++) {
            os.write(UUID.randomUUID().toString().getBytes());
        }

        // apache executor thread is killed on close (must enforce close in finalize)
        os.close();

        es.shutdown();
    }

    @Timed
    public static void main(String[] args) throws Exception {

        doPost(1000*1000, "http://localhost:3000");

    }

  /*  1. Are we heading into a bad zone here with too many threads being spawned and managed? - Need executor service manager

      2. We need to simulate the case where the WriteToSocket() is slow — >> the PipeInputStream.read() will be slow — > begs the question:  does it stall the writer ? Or does the buffer of the pipe blow up?

      3. We should test performance of Pipes under load + under concurrency + under slow PipeInputStream read to make sure we don’t have overflow pipes or broken pipes etc..
   */

}