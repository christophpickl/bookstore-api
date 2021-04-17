package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.Id
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/books/{id}/cover", produces = [MediaType.IMAGE_PNG_VALUE])
class CoverController(
    private val service: BookService
) {

    @GetMapping("")
    fun findBookCover(
        @PathVariable id: UUID
    ): ResponseEntity<ByteArray> =
        service.findOrNull(Id(id))?.let {
            ResponseEntity.ok(it.cover.bytes)
        } ?: ResponseEntity.notFound().build()

    // FUTURE cover update/delete operations

}