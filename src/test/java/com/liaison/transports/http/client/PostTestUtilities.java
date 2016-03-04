package com.liaison.transports.http.client;

import org.apache.http.Header;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rob on 3/4/16.
 */
public class PostTestUtilities {

    public static void doPost(int payloadSizeFactor, String url, int pipeSize, boolean block) {

        ExecutorService es = Executors.newCachedThreadPool();

        doPost(es, payloadSizeFactor, url, pipeSize, block);

        // now block on closing thread
        es.shutdown();
    }

    public static void doPost(ExecutorService es, int payloadSizeFactor, String url, int pipeSize, boolean block) {

        // get output stream for an endpoint
        Header[] headers = null;

        HttpPipedOutputStream os = new HttpPipedOutputStream(url, headers, pipeSize, block);

        HttpPostExecutionRunner hper = new HttpPostExecutionRunner(os);

        // kick off apache post execution thread
        es.execute(hper);

        // write
        for (int x=0; x<payloadSizeFactor; x++) {
            byte randomBytes[] = UUID.randomUUID().toString().getBytes();

            try {
                os.write(randomBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            // apache executor thread is killed on close
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
