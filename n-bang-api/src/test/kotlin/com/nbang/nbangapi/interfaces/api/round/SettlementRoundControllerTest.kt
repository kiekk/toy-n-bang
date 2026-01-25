package com.nbang.nbangapi.interfaces.api.round

import com.nbang.nbangapi.application.gathering.request.GatheringCreateRequest
import com.nbang.nbangapi.application.round.request.ExclusionRequest
import com.nbang.nbangapi.application.round.request.RoundCreateRequest
import com.nbang.nbangapi.application.round.request.RoundUpdateRequest
import com.nbang.nbangapi.support.E2ETest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate

class SettlementRoundControllerTest : E2ETest() {

    private var gatheringId: Long = 0L
    private var payerId: Long = 0L
    private var participant2Id: Long = 0L

    @BeforeEach
    fun setUp() {
        val gatheringResult = createGatheringWithParticipants()
        gatheringId = gatheringResult.first
        payerId = gatheringResult.second[0]
        participant2Id = gatheringResult.second[1]
    }

    @Test
    @DisplayName("정산 라운드를 생성할 수 있다")
    fun create() {
        // given
        val request = RoundCreateRequest(
            title = "1차 고기집",
            amount = BigDecimal("30000"),
            payerId = payerId,
            exclusions = null
        )

        // when & then
        performPost("/api/gatherings/$gatheringId/rounds", request)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("1차 고기집"))
            .andExpect(jsonPath("$.amount").value(30000))
            .andExpect(jsonPath("$.payerId").value(payerId))
    }

    @Test
    @DisplayName("라운드 생성 시 제외 인원을 설정할 수 있다")
    fun createWithExclusions() {
        // given
        val request = RoundCreateRequest(
            title = "1차 술집",
            amount = BigDecimal("20000"),
            payerId = payerId,
            exclusions = listOf(ExclusionRequest(participant2Id, "술 안마심"))
        )

        // when & then
        performPost("/api/gatherings/$gatheringId/rounds", request)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.exclusions").isArray)
            .andExpect(jsonPath("$.exclusions.length()").value(1))
            .andExpect(jsonPath("$.exclusions[0].participantId").value(participant2Id))
            .andExpect(jsonPath("$.exclusions[0].reason").value("술 안마심"))
    }

    @Test
    @DisplayName("모임별 라운드 목록을 조회할 수 있다")
    fun findByGatheringId() {
        // given
        createRound(gatheringId, "1차", BigDecimal("30000"))
        createRound(gatheringId, "2차", BigDecimal("20000"))

        // when & then
        performGet("/api/gatherings/$gatheringId/rounds")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    @DisplayName("ID로 라운드를 조회할 수 있다")
    fun findById() {
        // given
        val roundId = createRound(gatheringId, "1차 고기집", BigDecimal("30000"))

        // when & then
        performGet("/api/rounds/$roundId")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(roundId))
            .andExpect(jsonPath("$.title").value("1차 고기집"))
    }

    @Test
    @DisplayName("존재하지 않는 라운드 조회 시 404를 반환한다")
    fun findByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        performGet("/api/rounds/$nonExistentId")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.meta.result").value("FAIL"))
            .andExpect(jsonPath("$.meta.errorCode").value("ROUND_NOT_FOUND"))
    }

    @Test
    @DisplayName("라운드 정보를 수정할 수 있다")
    fun update() {
        // given
        val roundId = createRound(gatheringId, "원래 제목", BigDecimal("10000"))

        val updateRequest = RoundUpdateRequest(
            title = "수정된 제목",
            amount = BigDecimal("50000"),
            payerId = participant2Id,
            exclusions = null
        )

        // when & then
        performPatch("/api/rounds/$roundId", updateRequest)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("수정된 제목"))
            .andExpect(jsonPath("$.amount").value(50000))
            .andExpect(jsonPath("$.payerId").value(participant2Id))
    }

    @Test
    @DisplayName("라운드를 삭제할 수 있다")
    fun delete() {
        // given
        val roundId = createRound(gatheringId, "삭제할 라운드", BigDecimal("10000"))

        // when & then
        performDelete("/api/rounds/$roundId")
            .andExpect(status().isNoContent)

        // 삭제 확인
        performGet("/api/rounds/$roundId")
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("영수증 이미지를 업로드할 수 있다")
    fun uploadImage() {
        // given
        val roundId = createRound(gatheringId, "1차", BigDecimal("30000"))

        val file = MockMultipartFile(
            "file",
            "receipt.jpg",
            "image/jpeg",
            "test image content".toByteArray()
        )

        // when & then
        mockMvc.perform(
            multipart("/api/rounds/$roundId/image")
                .file(file)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.receiptImageUrl").value("/uploads/receipt.jpg"))
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

    private fun createRound(gatheringId: Long, title: String, amount: BigDecimal): Long {
        val request = RoundCreateRequest(
            title = title,
            amount = amount,
            payerId = payerId,
            exclusions = null
        )

        val result = performPost("/api/gatherings/$gatheringId/rounds", request)
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString)
            .get("id").asLong()
    }
}
