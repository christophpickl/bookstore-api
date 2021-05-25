FROM openjdk:11-jre-slim-buster
#FROM adoptopenjdk:11-jre-hotspot

LABEL maintainer="christoph.pickl@gmail.com"
#COPY build/libs/bookstore-api.jar bookstore-api.jar
ADD build/libs/bookstore-api.jar bookstore-api.jar

#VOLUME /tmp
EXPOSE 80
# don't run as root
#RUN addgroup -S spring && adduser -S spring -G spring
#USER spring:spring

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/bookstore-api.jar"]
