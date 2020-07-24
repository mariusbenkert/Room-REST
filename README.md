# Room-REST

## Introduction
This project was part of a programming exam over a period of three months. The goal was to build a working RESTful API for a given theme. Our task was to build one for a basic room
administration including making/deleting/changing reservations for said rooms.

## Compatibility
``` 
Java Version 8 
```

## Building & Running
```
git clone https://github.com/CaptainKappa/Room-REST.git
cd Room-Rest
docker-compose up
mvn -DskipTests=true package
java -jar target/examtemplate-0.0.1-jar-with-dependencies.jar
```

## Functionality
In our REST-API we are offering the following requests for our ressources rooms and reservations.

__StartService__

`GET` http://localhost:8080/exam/api


__RoomService__

`GET, POST` http://localhost:8080/exam/api/rooms

`GET, PUT, DELETE` http://localhost:8080/exam/api/rooms/{id}

`GET, POST` http://localhost:8080/exam/api/rooms/{id}/reservations

`GET, PUT, DELETE` http://localhost:8080/exam/api/rooms/{id}/reservations/{id}

***
__Mediatype Ressources__

Requests: `JSON & XML`

Responses: `JSON & XML`

***
__Database__

We connect to a locally hosted mongo-db via the `Java MongoDB Driver` which stores our data and is also the place where we query our data.


## Author 
- Marius Benkert 
- Paul Väthjunker
