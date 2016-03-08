package com.liaison.transports.http.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Rob on 3/4/16.
 */
public class PipedApacheClientOutputStream extends PipedOutputStream {

    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PipedApacheClientOutputStream.class);

    private final PipedApacheClientOutputStreamConfig config;

    private final HttpPost request;
    private final InputStreamEntity entity;
    private final PipedInputStream pis;

    private final HttpPOSTExecutionRunner clientRunner;

    private Future<CloseableHttpResponse> result = null;
    private boolean initialized = false;

    /**
     * Constructor
     *
     * @param config
     */
    public PipedApacheClientOutputStream(PipedApacheClientOutputStreamConfig config) {

        this.config = config;

        this.request = new HttpPost(config.getUrl());

        this.request.setHeaders(config.getHeaders());
        this.pis = new PipedInputStream(config.getPipeBufferSizeBytes());

        try {
            this.pis.connect(this);
        } catch (IOException e) {
            // FATAL, unexpected
            throw new RuntimeException(e);
        }

        this.entity = new InputStreamEntity(this.pis, -1); // connect read stream
        this.request.setEntity(entity);

        this.clientRunner = new HttpPOSTExecutionRunner(this, config.getHttpClient());

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
     *
     * @return
     */
    public boolean completedPOSTExecution() {
        return this.result.isDone();
    }

    /**
     * Kicks off ACL execute() worked if not already started for this instance
     */
    private void assertReadyToWrite() {

        // WARN:  Not thread-safe.
        // Assumes only one thread owns this instance (currently safe for known use cases)

        if (!initialized) {
            initialized = true;
            logger.debug("Initializing OutputStream... ");


            // start ACL execution thread, and don't wait for response...
            // this thread will spin on pipedInputStream.read() until the piped os is closed
            result = config.getThreadPool().submit(clientRunner);
            logger.debug("Apache HTTP Client (execute POST) thread invoked.");

        }

    }

    @Override
    public void write(int b) throws IOException {
        // before first write, make sure client execution thread is started
        assertReadyToWrite();
        super.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        // before first write, make sure client execution thread is started
        assertReadyToWrite();
        super.write(b, off, len);
    }

    @Override
    public void close() throws IOException {

        // first close so that PipedInputStream
        // gets the message to stop reading
        super.close();

        // then block on piped inputstream closed if requested
        if (config.isBlock()) {
            while (!this.completedPOSTExecution()) {
                try {
                    logger.debug("Waiting for Apache HTTP Execution to complete.");
                    Thread.sleep(config.getBlockSleepTimeMillis());
                } catch (InterruptedException e) {
                    // ok
                    logger.debug(e.getLocalizedMessage(), e);
                }
            }
        }

    }

    /**
     * Must be called only -after- completedPOSTExecution()
     *
     * @return
     * @throws Exception
     */
    public CloseableHttpResponse getResponse() throws ExecutionException {
        try {
            return this.result.get();
        } catch (InterruptedException e) {
            // FATAL, unexpected.
            throw new RuntimeException(e);
        }
    }

}

class HttpPOSTExecutionRunner implements Callable<CloseableHttpResponse> {

    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpPOSTExecutionRunner.class);

    private final CloseableHttpClient httpclient;
    private final PipedApacheClientOutputStream pos;

    public HttpPOSTExecutionRunner(PipedApacheClientOutputStream pos, CloseableHttpClient httpClient) {
        this.pos = pos;
        this.httpclient = httpClient;
    }

    @Override
    public CloseableHttpResponse call() throws IOException {

        try {
            CloseableHttpResponse response = httpclient.execute(this.pos.getPostObject()); // blocks on is.read which blocks on os.close
            logger.debug("Completed HTTP POST:  " + response.getStatusLine());
            return response;
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            try {
                pos.getConnectedInputStream().close(); // close connected inputstream
            } catch (IOException failedToCloseInputStream) {
                logger.error(failedToCloseInputStream.getLocalizedMessage(), e);
            }
            try {
                pos.close(); // close connected inputstream
            } catch (IOException failedToCloseOutputStream) {
                logger.error(failedToCloseOutputStream.getLocalizedMessage(), e);
            }
            throw e;
        }

    }

}