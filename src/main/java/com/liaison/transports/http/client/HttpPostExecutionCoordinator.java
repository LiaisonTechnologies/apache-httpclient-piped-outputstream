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
public class HttpPostExecutionCoordinator implements Runnable {

    private final InputStream is;
    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPost request;
    private final ExecutorService es;

    public HttpPostExecutionCoordinator(ExecutorService es, PipedOutputStream pos, String url, Header[] headers) throws IOException {

        this.is = new PipedInputStream(pos);

        // we create the request internally since we have to maintain control of
        /// the InputStreamEntity
        this.request = new HttpPost(url);

        this.request.setHeaders(headers);

        InputStreamEntity e = new InputStreamEntity(is, -1);
        this.request.setEntity(e);

        this.es = es;

    }

    public void init() {
        this.es.execute(this); // TODO move this into first byte written for optimization
    }

    public HttpPost getPostObject() {
        return this.request;
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