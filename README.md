# Bookstore API

A sample ReST(-like) (Web-)API for a job assignment using Spring Boot and Kotlin.

Simply run the class [`BookstoreApp`](https://github.com/christophpickl/bookstore-api/blob/master/src/main/kotlin/com/github/cpickl/bookstore/BookstoreApp.kt) 
and open [http://localhost/swagger-ui.html](http://localhost/swagger-ui.html) in your browser, 
or make use of the provided [Postman collection](https://github.com/christophpickl/bookstore-api/tree/master/src/doc).

## Business Requirements

* [x] content negotiation (JSON, XML)
* [x] User model: author pseudonym field
* [x] Book model: title, description, author (User reference), cover image, price
* [X] JWT auth (user/pass based)
* `/books` resource
    * [x] GET list and detail (public, no auth required)
    * [x] search books via query params
    * [x] CRUD operations (for auth only)
    * [x] DELETE unpublishes a book

## Technical

### Toolstack

* Kotlin 1.4, JDK 11, Gradle 6
* Spring Boot 2.4.5, Jackson
* OpenAPI 3
* JUnit 5, Mockito, Assertk, JSONAssert, XMLUnit
* Detekt

### Ideas

_Now_:

* Book has no User but an Author field (no hashedPwd/username)
* admin scope CRUD users
* test SQL schema/"raw-values" (table name, column name, enum mapping values)
* spring rest controller: https://spring.io/guides/gs/accessing-data-rest/
* improve ErrorDto, add:  path, method, (optional) stacktrace
* test invalid currencyCode
* replace "whitelabel error page"
* DB migration (https://thorben-janssen.com/database-migration-with-spring-boot/)
* split UT from IT => measure coverage
* document JWT in openAPI spec
* pagination
* dockerize (+compose for DBMS; introduce prod-ready DBMS)
* hardening (improve exception handling, input (bean) validation)
* replace password type from String to CharArray for security reasons
* mini-frontend (kotlin HTML-DSL)
* testcontainer: https://programmerfriend.com/spring-boot-integration-testing-done-right/

_Later_:

* BDD
* kotlin MPP (javascript, android)
* CI/CD pipeline (github/gitlab/travis?)
  * automated release (kotlin scripts)
  * cloud deployment (heroku?)
* HATEOS API (higher ReST maturity level via hypermedia)
* exposed persistence layer
* GraphQL API
* CQRS + event sourcing (kafka, reddis, elastic)
* Reactive API
