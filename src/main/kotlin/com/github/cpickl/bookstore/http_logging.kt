package com.github.cpickl.bookstore

import mu.KLogger
import mu.KotlinLogging.logger
import org.springframework.boot.web.servlet.DispatcherType
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import java.lang.reflect.Type
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface LoggingService {
    fun logRequest(httpServletRequest: HttpServletRequest, body: Any?)
    fun logResponse(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, body: Any?)
}

@Service
class LoggingServiceImpl(
    private val log: KLogger = logger {}
) : LoggingService {

    override fun logRequest(httpServletRequest: HttpServletRequest, body: Any?) {
        log.info {
            val requestHeaders = httpServletRequest.headersMap
            val preparedBody = prepareBody(body, requestHeaders)
            "REQUEST: ${httpServletRequest.method} ${httpServletRequest.requestURI}" +
                    "\n\tHEADERS: $requestHeaders" +
                    if (preparedBody != null) {
                        "\n\tBODY: $preparedBody"
                    } else ""
        }
    }

    override fun logResponse(
        httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, body: Any?
    ) {
        log.info {
            val responseHeaders = httpServletResponse.headersMap
            val preparedBody = prepareBody(body, responseHeaders)
            "RESPONSE: ${httpServletRequest.method} ${httpServletRequest.requestURI} >> ${httpServletResponse.status}" +
                    "\n\tHEADERS: $responseHeaders" +
                    if (preparedBody != null) {
                        "\n\tBODY: $preparedBody"
                    } else ""
        }
    }

    private fun prepareBody(body: Any?, headers: Map<String, String>): String? =
        if (
            body != null &&
            headers.getOrDefault(HttpHeaders.CONTENT_TYPE, "")
                .contains(MediaType.APPLICATION_JSON_VALUE, ignoreCase = true)
        ) {
            "<<$body>>"
        } else null

    private val HttpServletRequest.headersMap
        get(): Map<String, String> =
            headerNames.toList().associateWith { headerName ->
                getHeaders(headerName).toList().joinToString()
            }

    private val HttpServletResponse.headersMap
        get(): Map<String, String> =
            headerNames.toList().associateWith { headerName ->
                getHeaders(headerName).toList().joinToString()
            }
}

@Component
class LogGetRequestsInterceptor(
    private val loggingService: LoggingService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.isLoggable) {
            loggingService.logRequest(request, null)
        }
        return true
    }

    private val HttpServletRequest.isLoggable
        get() =
            dispatcherType.name == DispatcherType.REQUEST.name &&
                    method == HttpMethod.GET.name

    override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, o: Any, model: ModelAndView?) {
        // no-op
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, o: Any, e: Exception?) {
        // no-op
    }
}

@ControllerAdvice
class LogPostEtcRquestsAdvice(
    private val loggingService: LoggingService,
    private val httpServletRequest: HttpServletRequest,
) : RequestBodyAdviceAdapter() {

    override fun supports(methodParameter: MethodParameter, type: Type, aClass: Class<out HttpMessageConverter<*>>) =
        true

    override fun afterBodyRead(
        body: Any, inputMessage: HttpInputMessage, parameter: MethodParameter, targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>
    ): Any {
        loggingService.logRequest(httpServletRequest, body)
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType)
    }
}


@ControllerAdvice
class LogResponseAdvice(
    private val loggingService: LoggingService,
) : ResponseBodyAdvice<Any> {

    override fun supports(methodParameter: MethodParameter, aClass: Class<out HttpMessageConverter<*>>) = true

    override fun beforeBodyWrite(
        body: Any?,
        methodParameter: MethodParameter,
        mediaType: MediaType,
        aClass: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        if (request is ServletServerHttpRequest && response is ServletServerHttpResponse) {
            loggingService.logResponse(request.servletRequest, response.servletResponse, body)
        }
        return body
    }
}
