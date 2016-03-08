package com.liaison.transports.http.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Rob on 3/2/16.
 */

public class HttpPostBlockOnOutputStreamCloseTest {

    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpPostBlockOnOutputStreamCloseTest.class);

    // TODO simulate the case where the WriteToSocket() is slow — >> the PipeInputStream.read() will be slow — >
    // TODO begs the question:  does it stall the writer ? Or does the buffer of the pipe blow up?

    // TODO Test performance of Pipes under load + under concurrency + under slow PipeInputStream read to make sure we don’t
    // TODO have overflow pipes or broken pipes etc..

    // TODO create client and executor service in pre step... and close executor service in post step

    @Test
    public void testBlocking() throws Exception {

        // Client manages thread-pool
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("apache-client-executor-thread-%d").build();
        ExecutorService es = Executors.newCachedThreadPool(namedThreadFactory);

        // Client owns and provides http client builder
        CloseableHttpClient client = HttpClientBuilder.create().build();

        // Build Apache Client HTTP POST OutputStream
        PipedApacheClientOutputStreamConfig config = new PipedApacheClientOutputStreamConfig();
        config.setBlock(true);
        config.setUrl("http://localhost:3000"); // setup test server with ./run-server.sh from project root
        config.setHeaders(new Header[]{});
        config.setPipeBufferSizsBytes(1024);
        config.setBlockSleepTimeMillis(500L);
        config.setThreadPool(es);

        // TODO - Noticed a bug where, if client not set here, will get
        // TODO java.lang.NullPointerException
        // TODO java.util.concurrent.ExecutionException: java.lang.NullPointerException
        // TODO at java.util.concurrent.FutureTask.report(FutureTask.java:122)
        // TODO at java.util.concurrent.FutureTask.get(FutureTask.java:188)
        // TODO at com.liaison.transports.http.client.PipedApacheClientOutputStream.getResponse(PipedApacheClientOutputStream.java:154)
        // TODO config.setHttpClient(HttpClientBuilder.create().build());
        // TODO Should fail on null client prior to calling execute(), but more concerning is that
        // TODO a NPE isn't thrown when execute() is called on null client.
        config.setHttpClient(client);

        PipedApacheClientOutputStream os = new PipedApacheClientOutputStream(config);

        // Write some dummy data
        for (int x=0; x<3; x++) {
            byte randomBytes[] = UUID.randomUUID().toString().getBytes();

            logger.debug("Writing: " + new String(randomBytes));

            try {
                os.write(randomBytes);
                logger.debug("Wrote: " + new String(randomBytes));
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                // TODO fail test
                break;
            }
        }

        try {
            // apache executor thread is killed on close
            logger.debug("Closing OutputStream");
            os.close();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        logger.debug(os.getResponse().getStatusLine().toString());

        // TODO this should be called from os.close(), and
        // TODO response should be obtained/used prior to close

        os.getResponse().close(); // close http response

        // finally, shut down thread pool (must occur after retrieving response)
        es.shutdown();

    }

    /**
     * Pointed to by application YML as "run" target since src/main code intended
     * to be driven from a larger scope (used as lib or copy/paste)
     *
     * @param args
     * @throws Exception
     */
    public static void main (String[] args) throws Exception {
        new HttpPostBlockOnOutputStreamCloseTest().testBlocking();
    }

}