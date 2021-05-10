package com.github.cpickl.bookstore.boundary

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.PermitAll
import javax.annotation.security.RolesAllowed

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(SecurityTestConfig::class)
@ActiveProfiles("test")
class SecurityApiTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `When get open endpoint without authentication Then return success`() {
        mockMvc.perform(get("/test/permitAll"))
            .andExpect(status().isOk)
    }

    @Test
    fun `When get secured for user without authentication Then return error`() {
        mockMvc.perform(get("/test/secured_user"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code", `is`("FORBIDDEN")))
            .andExpect(jsonPath("$.status", `is`(403)))
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `When get secured for user and request as user Then return successful and current username`() {
        mockMvc.perform(get("/test/secured_user"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", `is`("testUser")))
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `When get secured for admin and request as user Then return error`() {
        mockMvc.perform(get("/test/secured_admin"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code", `is`("FORBIDDEN")))
            .andExpect(jsonPath("$.status", `is`(403)))
    }
}

@TestConfiguration
class SecurityTestConfig {

    @RestController
    @RequestMapping(
        "/test",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    class TestController {

        @GetMapping("/permitAll")
        @PermitAll
        fun allPermitted() = SecurityDto()

        @GetMapping("/secured_user")
        @RolesAllowed("ROLE_USER")
        fun securedForUser() = SecurityDto(SecurityContextHolder.getContext().authentication.name)

        @GetMapping("/secured_admin")
        @RolesAllowed("ROLE_ADMIN")
        fun securedForAdmin() = SecurityDto(SecurityContextHolder.getContext().authentication.name)
    }
}

data class SecurityDto(
    val message: String = "success"
)
