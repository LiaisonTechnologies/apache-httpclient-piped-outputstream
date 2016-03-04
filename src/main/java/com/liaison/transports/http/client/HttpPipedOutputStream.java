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
    private final boolean block;
    // set state that completion has executed...
    // should be post close()
    boolean postCompletion = false;

    public HttpPipedOutputStream(String url, Header[] headers, int pipeSize, boolean block){
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


