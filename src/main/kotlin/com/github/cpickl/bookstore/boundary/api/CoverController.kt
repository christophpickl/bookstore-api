package com.github.cpickl.bookstore.boundary.api

import com.github.cpickl.bookstore.domain.CoverService
import com.github.cpickl.bookstore.domain.CoverUpdateRequest
import com.github.cpickl.bookstore.domain.Roles
import com.github.cpickl.bookstore.domain.unaryPlus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import javax.annotation.security.PermitAll
import javax.annotation.security.RolesAllowed

@Tag(
    name = "Cover API",
    description = "CRUD operations for covers (partially secured)."
)
@RestController
@RequestMapping("/api/books/{id}/cover")
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
            ApiResponse(responseCode = "200", description = "Book cover found.", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Book cover not found.", content = [Content()]),
        ]
    )
    @GetMapping("", produces = [MediaType.IMAGE_PNG_VALUE])
    @PermitAll
    fun findBookCover(
        @PathVariable id: UUID
    ): ByteArray = service.find(+id).bytes

    @Operation(
        operationId = "updateBookCover",
        summary = "create/update cover image for the book",
        description = "Send a multipart PNG file named 'cover-file'.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Book cover successfully updated.", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Book cover not found.", content = [Content()]),
        ]
    )
    @PutMapping(
        "",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @RolesAllowed(Roles.user)
    fun updateBookCover(
        @PathVariable id: UUID,
        @RequestParam("cover-file") file: MultipartFile,
    ): ResponseEntity<Any> {
        service.update(+id, CoverUpdateRequest(file.bytes))
        return ResponseEntity.noContent().build()
    }


    @Operation(
        operationId = "deleteBookCover",
        summary = "delete cover image for the book",
        description = "Resets the book's cover image to the default one.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204", description = "Book cover deleted or none was existing before.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "404", description = "Book cover not found.", content = [Content()]),
        ]
    )
    @DeleteMapping("")
    @RolesAllowed(Roles.user)
    fun deleteBookCover(
        @PathVariable id: UUID,
    ): ResponseEntity<Any> {
        service.delete(+id)
        return ResponseEntity.noContent().build()
    }
}
