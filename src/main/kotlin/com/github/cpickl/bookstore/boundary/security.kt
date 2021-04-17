package com.github.cpickl.bookstore.boundary

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Service
import java.util.Date
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object SecurityConstants {
    // FUTURE annotate controller methods instead
    val PERMIT_ALL_PATHS = listOf(
        HttpMethod.GET to "/",
        HttpMethod.POST to "/login",
        HttpMethod.GET to "/books",
        HttpMethod.GET to "/books/*",
        HttpMethod.GET to "/books/*/cover",
    )
    const val EXPIRATION_TIME = 864000000 // 10 days
    const val SECRET = "my_top_secret"

    val admin = LoginDto("admin", "admin")
    const val adminAuthorName = "admin author"
}

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
        val user = userRepository.findOrNull(username) ?: throw UsernameNotFoundException(username)
        return User(user.username, user.passwordHash, emptyList())
    }
}

@EnableWebSecurity
class SecurityConfiguration(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val authenticationUserDetailService: AuthenticationUserDetailService
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable().authorizeRequests()
            .let {
                SecurityConstants.PERMIT_ALL_PATHS.fold(it) { acc, path ->
                    acc.antMatchers(path.first, path.second).permitAll()
                }
            }
            .anyRequest().authenticated()
            .and()
            .addFilter(JWTAuthenticationFilter(authenticationManager()))
            // this disables session creation on Spring Security
            .addFilter(JWTAuthorizationFilter(authenticationManager()))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService<UserDetailsService>(authenticationUserDetailService).passwordEncoder(passwordEncoder)
    }
}

class JWTAuthenticationFilter(
    private val authenticationManagerx: AuthenticationManager
) : UsernamePasswordAuthenticationFilter() {

    private val log = logger {}
    private val jackson = jacksonObjectMapper()

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val user = jackson.readValue<LoginDto>(request.inputStream)
        val emptyAuthorities = ArrayList<GrantedAuthority>()
        return authenticationManagerx.authenticate(
            UsernamePasswordAuthenticationToken(
                user.username,
                user.password,
                emptyAuthorities
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
            .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    }

}

class JWTAuthorizationFilter(
    authenticationManager: AuthenticationManager
) : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response)
            return
        }
        val authentication = getAuthentication(request)
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(request, response)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        val user = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
            .build()
            .verify(token.replace("Bearer ", ""))
            .subject
        return user?.let { UsernamePasswordAuthenticationToken(it, null, emptyList()) }
    }
}
