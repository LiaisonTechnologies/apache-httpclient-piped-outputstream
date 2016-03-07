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

    public static void doPost(int payloadSizeFactor, String url, int pipeSize, boolean block, long blockWaitTimeMillis) {

        // client manages thread-pool
        ExecutorService es = Executors.newCachedThreadPool();

        // set HTTP POST headers
        Header[] headers = null;


        // build outputstream
        HttpPipedOutputStream os = new HttpPipedOutputStream(es, url, headers, pipeSize, block, blockWaitTimeMillis);

        // ... here is where FS2 returns the outputstream

        // write dummy data
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

        es.shutdown();

    }


}
