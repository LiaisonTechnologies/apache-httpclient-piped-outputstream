<h1>Apache Client Outputstream Handler</h1>

<i>This is a work in progress.  Tests are still minimal at this point and need improvement.</i>

<p>ACOH inverts Apache Client's API so that client threads can control outputstream at runtime.</p>

<h2>Quick start</h2>

To sanity check and exercise code, run the nodejs server from the root of project with ./run-server.sh.  You may need to chmod +x, and install node before this will work.  Once the server is running, the tests will run out of the box.  Otherwise tweak the hardcoded url.

Note - Made as dropwizard project but needs to deploy as a simple single file.

Calling code

* Owns the executor service that spawns Apache client execution threads
* Supplies the HttpClient instance so that calling code can manage http connection pool
** But outputstream owns the HttpPOST because in order for this to work we need to do specific things like set the InputStreamEntity
* Blocking is optional
** Blocking means that outputStream.close() will block until inputstream.close() is called.

Testing

Tests are Junit (gradle default)

* Run ./run-server.sh to start a server on localhost:3000
* Tests will post to localhost:3000 by default
* ./gradlew test

