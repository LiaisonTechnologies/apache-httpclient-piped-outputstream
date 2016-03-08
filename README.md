<h1>Piped Apache Client OutputStream</h1>

<h2>About</h2>
<p>Common Java HTTP clients do not provide direct access to the HTTP POST OutputStream, instead providing an InputStream-only interface.  There are cases where a handle on the OutputStream is preferred, and this project serves that purpose.
</p>
<p>
The PipedApacheClientOutputStream inverts Apache Commons HTTP Client API to allow for calling-code-control over OutputStream for HTTP message POST.
</p>

<h2>Note</h2>

This project was started from a DropWizard template, is more likely to be used as a lib within a larger project.  To integrate with a composing project, manually port the http.client package and gradle build dependencies, or use the jar distributed to build/libs.  This could use some improvement.

<h2>Quick start</h2>

<h3>To exercise the OutputStream through test code</h3>

<pre>
./run-server.sh # starts node.js echo servlet that tests POST to
./gradlew test # runs all tests
</pre>

<h2>PipedApacheOutputStream Features</h2>

* Calling-code owns the executor service that spawns Apache client execution threads.
* Calling-code supplies the HttpClient instance so that calling code can manage things like HTTP connection pooling.
* Blocking on pipedOutputStream.close() - since wait on pipedInputStream.close() is optional.
 * Blocking is useful in cases where immediately downstream calling code expects a new state incurred from the POST.

<h2>Example Usage</h2>
Also see src/test for working examples.
<pre>
// Calling-code manages thread-pool
ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
    .setNameFormat("apache-client-executor-thread-%d").build();
ExecutorService es = Executors.newCachedThreadPool(namedThreadFactory);

// Client owns and provides http client builder
CloseableHttpClient client = HttpClientBuilder.create().build();

// Build configuration
PipedApacheClientOutputStreamConfig config = new PipedApacheClientOutputStreamConfig();
config.setUrl("http://localhost:3000");
config.setPipeBufferSizeBytes(1024);
config.setThreadPool(es);
config.setHttpClient(client);

// Instantiate OutputStream
PipedApacheClientOutputStream os = new PipedApacheClientOutputStream(config);

// Write to OutputStream
os.write(...);

try {
  os.close();
} catch (IOException e) {
  logger.error(e.getLocalizedMessage(), e);
}

// Do stuff with HTTP response
...

// Close the HTTP response
os.getResponse().close();

// Finally, shut down thread pool
// This must occur after retrieving response (after is) if interested in POST result
es.shutdown();
</pre>
<i>In practice the same client, executor service, and config will likely be reused throughout the life of the application, so the outer prep and close code in the above example will likely live in bootstrap/init and finalization code rather than directly inline with the OutputStream instantiation.</i>

<h2>Roadmap</h2>
Please use github issues for roadmap and bug tasks.