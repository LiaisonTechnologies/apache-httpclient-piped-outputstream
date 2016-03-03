package com.liaison.transports.http.client;

import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.lifecycle.setup.ExecutorServiceBuilder;

import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Rob on 3/2/16.
 */
public class ACLPipedOutputStream extends PipedOutputStream {


    public ACLPipedOutputStream(String url) throws Exception {
        ApacheRequestExecutor requestExecutor = new ApacheRequestExecutor(this, url);

        // TODO properly vet pooling strategy
        // TODO should be registered with ExecutorServiceManager
        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(requestExecutor); // TODO move this into first byte written for optimization
        es.shutdown();
    }



}
