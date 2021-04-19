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
* JUnit 5, Mockito, Assertk
* Detekt

### Ideas

* JPA/exposed persistence layer
* dockerize (+compose for DBMS)
* hardening (input validation, exception handling)
* HATEOS API (higher ReST maturity level via hypermedia)
* replace password type from String to CharArray for security reasons
* fine-tune XML representation
* reactive API
