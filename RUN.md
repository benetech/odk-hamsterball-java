# Run Open Data Kit Hamsterball Client

Here's how you get Open Data Kit Hamsterball client up and running locally.

## Prerequisites
It is assumed you have working knowledge of Git, Maven, and Tomcat.

* [Open Data Kit Hamster Web Service](https://github.com/benetech/odk-hamster)
* Maven 3.3.3 or above
* Java 8

## First, install ODK Hamster Web Service

[ODK Hamster Web Service installation instructions are here.](https://github.com/benetech/odk-hamster/blob/master/RUN.md)

##  Second, check out source code.

```shell
git clone git@github.com:benetech/odk-hamsterball-java.git
```

## Third, compile the code

From the odk-hamsterball-java directory, run:

```shell
mvn clean install -DskipTests
```

If you'd rather run unit tests:

```shell
mvn clean install
```

The first unit tests are in progress so there may not be much difference between the two commands.

## Fourth, run the code

The code runs by default at port 8090, so that you can run it on the same server as odk-hamster, which uses port 8080 by default.  You may want to disable any other services you're running that are using port 8090 at this time.

Start the web client:

```shell
java -jar target/*.jar
```

Optionally, if you must run on another port, add the following line to `odk-hamsterball-java/src/main/resources/application-default.properties` where `9009` is a port you know to be free.
```
server.port=9009
```

## You're running!

The service will be launched by default at http://localhost:8090.  There should be a login page.
The default login information is username: `admin`, password: `aggregate`.

## What's next?

Do you want to run this web service inside a Docker container? [Read the Docker container instructions.](DOCKER.md)

Do you want to see this project in Eclipse?  [Read the IDE instructions.](https://github.com/benetech/odk-hamster/blob/master/ECLIPSE.md)
