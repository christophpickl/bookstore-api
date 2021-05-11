# Bookstore API

A sample ReST(-like) (Web-)API for a job assignment using Spring Boot and Kotlin.

Simply run the
class [`BookstoreApp`](https://github.com/christophpickl/bookstore-api/blob/master/src/main/kotlin/com/github/cpickl/bookstore/BookstoreApp.kt)
and open [http://localhost/swagger-ui.html](http://localhost/swagger-ui.html) in your browser, or make use of the
provided [Postman collection](https://github.com/christophpickl/bookstore-api/tree/master/src/doc).

## Features Overview

### Business

* Management of users, books and covers
* Multi-currency aware
* Search books

### Technical

* JWT role-based authentication
* Content negotiation (JSON, XML)

### Toolstack

* Kotlin 1.4, JDK 11, Gradle 6
* Spring Boot 2, Jackson
* OpenAPI 3
* JUnit 5, Mockito, Assertk, JSONAssert, XMLUnit
* Detekt

## How to start

* __Locally__:
  * Run the `BookstoreApp` class
  * Define the following VM arguments: `-Dspring.profiles.active=dev,dummyData`
* __Production__:
  * Docker ... TBD
  * Set mandatory environment variables:
    * `bookstore.hashSecret=xxx`
    * `bookstore.adminDefaultPassword=xxx`

## Further Ideas

_Now_:

* unit test for Id (equals, hashCode, ...)
* gradle todo plugin: https://github.com/HoldYourWaffle/gradle-todo-plugin-fixed
* admin scope CRUD users
* test SQL schema/"raw-values" (table name, column name, enum mapping values)
* spring rest controller: https://spring.io/guides/gs/accessing-data-rest/
* improve ErrorDto, add:  path, method, (optional) stacktrace
* test invalid currencyCode
* DB migration (https://thorben-janssen.com/database-migration-with-spring-boot/)
* split UT from IT => measure coverage
* document JWT in openAPI spec
* pagination
* custom sorting
* dockerize (+compose for DBMS; introduce prod-ready DBMS)
* hardening (improve exception handling, input (bean) validation)
* replace password type from String to CharArray for security reasons
* testcontainer: https://programmerfriend.com/spring-boot-integration-testing-done-right/
* make hibernate aware of custom ID type (no stringly typed anymore)
* DB layer hardening: cascade deletes
* security hardening: failed login (time delay? lock user?); token expired (tests)
* provide /favicon.ico

_Later_:

* BDD
* kotlin MPP (javascript, android)
* extend CI/CD pipeline (github/gitlab/travis?)
  * automated release (kotlin scripts)
  * cloud deployment (heroku?)
* HATEOS API (higher ReST maturity level via hypermedia)
* exposed persistence layer
* GraphQL API
* CQRS + event sourcing (kafka, reddis, elastic)
* Reactive API
