#!/bin/zsh

echo "Building gradle artifact ..."
./gradlew build --exclude-task test

echo "Building docker image ..."
docker build -t bookstore/bookstore-api:latest .

echo "Starting up docker-compose ..."
docker-compose up
