package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.InternalException
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import javax.xml.bind.annotation.XmlRootElement

@Schema(
    name = "Error",
    description = "Common API error object.",
)
@XmlRootElement(name = "error")
data class ErrorDto(

    @get:Schema(
        description = "HTTP status code.",
        required = true,
        example = "404",
    )
    val status: Int,

    @get:Schema(
        description = "Internal error code mapping.",
        required = true,
    )
    val code: ErrorCode,

    @get:Schema(
        description = "Human readable descriptive text.",
        required = true,
        example = "Oooops, something went wrong!",
    )
    val message: String,
    // TODO enhance ErrorDto
    // 2021-04-20T19:29:48.256+00:00
//    val timestamp: String,
//    val exception: String?,
//    val path: String,
)

@ControllerAdvice
class ExceptionHandlers {
    @ExceptionHandler(BookNotFoundException::class)
    fun handleBookNotFoundException(exception: BookNotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(
            ErrorDto(
                status = HttpStatus.NOT_FOUND.value(),
                code = ErrorCode.BOOK_NOT_FOUND,
                message = exception.displayMessage,
            )
        )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleJsonMappingException(exception: MethodArgumentTypeMismatchException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorDto(
                status = HttpStatus.BAD_REQUEST.value(),
                code = ErrorCode.INVALID_INPUT,
                message = exception.displayMessage,
            )
        )

    @ExceptionHandler(Exception::class, InternalException::class)
    fun handleException(exception: Exception) = ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorDto(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                code = ErrorCode.UNKNOWN,
                message = exception.displayMessage,
            )
        )

    private val Exception.displayMessage get() = "${this::class.simpleName}: $message"
}

enum class ErrorCode {
    UNKNOWN,
    BOOK_NOT_FOUND,
    INVALID_INPUT,
}
