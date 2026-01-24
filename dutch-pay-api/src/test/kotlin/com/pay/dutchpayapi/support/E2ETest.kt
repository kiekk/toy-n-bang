package com.pay.dutchpayapi.support

import tools.jackson.databind.ObjectMapper
import com.pay.dutchpayapi.support.utils.DatabaseCleanUp
import org.junit.jupiter.api.AfterEach
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
@Import(TestcontainersConfiguration::class, JacksonAutoConfiguration::class)
abstract class E2ETest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var databaseCleanUp: DatabaseCleanUp

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    protected fun performGet(url: String): ResultActions {
        return mockMvc.perform(
            get(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
    }

    protected fun performPost(url: String, body: Any): ResultActions {
        return mockMvc.perform(
            post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andDo(print())
    }

    protected fun performPatch(url: String, body: Any): ResultActions {
        return mockMvc.perform(
            patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andDo(print())
    }

    protected fun performDelete(url: String): ResultActions {
        return mockMvc.perform(
            delete(url)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())
    }

    protected fun <T> toObject(json: String, clazz: Class<T>): T {
        return objectMapper.readValue(json, clazz)
    }
}
