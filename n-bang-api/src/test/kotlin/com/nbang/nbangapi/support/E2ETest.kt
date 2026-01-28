package com.nbang.nbangapi.support

import tools.jackson.databind.ObjectMapper
import com.nbang.nbangapi.support.security.JwtTokenProvider
import com.nbang.nbangapi.support.utils.DatabaseCleanUp
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class, JacksonAutoConfiguration::class, TestSecurityConfig::class)
abstract class E2ETest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var databaseCleanUp: DatabaseCleanUp

    @Autowired
    private lateinit var testMemberSetup: TestMemberSetup

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    protected var testMemberId: Long = 0L
    protected lateinit var accessToken: String

    @BeforeEach
    fun setUpAuth() {
        val member = testMemberSetup.createTestMember()
        testMemberId = member.id!!
        accessToken = jwtTokenProvider.createAccessToken(testMemberId)
    }

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    protected fun performGet(url: String): ResultActions {
        return mockMvc.perform(
            get(url)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
    }

    protected fun performPost(url: String, body: Any): ResultActions {
        return mockMvc.perform(
            post(url)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andDo(print())
    }

    protected fun performPatch(url: String, body: Any): ResultActions {
        return mockMvc.perform(
            patch(url)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andDo(print())
    }

    protected fun performDelete(url: String): ResultActions {
        return mockMvc.perform(
            delete(url)
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
    }

    protected fun <T> toObject(json: String, clazz: Class<T>): T {
        return objectMapper.readValue(json, clazz)
    }
}
