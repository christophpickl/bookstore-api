package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.github.cpickl.bookstore.common.Clock
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.ErrorCode
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.InternalException
import com.github.cpickl.bookstore.domain.UserNotFoundException
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.read
import com.github.cpickl.bookstore.requestPost
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import kotlin.reflect.KClass

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
class ExceptionHandlingApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {

    @MockBean
    private lateinit var clock: Clock
    private val message = "test exception message"
    private val dateTimeString = "2021-05-05T11:17:31.518423"
    private val dateTime = LocalDateTime.parse(dateTimeString)
    private val anyId = Id.any()

    @BeforeEach
    fun `mock clock`() {
        whenever(clock.now()).thenReturn(dateTime)
    }

    @Test
    fun `Given verbose error handling is not activated When exception thrown Then payload doesnt contain exception`() {
        val error = restTemplate.requestException(
            ExceptionDefinitionDto(Exception::class, "any"), INTERNAL_SERVER_ERROR
        )

        assertThat(error.exception).isNull()
    }

    @Test
    fun `When Exception thrown Then proper ErrorDto returned`() {
        val error = restTemplate.requestException(
            ExceptionDefinitionDto(Exception::class, message), INTERNAL_SERVER_ERROR
        )

        assertThat(error).isMatching(
            status = 500,
            code = ErrorCode.UNKNOWN,
            message = "Internal error",
        )
    }

    @Test
    fun `When InternalException thrown Then proper ErrorDto returned`() {
        val error = restTemplate.requestException(
            ExceptionDefinitionDto(InternalException::class, message), INTERNAL_SERVER_ERROR
        )

        assertThat(error).isMatching(
            status = 500,
            code = ErrorCode.UNKNOWN,
            message = "Internal error",
        )
    }

    @Test
    fun `When BookNotFoundException thrown Then proper ErrorDto returned`() {
        val error = restTemplate.requestException(
            ExceptionDefinitionDto(BookNotFoundException::class, anyId), NOT_FOUND
        )

        assertThat(error).isMatching(
            status = 404,
            code = ErrorCode.BOOK_NOT_FOUND,
            message = "Book not found",
        )
    }

    @Test
    fun `When UserNotFoundException thrown Then proper ErrorDto returned`() {
        val error = restTemplate.requestException(
            ExceptionDefinitionDto(UserNotFoundException::class, anyId), NOT_FOUND
        )

        assertThat(error).isMatching(
            status = 404,
            code = ErrorCode.USER_NOT_FOUND,
            message = "User not found",
        )
    }

    @Test
    fun `When request secured endpoint without token Then proper ErrorDto returned`() {
        val response = restTemplate.requestPost("/api/books", null, HttpHeaders.EMPTY)

        assertThat(response.read<ErrorDto>(FORBIDDEN)).isMatching(
            status = 403,
            code = ErrorCode.FORBIDDEN,
            message = "Access denied",
            path = "/api/books"
        )
    }

    // TODO test bad request payload (body + query)

    private fun Assert<ErrorDto>.isMatching(
        status: Int,
        code: ErrorCode,
        message: String,
        method: String = "POST",
        path: String = "/test/throw"
    ) {
        given {
            assertThat(it).isEqualTo(
                ErrorDto(
                    status = status,
                    code = code,
                    message = message,
                    exception = null,
                    timestamp = dateTimeString,
                    method = method,
                    path = path,
                )
            )
        }
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
@ActiveProfiles("verboseErrorHandling")
class VerboseExceptionHandlingApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {

    private val message = "internal exception message"
    private val exceptionType = Exception::class
    private val id = Id.any()

    @Test
    fun `When exception thrown Then payload contains exception`() {
        val error = restTemplate.requestException(
            ExceptionDefinitionDto(exceptionType, message), INTERNAL_SERVER_ERROR
        )

        assertThat(error.exception).isNotNull().isEqualTo(
            ExceptionDto(exceptionType.qualifiedName!!, message, error.exception!!.stackTrace)
        )
        assertThat(error.exception!!.stackTrace).isNotEmpty()
    }

    @Test
    fun `When BookNotFoundException thrown Then proper ErrorDto returned`() {
        val error = restTemplate.requestException(
            ExceptionDefinitionDto(BookNotFoundException::class, id), NOT_FOUND
        )

        assertThat(error.exception!!.message).isEqualTo("Book not found by ID: $id")
    }
}

private fun TestRestTemplate.requestException(definition: ExceptionDefinitionDto, expected: HttpStatus) =
    requestPost("/test/throw", definition).read<ErrorDto>(expected)

@TestConfiguration
class TestConfig {

    @RestController
    @RequestMapping(
        "/test",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    class TestController {

        @PostMapping("/throw")
        fun throwException(@RequestBody definition: ExceptionDefinitionDto) {
            throw definition.build()
        }

        private fun ExceptionDefinitionDto.build(): Exception {
            @Suppress("UNCHECKED_CAST")
            val clazz = Class.forName(fullQualifiedName) as Class<Exception>
            val ctor = clazz.getConstructor(*arguments.map { Class.forName(it.fullQualifiedName) }.toTypedArray())
            return ctor.newInstance(*arguments.map {
                when (it.fullQualifiedName) {
                    String::class.java.name -> it.value
                    Id::class.qualifiedName -> Id(it.value)
                    else -> throw IllegalArgumentException("Unhandled argument: $it")
                }
            }.toTypedArray())
        }
    }
}

data class ExceptionDefinitionDto(
    val fullQualifiedName: String,
    val arguments: List<ArgumentDto>
) {
    constructor(exceptionType: KClass<out Exception>, vararg arguments: Any) : this(
        fullQualifiedName = exceptionType.qualifiedName!!,
        arguments = arguments.map { ArgumentDto(it) },
    )
}

data class ArgumentDto(
    val fullQualifiedName: String,
    val value: String,
) {
    constructor(argument: Any) : this(
        fullQualifiedName = argument.javaClass.name,
        value = argument.toString(),
    )
}
