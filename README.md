# bookstore-api

A sample project for a job assignment using Spring Boot and Kotlin.

## Howto Run

* Startup the `BookstoreApp` class and define via JVM arguments the `-Dspring.profiles.active=prod`

## Requirements

* [ ] content negotiation (JSON, XML)
* [ ] User model: author pseudonym field
* [ ] Book model: title, description, author (User reference), cover image, price
* [ ] JWT auth (user/pass based)
* /books resource
    * [ ] GET list and detail (public, no auth required)
    * [ ] CRUD (for auth only)
    * [ ] DELETE (unpublish a book only user owns)

### Technical

* [ ] exception hanlding
* [ ] openAPI doc
* [ ] provide postman collection
* [ ] docker container
* [ ] upgrade to java 11

