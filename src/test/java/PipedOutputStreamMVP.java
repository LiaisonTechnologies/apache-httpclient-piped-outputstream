
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;

import java.io.IOException;

public class PipedOutputStreamMVP {

    public static void main(String[] args) throws Exception {

        // get output stream for an endpoint
        OutputStream os = ClientInterface.getOutputStream("http://localhost:3000");

        // write
        os.write("fdfd".getBytes());

        // apache executor thread is killed on close (must enforce close in finalize)
        os.close();
    }

}

class ClientInterface {

    public static OutputStream getOutputStream(String url) throws Exception {
        PipedOutputStream os = new PipedOutputStream();
        ApacheHttpRequestExecutor requestExecutor = new ApacheHttpRequestExecutor(os, url);
        new Thread(requestExecutor).start(); // TODO move into write for performance (minimizes thread alive time)
        return os;
    }

}

class ApacheHttpRequestExecutor implements Runnable {

    private final InputStream is;
    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    private final HttpPost request;

    public ApacheHttpRequestExecutor(PipedOutputStream os, String url) throws IOException {
        this.is = new PipedInputStream(os);
        this.request = new HttpPost(url);
        InputStreamEntity e = new InputStreamEntity(is, -1);
        this.request.setEntity(e);
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