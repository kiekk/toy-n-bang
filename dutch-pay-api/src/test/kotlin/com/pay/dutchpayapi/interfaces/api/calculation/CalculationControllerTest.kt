package com.pay.dutchpayapi.interfaces.api.calculation

import com.pay.dutchpayapi.application.gathering.request.GatheringCreateRequest
import com.pay.dutchpayapi.application.round.request.ExclusionRequest
import com.pay.dutchpayapi.application.round.request.RoundCreateRequest
import com.pay.dutchpayapi.support.E2ETest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate

class CalculationControllerTest : E2ETest() {

    private var gatheringId: Long = 0L
    private var participant1Id: Long = 0L // 홍길동
    private var participant2Id: Long = 0L // 김철수
    private var participant3Id: Long = 0L // 이영희

    @BeforeEach
    fun setUp() {
        val gatheringResult = createGatheringWithParticipants()
        gatheringId = gatheringResult.first
        participant1Id = gatheringResult.second[0]
        participant2Id = gatheringResult.second[1]
        participant3Id = gatheringResult.second[2]
    }

    @Test
    @DisplayName("정산 결과를 계산할 수 있다")
    fun calculate() {
        // given
        createRound(gatheringId, "1차 고기집", BigDecimal("30000"), participant1Id, null)

        // when & then
        performGet("/api/gatherings/$gatheringId/calculate")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.gatheringId").value(gatheringId))
            .andExpect(jsonPath("$.totalAmount").value(30000))
            .andExpect(jsonPath("$.balances").isArray)
            .andExpect(jsonPath("$.balances.length()").value(3))
            .andExpect(jsonPath("$.debts").isArray)
    }

    @Test
    @DisplayName("제외 인원 포함 정산 결과를 계산할 수 있다")
    fun calculateWithExclusion() {
        // given
        createRound(
            gatheringId,
            "1차 술집",
            BigDecimal("20000"),
            participant1Id,
            listOf(ExclusionRequest(participant3Id, "술 안마심"))
        )

        // when & then
        performGet("/api/gatherings/$gatheringId/calculate")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalAmount").value(20000))
            .andExpect(jsonPath("$.balances.length()").value(3))
    }

    @Test
    @DisplayName("존재하지 않는 모임 정산 시 404를 반환한다")
    fun calculateNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        performGet("/api/gatherings/$nonExistentId/calculate")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.meta.result").value("FAIL"))
            .andExpect(jsonPath("$.meta.errorCode").value("GATHERING_NOT_FOUND"))
    }

    private fun createGatheringWithParticipants(): Pair<Long, List<Long>> {
        val request = GatheringCreateRequest(
            name = "테스트 모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            participantNames = listOf("홍길동", "김철수", "이영희")
        )

        val result = performPost("/api/gatherings", request)
            .andReturn()

        val json = objectMapper.readTree(result.response.contentAsString)
        val gatheringId = json.get("id").asLong()
        val participantIds = json.get("participants").map { it.get("id").asLong() }

        return Pair(gatheringId, participantIds)
    }

    private fun createRound(
        gatheringId: Long,
        title: String,
        amount: BigDecimal,
        payerId: Long,
        exclusions: List<ExclusionRequest>?
    ): Long {
        val request = RoundCreateRequest(
            title = title,
            amount = amount,
            payerId = payerId,
            exclusions = exclusions
        )

        val result = performPost("/api/gatherings/$gatheringId/rounds", request)
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .get("id").asLong()
    }
}
