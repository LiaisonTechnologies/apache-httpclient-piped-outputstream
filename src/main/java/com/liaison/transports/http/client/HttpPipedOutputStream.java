package com.liaison.transports.http.client;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.concurrent.Executors;

import java.io.IOException;
import java.io.OutputStream;


class HttpPipedOutputStream extends OutputStream {

    // members
    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final String url;
    private final HttpPost request;
    private final PipedInputStreamEntity ise;
    private final PipedOutputStream os;

    /**
     * Prepares an HTTP post request, with handle on OutputStream
     * via piping.
     *
     * @param url
     * @throws Exception
     */
    public HttpPipedOutputStream(String url) throws Exception {

        // Prep url and post method
        this.url = url;
        this.request = new HttpPost(url);

        // Prep InputStream, http client wil read from this
        PipedInputStream is = new PipedInputStream();
        PipedInputStreamEntity ise = new PipedInputStreamEntity(is);
        this.ise = ise;

        // Connect PipedInputStream to a PipedOutputStream
        PipedOutputStream os = new PipedOutputStream(is);
        this.os = os;

        // Set the InputStream entity on the request
        request.setEntity(ise);

    }

    public void init() {


            // execute in separate thread
            try {

                System.out.println("Initiating client...");

                Thread t = Executors.defaultThreadFactory().newThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // WARN:  should be put into future with callback and
                            // edge case handling
                            // starts execution in a separate thread,
                            // will block reading inputstream from entity
                            // until os.close() called, then complete
                            httpclient.execute(request);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                //t.start();

                t.setName("HTTP_CLIENT_THREAD");
                Executors.newSingleThreadExecutor().execute(t);


            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {


        if (offset < 0 || length < 0 || offset + length > buffer.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (os != null) {
            os.write(buffer, offset, length);
        }
    }

    private boolean writeInitiated = false;

    @Override
    public void write(int b) throws IOException {

      os.write(b);
    }

    @Override
    public void close() {
        try {
            os.flush();
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



}



