# About The Dockerfile

The included [Dockerfile](Dockerfile) is to run an insecure instance which will reside behind an SSL proxy.  SSL proxy not included.

The [Dockerfile](Dockerfile) is in the root directory.

The following environment variable needs to be overridden.

Environment variables to override (with sample values):
+ ODK\_URL=http://192.168.86.113:8080

ODK_URL is the address of the [ODK Hamster Web Service](https://github.com/benetech/odk-hamster).  Make sure you've set that up first, or this web client won't do much.

Note that ODK_URL cannot be localhost, since the ODK Hamster Web Service will reside in a separate Docker container.

The Tomcat instance uses the port 8090, so that you can run it on the same machine with the ODK Hamster Web Service and it will not conflict with that service on port 8080.

## Building and running in a Docker container

Build the [Dockerfile](Dockerfile) in the project root directory with tag odk_cli
```shell
docker build -t odk_cli .
```
Install and start Docker container with:
+ Name ```odk_webclient```.
+ From image ```odk_cli```.
+ Forwarding the Tomcat port
+ Overriding the web service environment variable.

```shell
docker run -d -i -t --name odk_webclient -p 8090:8090  -e "ODK_URL=http://192.168.86.113:8080" odk_cli
```
Tail the log:
```shell
docker logs -f odk_webclient
```
Stop and delete the image and container. (This can be helpful if you realize you need to do some additional configuration on your Docker container and you would like to start over from the `docker build` command.
```shell
docker stop odk_webclient;docker rm odk_webclient;docker rmi odk_cli
```

