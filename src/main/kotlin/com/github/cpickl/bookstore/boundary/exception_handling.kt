package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.cpickl.bookstore.common.Clock
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.BookstoreException
import com.github.cpickl.bookstore.domain.ErrorCode
import com.github.cpickl.bookstore.domain.InternalException
import com.github.cpickl.bookstore.domain.UserNotFoundException
import io.swagger.v3.oas.annotations.media.Schema
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletRequest
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

    @get:Schema(
        description = "Formatted datetime when exception was thrown.",
        required = true,
        example = "2021-04-20T19:29:48.256+00:00",
    )
    val timestamp: String,

    @get:Schema(
        description = "Request path when the error happened.",
        required = true,
        example = "/api/books",
    )
    val path: String,

    @get:Schema(
        description = "The request's HTTP method.",
        required = true,
        example = "GET",
    )
    val method: String,

    @get:Schema(
        description = "Optional name, message and stacktrace of the thrown exception.",
        required = false,
    )
    val exception: ExceptionDto?,
)

@Schema(
    name = "Exception",
    description = "Representing a Java typical Exception instance.",
)
data class ExceptionDto(
    @get:Schema(
        description = "Fully qualified class name.",
        required = true,
        example = "java.lang.Exception",
    )
    val name: String,

    @get:Schema(
        description = "The custom, internal exception message, potentially exposing security relevant information.",
        required = true,
        example = "Something went wrong.",
    )
    val message: String,

    @get:Schema(
        description = "The Java typical stacktrace of an Exception.",
        required = true,
    )
    val stackTrace: List<String>,
)

@Service
class ErrorDtoFactory(
    private val clock: Clock,
    @Value("\${bookstore.printExceptions}") private val printExceptions: Boolean,
) {
    private val log = logger {}

    fun build(
        exception: Exception,
        request: WebRequest,
        status: HttpStatus,
        code: ErrorCode,
    ) = build(exception, (request as ServletWebRequest).request, status, code)

    fun build(
        exception: Exception,
        request: HttpServletRequest,
        status: HttpStatus,
        code: ErrorCode,
    ): ErrorDto {
        log.trace { "$request => caused: $exception" }
        return ErrorDto(
            status = status.value(),
            code = code,
            message = buildMessage(exception, status),
            timestamp = clock.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            method = request.method,
            path = request.requestURI,
            exception = if (printExceptions) exception.toExceptionDto() else null,
        )
    }

    private fun buildMessage(exception: Exception, status: HttpStatus) = when (exception) {
        is BookstoreException -> exception.domainMessage
        is MethodArgumentTypeMismatchException -> "Bad request"
        else -> when (status) {
            HttpStatus.INTERNAL_SERVER_ERROR -> "Internal error"
            HttpStatus.FORBIDDEN -> "Access denied"
            HttpStatus.NOT_FOUND -> "Not found"
            HttpStatus.BAD_REQUEST,
            HttpStatus.NOT_ACCEPTABLE,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            HttpStatus.METHOD_NOT_ALLOWED,
            -> "Bad request"
            else -> "N/A"
        }
    }
}

private fun Exception.toExceptionDto(): ExceptionDto {
    return ExceptionDto(
        name = this::class.qualifiedName ?: "N/A",
        message = message ?: "N/A",
        stackTrace = stackTrace.map { it.toString() },
    )
}

@ControllerAdvice
class ExceptionHandlers(
    private val errorFactory: ErrorDtoFactory,
    private val jackson: ObjectMapper,
) {

    private val log = logger {}

    @ExceptionHandler(BookNotFoundException::class)
    fun handleBookNotFoundException(exception: BookNotFoundException, request: WebRequest) =
        buildResponseEntity(exception, request, HttpStatus.NOT_FOUND, ErrorCode.BOOK_NOT_FOUND)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleBookNotFoundException(exception: UserNotFoundException, request: WebRequest) =
        buildResponseEntity(exception, request, HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleJsonMappingException(exception: MethodArgumentTypeMismatchException, request: WebRequest) =
        buildResponseEntity(exception, request, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(exception: HttpMessageNotReadableException, request: WebRequest) =
        buildResponseEntity(exception, request, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST)

    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleHttpMediaTypeNotAcceptableException(
        exception: HttpMediaTypeNotAcceptableException, request: WebRequest
    ): ResponseEntity<String> {
        val errorDto = errorFactory.build(exception, request, HttpStatus.NOT_ACCEPTABLE, ErrorCode.BAD_REQUEST)
        // as there is no proper accept header in the request, we just have to assume JSON is ok for the error response
        return ResponseEntity
            .status(HttpStatus.NOT_ACCEPTABLE)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            // FUTURE could indicate which mediatypes are actually accepted for this endpoint
            .body(jackson.writeValueAsString(errorDto))

    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(exception: HttpMediaTypeNotSupportedException, request: WebRequest) =
        buildResponseEntity(exception, request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.BAD_REQUEST)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        exception: HttpRequestMethodNotSupportedException, request: WebRequest
    ) =
        buildResponseEntity(exception, request, HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.BAD_REQUEST)

    @ExceptionHandler(
        AuthenticationException::class,
        InsufficientAuthenticationException::class,
    )
    fun handleAuthenticationException(exception: AuthenticationException, request: WebRequest) =
        buildResponseEntity(exception, request, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN)

    @ExceptionHandler(
        Exception::class,
        InternalException::class,
    )
    fun handleException(exception: Exception, request: WebRequest): ResponseEntity<ErrorDto> {
        log.error(exception) { "Unhandled exception was thrown, going to return 500!" }
        return buildResponseEntity(exception, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN)
    }

    private fun buildResponseEntity(
        exception: Exception,
        request: WebRequest,
        status: HttpStatus,
        code: ErrorCode,
    ): ResponseEntity<ErrorDto> = ResponseEntity
        .status(status)
        .body(errorFactory.build(exception, request, status, code))
}

//@Configuration
//class AdditionalHandler {
//    @Bean
//    fun authenticationFailureHandler(): AuthenticationFailureHandler {
//        return AuthenticationFailureHandler { _, _, exception -> throw exception }
//    }
//}
