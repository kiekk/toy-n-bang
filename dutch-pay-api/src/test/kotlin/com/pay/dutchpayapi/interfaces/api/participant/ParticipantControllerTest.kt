package com.pay.dutchpayapi.interfaces.api.participant

import com.pay.dutchpayapi.application.gathering.request.GatheringCreateRequest
import com.pay.dutchpayapi.application.participant.request.ParticipantCreateRequest
import com.pay.dutchpayapi.support.E2ETest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class ParticipantControllerTest : E2ETest() {

    private var gatheringId: Long = 0L

    @BeforeEach
    fun setUp() {
        gatheringId = createGathering()
    }

    @Test
    @DisplayName("모임에 참여자를 추가할 수 있다")
    fun addToGathering() {
        // given
        val request = ParticipantCreateRequest(name = "홍길동")

        // when & then
        performPost("/api/gatherings/$gatheringId/participants", request)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("홍길동"))
    }

    @Test
    @DisplayName("존재하지 않는 모임에 참여자 추가 시 404를 반환한다")
    fun addToGatheringNotFound() {
        // given
        val nonExistentGatheringId = 999999L
        val request = ParticipantCreateRequest(name = "홍길동")

        // when & then
        performPost("/api/gatherings/$nonExistentGatheringId/participants", request)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.meta.result").value("FAIL"))
            .andExpect(jsonPath("$.meta.errorCode").value("GATHERING_NOT_FOUND"))
    }

    @Test
    @DisplayName("모임별 참여자 목록을 조회할 수 있다")
    fun findByGatheringId() {
        // given
        createParticipant(gatheringId, "홍길동")
        createParticipant(gatheringId, "김철수")
        createParticipant(gatheringId, "이영희")

        // when & then
        performGet("/api/gatherings/$gatheringId/participants")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    @DisplayName("참여자를 삭제할 수 있다")
    fun delete() {
        // given
        val participantId = createParticipant(gatheringId, "홍길동")

        // when & then
        performDelete("/api/participants/$participantId")
            .andExpect(status().isNoContent)

        // 삭제 확인
        performGet("/api/gatherings/$gatheringId/participants")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @DisplayName("존재하지 않는 참여자 삭제 시 404를 반환한다")
    fun deleteNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        performDelete("/api/participants/$nonExistentId")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.meta.result").value("FAIL"))
            .andExpect(jsonPath("$.meta.errorCode").value("PARTICIPANT_NOT_FOUND"))
    }

    private fun createGathering(): Long {
        val request = GatheringCreateRequest(
            name = "테스트 모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            participantNames = null
        )

        val result = performPost("/api/gatherings", request)
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .get("id").asLong()
    }

    private fun createParticipant(gatheringId: Long, name: String): Long {
        val request = ParticipantCreateRequest(name = name)

        val result = performPost("/api/gatherings/$gatheringId/participants", request)
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .get("id").asLong()
    }
}
