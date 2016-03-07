package com.liaison.transports.http.client;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Rob on 3/4/16.
 */
public class HttpPipedOutputStream extends PipedOutputStream {

    private final HttpPost request;
    private final InputStreamEntity entity;
    private final PipedInputStream pis;
    private final boolean block;
    private final ExecutorService es;
    private final HttpPOSTExecutionRunner clientRunner;
    private List<Future<POSTResult>> result = null;
    private POSTResult postResult = null;
    private final long blockSleepTimeMillis;

    boolean initialized = false;

    public HttpPipedOutputStream(ExecutorService es, String url, Header[] headers, int pipeSize, boolean block, long blockSleepTimeMillis) {

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

        this.blockSleepTimeMillis = blockSleepTimeMillis;

        this.clientRunner = new HttpPOSTExecutionRunner(this);
    }

    /**
     * Useful if client code wants to further manipulate the request object
     * prior to Apache Client POST execution.
     *
     * @return HttpPost object used by Apache Client
     */
    public HttpPost getPostObject() {
        return this.request;
    }

    /**
     * @return InputStream sinked to this outputstream
     */
    public PipedInputStream getConnectedInputStream() {
        return pis;
    }

    /**
     * Returns true if callable has completed and we have the post result.
     * Post result is saved with "this" so that client code can handle HTTP response downstream.
     * @return
     */
    public boolean completedPOSTExecution() {
        // if we have the post result, we've completed execution
        if (null == this.postResult) {
            return true;
        }

        // try to obtain post result
        if (null != result && null != result.get(1)) {
            try {
                this.postResult = result.get(1).get();
                return true;
            } catch (ExecutionException e) {
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    // kicks off apache client thread if not already started...
    //
    private void init() {

        if (!initialized) { // first check (no lock)

            synchronized (this) {

                if (!initialized) { // second check (with lock)
                    initialized = true;

                    // kick off thread
                    List<HttpPOSTExecutionRunner> runners = new ArrayList<HttpPOSTExecutionRunner>();
                    runners.add(clientRunner);
                    try {
                         result = es.invokeAll(runners);
                    } catch (InterruptedException e) {
                        e.printStackTrace(); // TODO log
                    }
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
            while (!this.completedPOSTExecution()) {
                try {
                    System.out.println("Waiting for Apache HTTP Execution to complete.");
                    Thread.sleep(blockSleepTimeMillis);
                } catch (InterruptedException e) {
                    // ok
                }

            }

        }

    }

    /**
     * Must be called after completedPOSTExecution()
     * @return
     * @throws Exception
     */
    public CloseableHttpResponse getResponse() throws Exception {
        if (null == this.postResult) throw new RuntimeException("Execution thread has not completed.");
        if (null != this.postResult.e) throw this.postResult.e;
        return this.postResult.response;
    }

}

class HttpPOSTExecutionRunner implements Callable<POSTResult> {

    // TODO consider support for user-supplied client
    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPipedOutputStream pos;

    public HttpPOSTExecutionRunner(HttpPipedOutputStream pos) {
        this.pos = pos;
    }

    public CloseableHttpClient getHttpClient() {
        return this.httpclient;
    }

    @Override
    public POSTResult call() {
        try {
            CloseableHttpResponse response = httpclient.execute(this.pos.getPostObject()); // blocks on is.read which blocks on os.close
            pos.getConnectedInputStream().close(); // close connected inputstream
            return new POSTResult(response);
        } catch (IOException e) {
            return new POSTResult(e);
        }
    }
}

class POSTResult {

    Exception e;
    CloseableHttpResponse response;

    public POSTResult(Exception e) {
        this.e = e;
    }

    public POSTResult(CloseableHttpResponse response) {
        this.response = response;
    }

}


