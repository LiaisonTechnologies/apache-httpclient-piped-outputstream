package com.liaison.transports.http.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Tests no blocking on os.close()
 * <p/>
 * Created by Rob on 3/2/16.
 */

public class HttpPostNonBlockingOutputStreamTest {

    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpPostBlockingOutputStreamTest.class);

    @Test
    public void testNonBlocking() throws Exception {

        // Client manages thread-pool
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("apache-client-executor-thread-%d").build();
        ExecutorService es = Executors.newCachedThreadPool(namedThreadFactory);

        // Client owns and provides http client builder
        CloseableHttpClient client = HttpClientBuilder.create().build();

        // Build Apache Client HTTP POST OutputStream
        PipedApacheClientOutputStreamConfig config = new PipedApacheClientOutputStreamConfig();
        config.setBlock(false);
        config.setUrl("http://localhost:3000"); // setup test server with ./run-server.sh from project root
        config.setPipeBufferSizsBytes(1024);
        config.setThreadPool(es);
        config.setHttpClient(client);

        PipedApacheClientOutputStream os = new PipedApacheClientOutputStream(config);

        // Write some dummy data
        for (int x = 0; x < 10; x++) {
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

}