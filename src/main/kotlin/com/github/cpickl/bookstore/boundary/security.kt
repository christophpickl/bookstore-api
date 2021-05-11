package com.github.cpickl.bookstore.boundary

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.cpickl.bookstore.domain.BookstoreException
import com.github.cpickl.bookstore.domain.ErrorCode
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Service
import java.util.Date
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class LoginDto(
    val username: String,
    val password: String,
) {
    companion object
}

@Service
class AuthenticationUserDetailService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    private val log = logger {}

    override fun loadUserByUsername(username: String): UserDetails? {
        log.debug { "Loading user '$username' ..." }
        val user = userRepository.findByUsername(username) ?: throw UsernameNotFoundException(username)
        return User(user.username, user.passwordHash, emptyList())
    }
}

@Configuration
@EnableGlobalMethodSecurity(
    jsr250Enabled = true,
    securedEnabled = true,
    prePostEnabled = true,
)
class ConfigureSpringSecurityAnnotations : GlobalMethodSecurityConfiguration()

@EnableWebSecurity
class SecurityConfiguration(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val authenticationUserDetailService: AuthenticationUserDetailService,
    private val jackson: ObjectMapper,
    private val userRepo: UserRepository,
    private val errorFactory: ErrorFactory,
    @Value("\${bookstore.hashSecret}") private val hashSecret: String,
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        val hashSecretBytes = hashSecret.toByteArray()
        // @formatter:off
        http
        .cors().and()
        .csrf().disable()
        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
        .addFilter(JWTAuthenticationFilter(authenticationManager(), userRepo, jackson, hashSecretBytes))
        .addFilter(JWTAuthorizationFilter(authenticationManager(), userRepo, errorFactory, hashSecretBytes))
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        // @formatter:on
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService<UserDetailsService>(authenticationUserDetailService).passwordEncoder(passwordEncoder)
    }

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint =
        CustomAuthenticationEntryPoint(errorFactory)
}

class CustomAuthenticationEntryPoint(
    private val errorFactory: ErrorFactory,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val error = errorFactory.build(ErrorContext(authException, request, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN))
        response.prepareBy(error)
    }
}

private fun UserRepository.findAuthoritiesFor(username: String): Collection<GrantedAuthority> =
    findByUsername(username)?.roles?.map { SimpleGrantedAuthority(it.roleName) } ?: emptyList()

class JWTAuthenticationFilter(
    private val authManager: AuthenticationManager,
    private val userRepo: UserRepository,
    private val jackson: ObjectMapper,
    private val hashSecret: ByteArray,
) : UsernamePasswordAuthenticationFilter() {

    companion object {
        private const val TOKEN_EXPIRATION_TIME = 864_000_000 // 10 days
    }

    private val log = logger {}

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val login = jackson.readValue<LoginDto>(request.inputStream)
        val authorities = userRepo.findAuthoritiesFor(login.username)
        return authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                login.username,
                login.password,
                authorities,
            )
        )
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        auth: Authentication
    ) {
        val user = auth.principal as User
        log.debug { "Successfully authenticated: ${user.username}" }
        val token = JWT.create()
            .withSubject(user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
            .sign(Algorithm.HMAC512(hashSecret))
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    }
}

class JWTAuthorizationFilter(
    authenticationManager: AuthenticationManager,
    private val userRepo: UserRepository,
    private val errorFactory: ErrorFactory,
    private val hashSecret: ByteArray,
) : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response)
            return
        }
        val authentication = try {
            extractAuthentication(request)
        } catch (e: BookstoreAuthException) {
            response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            val error = errorFactory.build(ErrorContext(e, request, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN))
            response.prepareBy(error)
            return
        }
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(request, response)
    }

    private fun extractAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)?.replace("Bearer ", "") ?: return null
        val jwt = try {
            JWT.require(Algorithm.HMAC512(hashSecret)).build().verify(token)
        } catch (e: SignatureVerificationException) {
            throw BookstoreAuthException("Failed to verify token: [$token]", e)
        }

        val user = jwt.subject
        val authorities = userRepo.findAuthoritiesFor(user)
        return UsernamePasswordAuthenticationToken(user, null, authorities)
    }
}

class BookstoreAuthException(internalMessage: String, cause: Exception) :
    BookstoreException(internalMessage, "Authentication failed", cause)

private fun HttpServletResponse.prepareBy(error: ErrorResponse) {
    status = error.status.value()
    setHeader(HttpHeaders.ACCEPT, error.contentType.toString())
    writer.write(error.content)
}
