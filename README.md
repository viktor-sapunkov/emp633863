# Installation

`git clone` the project, then `cd url-shortener` and finally, build it with `mvn clean package`.

# Launching

Execute it with `java -jar JARFILENAME-VERSION.jar [port number]` (the port number is optional and defaults to `8088`).
Example command line: `java -server -jar url-shortener-1.0-SNAPSHOT.jar 8088` (please make sure to pick up a free port otherwise you get an `Address in use` error).

# Usage

For usage guidelines, please follow the instructions above and then open `/help.html` URL in your favorite browser. E.g. open this page: `http://localhost:8088/help.html`.
