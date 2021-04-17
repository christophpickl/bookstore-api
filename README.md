# bookstore-api

A sample project for a job assignment using Spring Boot and Kotlin.

## Requirements

* [x] content negotiation (JSON, XML)
* [x] User model: author pseudonym field
* [x] Book model: title, description, author (User reference), cover image, price
* [X] JWT auth (user/pass based)
* `/books` resource
    * [x] GET list and detail (public, no auth required)
    * [x] search book list via query params
    * [x] CRU_ operations (for auth only)
    * [x] DELETE unpublishes a book

## Technical Ideas

* JPA/exposed persistence layer
* dockerize (+compose for DBMS)
* hardening (input validation, exception handling)
* openAPI doc
* HATEOS API (higher ReST maturity level via hypermedia)
* build plugins for reports/checks (static code analysis, coverage, dependencies)
* replace password type from String to CharArray for security reasons
* fine-tune XML representation
* reactive API
