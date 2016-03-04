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
public class ApachePostRequestExecutor implements Runnable {

    private final InputStream is;
    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPost request;

    public static OutputStream getOutputStream(ExecutorService es, String url, Header[] headers) throws Exception {
        PipedOutputStream pos = new PipedOutputStream();
        ApachePostRequestExecutor are = new ApachePostRequestExecutor(es, pos, url, headers); // TODO vet cache scheme
        return pos;
    }

    // TODO MOVE THIS INTO TESTS

    public ApachePostRequestExecutor(ExecutorService es, PipedOutputStream pos, String url, Header[] headers) throws IOException {

        this.is = new PipedInputStream(pos);

        // we create the request internally since we have to maintain control of
        /// the InputStreamEntity
        this.request = new HttpPost(url);

        this.request.setHeaders(headers);

        InputStreamEntity e = new InputStreamEntity(is, -1);
        this.request.setEntity(e);

        es.execute(this); // TODO move this into first byte written for optimization

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