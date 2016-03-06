<h1>Apache Client Outputstream Handler</h1>

<i>This is a work in progress.  Tests are still minimal at this point and need improvement.</i>

<p>ACOH inverts Apache Client's API so that client threads can control outputstream at runtime.</p>

<h2>Quick start</h2>

To sanity check and exercise code, run the nodejs server from the root of project with ./run-server.sh.  You may need to chmod +x, and install node before this will work.  Once the server is running, the tests will run out of the box.  Otherwise tweak the hardcoded url.

