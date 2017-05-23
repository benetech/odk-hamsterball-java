FROM openjdk:8-jdk

# Control Java heap and metaspace sizes
ENV MIN_HEAP 256m
ENV MAX_HEAP 1024m
ENV MAX_METASPACE 128m

ENV JAVA_OPTS -server -Xms$MIN_HEAP -Xmx$MAX_HEAP -XX:MaxMetaspaceSize=$MAX_METASPACE -XX:+UseG1GC

# This Dockerfile runs an insecure instance of ODK 2.0 (Hamster) Server on the default Tomcat port
# It is intended to be installed behind an SSL proxy

MAINTAINER Benetech <cadenh@benetech.org>

ENV ODK_URL='http://localhost:8080' 
    
VOLUME /tmp

ADD ./target/odk-hamsterball*.jar odk-hamsterball-client.jar
RUN sh -c 'touch /odk-hamsterball-client.jar'
    
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /odk-hamsterball-client.jar" ]
    
EXPOSE 8090