FROM openjdk:11-jre-slim-buster
COPY build/libs/bookstore-api.jar bookstore-api.jar
ENTRYPOINT ["java","-jar","/bookstore-api.jar"]
