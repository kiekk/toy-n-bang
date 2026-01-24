package com.pay.dutchpayapi.interfaces.api.gathering

import com.pay.dutchpayapi.application.gathering.request.GatheringCreateRequest
import com.pay.dutchpayapi.application.gathering.request.GatheringUpdateRequest
import com.pay.dutchpayapi.support.E2ETest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class GatheringControllerTest : E2ETest() {

    @Test
    @DisplayName("모임을 생성할 수 있다")
    fun create() {
        // given
        val request = GatheringCreateRequest(
            name = "제주도 여행",
            startDate = LocalDate.of(2025, 1, 15),
            endDate = LocalDate.of(2025, 1, 17),
            participantNames = null
        )

        // when & then
        performPost("/api/gatherings", request)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("제주도 여행"))
            .andExpect(jsonPath("$.startDate").value("2025-01-15"))
            .andExpect(jsonPath("$.endDate").value("2025-01-17"))
    }

    @Test
    @DisplayName("모임 생성 시 참여자도 함께 등록된다")
    fun createWithParticipants() {
        // given
        val request = GatheringCreateRequest(
            name = "제주도 여행",
            startDate = LocalDate.of(2025, 1, 15),
            endDate = LocalDate.of(2025, 1, 17),
            participantNames = listOf("홍길동", "김철수", "이영희")
        )

        // when & then
        performPost("/api/gatherings", request)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.participants").isArray)
            .andExpect(jsonPath("$.participants.length()").value(3))
    }

    @Test
    @DisplayName("모임 목록을 조회할 수 있다")
    fun findAll() {
        // given
        createGathering("여행1")
        createGathering("여행2")

        // when & then
        performGet("/api/gatherings")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    @DisplayName("ID로 모임을 조회할 수 있다")
    fun findById() {
        // given
        val gatheringId = createGathering("제주도 여행")

        // when & then
        performGet("/api/gatherings/$gatheringId")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(gatheringId))
            .andExpect(jsonPath("$.name").value("제주도 여행"))
    }

    @Test
    @DisplayName("존재하지 않는 모임 조회 시 404를 반환한다")
    fun findByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        performGet("/api/gatherings/$nonExistentId")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.meta.result").value("FAIL"))
            .andExpect(jsonPath("$.meta.errorCode").value("GATHERING_NOT_FOUND"))
    }

    @Test
    @DisplayName("모임 정보를 수정할 수 있다")
    fun update() {
        // given
        val gatheringId = createGathering("원래 이름")

        val updateRequest = GatheringUpdateRequest(
            name = "수정된 이름",
            startDate = LocalDate.of(2025, 2, 1),
            endDate = LocalDate.of(2025, 2, 5)
        )

        // when & then
        performPatch("/api/gatherings/$gatheringId", updateRequest)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("수정된 이름"))
            .andExpect(jsonPath("$.startDate").value("2025-02-01"))
            .andExpect(jsonPath("$.endDate").value("2025-02-05"))
    }

    @Test
    @DisplayName("모임을 삭제할 수 있다")
    fun delete() {
        // given
        val gatheringId = createGathering("삭제할 모임")

        // when & then
        performDelete("/api/gatherings/$gatheringId")
            .andExpect(status().isNoContent)

        // 삭제 확인
        performGet("/api/gatherings/$gatheringId")
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("존재하지 않는 모임 삭제 시 404를 반환한다")
    fun deleteNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        performDelete("/api/gatherings/$nonExistentId")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.meta.result").value("FAIL"))
            .andExpect(jsonPath("$.meta.errorCode").value("GATHERING_NOT_FOUND"))
    }

    private fun createGathering(name: String): Long {
        val request = GatheringCreateRequest(
            name = name,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            participantNames = null
        )

        val result = performPost("/api/gatherings", request)
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .get("id").asLong()
    }
}
