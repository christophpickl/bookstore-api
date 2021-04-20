package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.CoverService
import com.github.cpickl.bookstore.domain.CoverUpdateRequest
import com.github.cpickl.bookstore.domain.Id
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/books/{id}/cover")
class CoverController(
    private val service: CoverService,
) {

    @Operation(
        operationId = "findBookCover",
        summary = "find cover image for the book",
        description = "Return custom cover PNG, else the default picture.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Book cover found."),
            ApiResponse(responseCode = "404", description = "Book cover not found.", content = [Content()]),
        ]
    )
    @GetMapping("", produces = [MediaType.IMAGE_PNG_VALUE])
    fun findBookCover(
        @PathVariable id: UUID
    ): ResponseEntity<ByteArray> =
        service.find(Id(id))?.let {
            ResponseEntity.ok(it.bytes)
        } ?: ResponseEntity.notFound().build()

    @Operation(
        operationId = "updateBookCover",
        summary = "update cover image for the book",
        description = "Send a multipart PNG file named 'cover-file'.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Book cover successfully updated."),
            ApiResponse(responseCode = "404", description = "Book cover not found.", content = [Content()]),
        ]
    )
    @PostMapping(
        "",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun updateBookCover(
        @PathVariable id: UUID,
        @RequestParam("cover-file") file: MultipartFile,
    ): ResponseEntity<Any> =
        service.update(Id(id), CoverUpdateRequest(file.bytes))?.let {
            ResponseEntity.noContent().build()
        } ?: ResponseEntity.notFound().build()

    // FUTURE delete cover

}
