package com.liaison.transports.http.client;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by Rob on 3/2/16.
 */
public class HttpPostExecutionRunner implements Runnable {

    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPipedOutputStream pos;

    public HttpPostExecutionRunner(HttpPipedOutputStream pos) throws IOException {
        this.pos = pos;
    }

    @Override
    public void run() {
        try {
            httpclient.execute(this.pos.getPostObject()); // blocks on is.read which blocks on os.close
            pos.getConnectedInputStream().close(); // close connected inputstream
            pos.setCompletedExecution();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}