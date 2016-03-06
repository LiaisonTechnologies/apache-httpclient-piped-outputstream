package com.liaison.transports.http.client;

import com.codahale.metrics.annotation.Timed;

/**
 * Created by Rob on 3/2/16.
 */
public class NonBlockingTest {

    // TODO We need to simulate the case where the WriteToSocket() is slow — >> the PipeInputStream.read() will be slow — >
    // TODO begs the question:  does it stall the writer ? Or does the buffer of the pipe blow up?

    // TODO Test performance of Pipes under load + under concurrency + under slow PipeInputStream read to make sure we don’t
    // TODO have overflow pipes or broken pipes etc..

    // TODO make real test

    @Timed
    public static void main(String[] args) throws Exception {
        int pipeSizeBytes = 64 * 64;
        int payloadSizeFactor = pipeSizeBytes * 16;
        String url = "http://localhost:3000";
        PostTestUtilities.doPost(payloadSizeFactor, url, pipeSizeBytes, true);
    }

}