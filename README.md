# Installation

`git clone` the project:

```git clone https://github.com/viktor-sapunkov/emp633863.git```

then `cd emp633863/url-shortener` and finally, build it with `mvn clean package`.

# Launching

Having completed the above instructions for project cloning and building processes, do a `cd target` (that's from `emp633863/url-shortener` subdirectory) and then start the project's JAR file with `java -server -jar JARFILENAME-VERSION.jar [port number]` (the port number is optional and defaults to `8088`).

An example command line would be: `java -server -jar url-shortener-1.0-SNAPSHOT.jar 8088`

Please make sure to pick up a free port - otherwise you get an `Address in use` error.

# Usage

For usage guidelines, please follow the instructions above and then open `/help.html` URL in your favorite browser. E.g. open this page: `http://localhost:8088/help.html`.
