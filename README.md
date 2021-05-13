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

* __Local IDE__:
  * Run the `BookstoreApp` class
  * Define the following VM arguments: `-Dspring.profiles.active=dev,insertDummyData`
* __Local Gradle__:
  * Use the spring boot command: `./gradlew bootRun --args='--spring.profiles.active=dev'`
* __Local Docker__:
  * Build an image: `docker build -t bookstore-api:1.0 .`
  * Run in container with development profile: `docker run -e SPRING_PROFILES_ACTIVE=dev -p 80:80 -t bookstore-api:1.0`
* __Production__:
  * Set mandatory environment variables:
    * `bookstore.hashSecret=xxx`
    * `bookstore.adminDefaultPassword=xxx`

## Further Ideas

_Now_:

* DB migration (https://thorben-janssen.com/database-migration-with-spring-boot/)
* H2 should only be included be for DEV mode, not for PROD mode!
* change log date format when in prod mode
* unit test for Id (equals, hashCode, ...)
* test invalid currencyCode
* gradle todo plugin: https://github.com/HoldYourWaffle/gradle-todo-plugin-fixed
* admin scope CRUD users
* docker compose enabled integration tests (seperate
  project, `docker-compose -f ./docker-compose.integration-test.yml up`)

_Later_:

* pagination
* custom sorting
* cover image: validate/resize
* make hibernate aware of custom ID type (no stringly typed anymore)
* test SQL schema/"raw-values" (table name, column name, enum mapping values)
* split UT from IT => measure coverage
* document JWT in openAPI spec
* hardening: improve exception handling, input (bean) validation
* replace password type from String to CharArray for security reasons
* DB layer hardening: cascade deletes (tests!)
* security hardening: failed login (time delay? lock user?); token expired (tests)
* when requesting nonexisting endpoint with invalid accept, tomact error is shown => show custom page

_Dream_:

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
