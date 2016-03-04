package com.liaison.transports.http.client;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by Rob on 3/4/16.
 */
public class HttpPipedOutputStream extends PipedOutputStream {

    private final HttpPost request;
    private final InputStreamEntity entity;
    private final PipedInputStream pis;

    public HttpPipedOutputStream(String url, Header[] headers, int pipeSize) throws Exception {
        this.request = new HttpPost(url);
        this.request.setHeaders(headers);
        this.pis = new PipedInputStream(pipeSize);
        this.pis.connect(this);
        this.entity = new InputStreamEntity(this.pis, -1); // connect read stream
        this.request.setEntity(entity);
    }

    public HttpPost getPostObject() {
        return this.request;
    }

    public PipedInputStream getConnectedInputStream() {
        return pis;
    }

    // set state that completion has executed...
    // should be post close()
    boolean postCompletion = false;
    public void setCompletedExecution() {
        postCompletion = true;
    }
    public boolean isCompletedExecution() {
        return postCompletion;
    }

    @Override
    public void close() throws IOException {

        // first close so that PipedInputStream
        // gets the message to stop reading
        super.close();

        // then block on piped inputstream closed
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


