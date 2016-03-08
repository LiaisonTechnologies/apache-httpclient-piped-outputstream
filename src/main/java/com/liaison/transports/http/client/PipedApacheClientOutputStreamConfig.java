package com.liaison.transports.http.client;

import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.concurrent.ExecutorService;

/**
 * Created by Rob on 3/8/16.
 */
public class PipedApacheClientOutputStreamConfig {

    // calling code must provide a thread pool for initiating
    // the worker thread that calls Apache HTTP Client (ACL) execute()
    private ExecutorService es = null;

    // calling code provides the ACL instance to allow for provisioning
    // of http connection pool and other customizations
    private CloseableHttpClient httpClient = null;

    // url to post to
    private String url = null;

    // headers set on HTTP POST
    private Header[] headers = new Header[]{}; // default to empty

    // size of pipe buffer (bytes)
    private int pipeSize = 1024 * 1024; // default to 1 meg

    // whether to block the os.close() on sinked is.close()
    private boolean block = true; // default to blocking

    // milliseconds to wait for is.close()
    private long blockSleepTimeMillis = 500L;

    public ExecutorService getThreadPool() {
        return es;
    }

    public void setThreadPool(ExecutorService es) {
        this.es = es;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public int getPipeBufferSizeBytes() {
        return pipeSize;
    }

    public void setPipeBufferSizeBytes(int pipeSize) {
        this.pipeSize = pipeSize;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public long getBlockSleepTimeMillis() {
        return blockSleepTimeMillis;
    }

    public void setBlockSleepTimeMillis(long blockSleepTimeMillis) {
        this.blockSleepTimeMillis = blockSleepTimeMillis;
    }

}