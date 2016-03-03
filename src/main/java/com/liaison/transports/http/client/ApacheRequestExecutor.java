package com.liaison.transports.http.client;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rob on 3/2/16.
 */
public class ApacheRequestExecutor implements Runnable {

    private final InputStream is;
    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPost request;


    public static OutputStream getOutputStreamForHttpPost(String url, Header[] headers) throws Exception {
        PipedOutputStream pos = new PipedOutputStream();

        ApacheRequestExecutor are = new ApacheRequestExecutor(pos, url, headers);

        return pos;
    }

    public ApacheRequestExecutor(PipedOutputStream pos, String url, Header[] headers) throws IOException {

        this.is = new PipedInputStream(pos);

        // we create the request internally since we have to maintain control of
        /// the InputStreamEntity
        this.request = new HttpPost(url);

        this.request.setHeaders(headers);

        InputStreamEntity e = new InputStreamEntity(is, -1);
        this.request.setEntity(e);

        // TODO should be registered with ExecutorServiceManager
        ExecutorService es = Executors.newCachedThreadPool();

        // TODO properly vet pooling strategy
        es.execute(this); // TODO move this into first byte written for optimization
        es.shutdown();


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