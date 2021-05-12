package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.github.cpickl.bookstore.TestUserPreparer
import com.github.cpickl.bookstore.common.Clock
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.ErrorCode
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.InternalException
import com.github.cpickl.bookstore.domain.Roles
import com.github.cpickl.bookstore.domain.UserNotFoundException
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.read
import com.github.cpickl.bookstore.requestDelete
import com.github.cpickl.bookstore.requestGet
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
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID
import javax.annotation.security.RolesAllowed
import kotlin.reflect.KClass

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ExceptionHandlingTestConfig::class)
@ActiveProfiles("test")
class ExceptionHandlingApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: TestUserPreparer,
) {

    @MockBean
    private lateinit var clock: Clock
    private val message = "test exception message"
    private val dateTimeString = "2021-05-05T11:17:31.518423"
    private val dateTime = LocalDateTime.parse(dateTimeString)
    private val anyId = Id.any()
    private val unacceptableMediaType = MediaType.APPLICATION_CBOR_VALUE
    private val unsupportedContentType = MediaType.IMAGE_PNG_VALUE
    private val invalidUuid = "invalidUuid"

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
    fun `When Exception thrown Then return error`() {
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
    fun `When InternalException thrown Then return error`() {
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
    fun `When BookNotFoundException thrown Then return error`() {
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
    fun `When UserNotFoundException thrown Then return error`() {
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
    fun `When request secured endpoint without token Then return error`() {
        val response = restTemplate.requestGet("/test/user_secured")

        assertThat(response.read<ErrorDto>(FORBIDDEN)).isMatching(
            status = 403,
            code = ErrorCode.FORBIDDEN,
            message = "Access denied",
            method = "GET",
            path = "/test/user_secured"
        )
    }

    @Test
    fun `When request secured endpoint with invalid token Then return error`() {
        val response = restTemplate.requestGet("/test/user_secured", headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${Jwt.any()}")
        })

        assertThat(response.read<ErrorDto>(FORBIDDEN)).isMatching(
            status = 403,
            code = ErrorCode.FORBIDDEN,
            message = "Authentication failed",
            method = "GET",
            path = "/test/user_secured"
        )
    }

    @Test
    fun `Given user When request admin secured endpoint Then return error`() {
        userPreparer.saveTestUser()
        val jwt = restTemplate.login(userPreparer.userLogin)
        val response = restTemplate.requestGet("/test/admin_secured", headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
        })

        assertThat(response.read<ErrorDto>(FORBIDDEN)).isMatching(
            status = 403,
            code = ErrorCode.FORBIDDEN,
            message = "Access denied",
            method = "GET",
            path = "/test/admin_secured"
        )
    }

    @Test
    fun `When get unacceptable mediatype Then default to JSON and return error`() {
        val response = restTemplate.requestGet("/test/success", HttpHeaders().apply {
            set(HttpHeaders.ACCEPT, unacceptableMediaType)
        })

        assertThat(response.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
        assertThat(response.read<ErrorDto>(NOT_ACCEPTABLE)).isMatching(
            status = 406,
            code = ErrorCode.BAD_REQUEST,
            message = "Bad request",
            method = "GET",
            path = "/test/success",
        )
    }

    @Test
    fun `When post with unsupported contenttype Then return error`() {
        val response = restTemplate.requestPost("/test/success", "any body", HttpHeaders().apply {
            set(HttpHeaders.CONTENT_TYPE, unsupportedContentType)
        })

        assertThat(response.read<ErrorDto>(HttpStatus.UNSUPPORTED_MEDIA_TYPE)).isMatching(
            status = 415,
            code = ErrorCode.BAD_REQUEST,
            message = "Bad request",
            method = "POST",
            path = "/test/success",
        )
    }

    @Test
    fun `When post with invalid body Then return error`() {
        val response = restTemplate.requestPost("/test/success", "invalid json", HttpHeaders().apply {
            set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        })

        assertThat(response.read<ErrorDto>(BAD_REQUEST)).isMatching(
            status = 400,
            code = ErrorCode.BAD_REQUEST,
            message = "Bad request",
            method = "POST",
            path = "/test/success",
        )
    }

    @Test
    fun `When request with unsupported method Then return error`() {
        val response = restTemplate.requestDelete("/test/success")

        assertThat(response.read<ErrorDto>(METHOD_NOT_ALLOWED)).isMatching(
            status = 405,
            code = ErrorCode.BAD_REQUEST,
            message = "Bad request",
            method = "DELETE",
            path = "/test/success",
        )
    }

    @Test
    fun `When pass invalid query param Then return error`() {
        val response = restTemplate.requestGet("/test/success/$invalidUuid")

        assertThat(response.read<ErrorDto>(BAD_REQUEST)).isMatching(
            status = 400,
            code = ErrorCode.BAD_REQUEST,
            message = "Bad request",
            method = "GET",
            path = "/test/success/$invalidUuid",
        )
    }

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
@Import(ExceptionHandlingTestConfig::class)
@ActiveProfiles("verboseErrorHandling", "test")
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
class ExceptionHandlingTestConfig {

    @RestController
    @RequestMapping(
        "/test",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    class TestController {

        @GetMapping("/success")
        fun getSuccess() = SuccessDto()

        @GetMapping("/success/{id}")
        fun getSuccessWithId(@PathVariable id: UUID) = SuccessDto(id.toString())

        @PostMapping(
            "/success",
            consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        )
        fun postSuccess(@RequestBody dto: SuccessDto) = SuccessDto("Hello ${dto.message}!")

        @PostMapping("/throw")
        fun throwException(@RequestBody definition: ExceptionDefinitionDto) {
            throw definition.build()
        }

        @GetMapping("/user_secured")
        @RolesAllowed(Roles.user)
        fun getUserSecured() = SuccessDto()

        @GetMapping("/admin_secured")
        @RolesAllowed(Roles.admin)
        fun getAdminSecured() = SuccessDto()

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

data class SuccessDto(
    val message: String = "success"
)

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
