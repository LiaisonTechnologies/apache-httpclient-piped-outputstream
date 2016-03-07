package com.liaison.transports.http.client;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;

/**
 * Created by Rob on 3/4/16.
 */
public class HttpPipedOutputStream extends PipedOutputStream {

    private final HttpPost request;
    private final InputStreamEntity entity;
    private final PipedInputStream pis;
    private final boolean block;
    private final ExecutorService es;
    private final HttpPostExecutionRunner clientRunner;

    // set state that completion has executed...
    // should be post close()
    boolean postCompletion = false;
    boolean initialized = false;

    public HttpPipedOutputStream(ExecutorService es, String url, Header[] headers, int pipeSize, boolean block) {

        this.es = es;

        this.request = new HttpPost(url);
        this.request.setHeaders(headers);
        this.pis = new PipedInputStream(pipeSize);

        try {
            this.pis.connect(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.entity = new InputStreamEntity(this.pis, -1); // connect read stream
        this.request.setEntity(entity);
        this.block = block;

        this.clientRunner = new HttpPostExecutionRunner(this);
    }

    public HttpPost getPostObject() {
        return this.request;
    }

    public PipedInputStream getConnectedInputStream() {
        return pis;
    }

    public void setCompletedExecution() {
        postCompletion = true;
    }

    public boolean isCompletedExecution() {
        return postCompletion;
    }

    // kicks off apache client thread if not already started
    public void init() {

        if (!initialized) // first check (no lock)
        {
            synchronized (this) {

                if (!initialized) // second check (with lock)
                {
                    initialized = true;

                    // kick off thread
                    es.execute(clientRunner);
                }
            }
        }
    }

    @Override
    public void write(int b) throws IOException {

        init();  // before first write, make sure client execution thread is started

        super.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        init();  // before first write, make sure client execution thread is started

        super.write(b, off, len);
    }

    @Override
    public void close() throws IOException {

        // first close so that PipedInputStream
        // gets the message to stop reading
        super.close();

        // then block on piped inputstream closed if requested
        if (block) {
            while (!this.isCompletedExecution()) {
                try {
                    System.out.println("Waiting for Apache HTTP Execution to complete.");
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    // ok
                }
            }

        }

    }

}

// TODO make this a callable so that client can obtain error / http response objects
class HttpPostExecutionRunner implements Runnable {

    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPipedOutputStream pos;

    public HttpPostExecutionRunner(HttpPipedOutputStream pos) {
        this.pos = pos;
    }

    public CloseableHttpClient getHttpClient() {
        return this.httpclient;
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


