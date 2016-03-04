package com.liaison.transports.http.client;

import com.codahale.metrics.annotation.Timed;
import org.apache.http.Header;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rob on 3/2/16.
 */
public class NonBlockingTest {

    public static void doPost(int payloadSizeFactor, String url, int pipeSize, boolean block) throws Exception {

        ExecutorService es = Executors.newCachedThreadPool();

        // get output stream for an endpoint
        Header[] headers = null;

        HttpPipedOutputStream os = new HttpPipedOutputStream(url, headers, pipeSize, block);

        HttpPostExecutionRunner hper = new HttpPostExecutionRunner(os);

        // kick off apache post execution thread
        es.execute(hper);

        // write
        for (int x=0; x<payloadSizeFactor; x++) {
            os.write(UUID.randomUUID().toString().getBytes());
        }

        // apache executor thread is killed on close
        os.close();

        // now block on closing thread
        es.shutdown();

    }

    @Timed
    public static void main(String[] args) throws Exception {
        int pipeSizeBytes = 64 * 64;
        int payloadSizeFactor = pipeSizeBytes * 16;
        boolean block  = true;
        String url = "http://localhost:3000";

        doPost(payloadSizeFactor, url, pipeSizeBytes, block);
        doPost(payloadSizeFactor, url, pipeSizeBytes*10, block);
        doPost(payloadSizeFactor*24, url, pipeSizeBytes, true);
        //doPost(payloadSizeFactor, url, pipeSizeBytes, block);
        //doPost(payloadSizeFactor, url, pipeSizeBytes, block);
        //doPost(payloadSizeFactor, url, pipeSizeBytes/10, false);
        //doPost(payloadSizeFactor, url, pipeSizeBytes, false);
        //doPost(payloadSizeFactor*24, url, pipeSizeBytes, block);

    }

  /*  1. Are we heading into a bad zone here with too many threads being spawned and managed? - Need executor service manager

      2. We need to simulate the case where the WriteToSocket() is slow — >> the PipeInputStream.read() will be slow — > begs the question:  does it stall the writer ? Or does the buffer of the pipe blow up?

      3. We should test performance of Pipes under load + under concurrency + under slow PipeInputStream read to make sure we don’t have overflow pipes or broken pipes etc..
   */

}