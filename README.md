# bookstore-api

A sample project for a job assignment using Spring Boot and Kotlin.

## Requirements

* [x] content negotiation (JSON, XML)
* [x] User model: author pseudonym field
* [x] Book model: title, description, author (User reference), cover image, price
* [X] JWT auth (user/pass based)
* `/books` resource
    * [x] GET list and detail (public, no auth required)
    * [ ] search book list via query params
    * [x] CRU_ operations (for auth only)
    * [ ] DELETE unpublishes a book (only when user owns it)

### Technical

* [ ] JPA persistence
* [ ] hardening (input validation, exception handling)
* [ ] openAPI doc (+postman collection)
* [ ] docker container (+compose for real DBMS)
* [ ] build plugins (static code analysis, coverage, dependencies)
* [ ] replace password:String with password:CharArray for improved security
