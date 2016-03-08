<h1>Piped Apache Client OutputStream</h1>

<h2>About</h2>
Common Java HTTP clients do not provide direct access to the HTTP POST OutputStream, instead providing an InputStream-only interface.  There are cases where a handle on the OutputStream is preferred, and this project serves that purpose.

The PipedApacheClientOutputStream inverts Apache Commons HTTP Client API to allow for calling-code-control over OutputStream for HTTP message POST.

<h2>Note</h2>

This project was started from a DropWizard template, but can and likely will be used as a lib.  To integrate with a larger composing project, manually port the http.client package and gradle build dependencies, or use the jar distributed to build/libs.

<h2>Quick start</h2>

<h3>To exercise the OutputStream through test code</h3>

<pre>
./run-server.sh # starts node.js echo servlet that tests POST to
./gradlew test # runs all tests
</pre>

<h2>PipedApacheOutputStream Features</h2>

* Calling-code owns the executor service that spawns Apache client execution threads.
* Calling-code supplies the HttpClient instance so that calling code can manage things like HTTP connection pooling.
* Blocking on pipedOutputStream.close() (wait on pipedInputStream.close()) is optional.
** Blocking is useful in cases where immediately downstream calling code expects a new state incurred from the POST.

<h2>Roadmap</h2>
Please use github issues for roadmap and bug tasks.