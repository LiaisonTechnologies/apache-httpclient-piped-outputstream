package com.liaison.transports.http.client;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by Rob on 3/2/16.
 */
public class ApacheRequestExecutor implements Runnable {

    private final InputStream is;
    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPost request;

    public ApacheRequestExecutor(PipedOutputStream os, String url) throws IOException {
        this.is = new PipedInputStream(os);
        this.request = new HttpPost(url);
        InputStreamEntity e = new InputStreamEntity(is, -1);
        this.request.setEntity(e);
    }

    @Override
    public void run() {
        try {
            httpclient.execute(request); // blocks on is.read which blocks on os.close
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}