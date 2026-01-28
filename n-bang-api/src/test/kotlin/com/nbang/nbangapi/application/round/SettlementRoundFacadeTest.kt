package com.nbang.nbangapi.application.round

import com.nbang.nbangapi.application.gathering.GatheringFacade
import com.nbang.nbangapi.application.gathering.request.GatheringCreateRequest
import com.nbang.nbangapi.application.participant.response.ParticipantResponse
import com.nbang.nbangapi.application.round.request.ExclusionRequest
import com.nbang.nbangapi.application.round.request.RoundCreateRequest
import com.nbang.nbangapi.application.round.request.RoundUpdateRequest
import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate

class SettlementRoundFacadeTest @Autowired constructor(
    private val roundFacade: SettlementRoundFacade,
    private val gatheringFacade: GatheringFacade
) : IntegrationTest() {

    private val testMemberId = 1L
    private var gatheringId: Long = 0L
    private var participant1Id: Long = 0L
    private var participant2Id: Long = 0L

    @BeforeEach
    fun setUp() {
        val gathering = gatheringFacade.create(
            testMemberId,
            GatheringCreateRequest(
                name = "테스트 모임",
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                participantNames = listOf("홍길동", "김철수")
            )
        )
        gatheringId = gathering.id
        participant1Id = findParticipantIdByName(gathering.participants, "홍길동")
        participant2Id = findParticipantIdByName(gathering.participants, "김철수")
    }

    private fun findParticipantIdByName(participants: List<ParticipantResponse>, name: String): Long {
        return participants.first { it.name == name }.id
    }

    @Test
    @DisplayName("정산 라운드를 생성할 수 있다")
    fun create() {
        // given
        val request = RoundCreateRequest(
            title = "1차 고기집",
            amount = BigDecimal("30000"),
            payerId = participant1Id,
            exclusions = null
        )

        // when
        val response = roundFacade.create(gatheringId, request)

        // then
        assertThat(response.id).isNotNull()
        assertThat(response.title).isEqualTo("1차 고기집")
        assertThat(response.amount).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(response.payerId).isEqualTo(participant1Id)
        assertThat(response.payerName).isEqualTo("홍길동")
    }

    @Test
    @DisplayName("제외 인원과 함께 라운드를 생성할 수 있다")
    fun createWithExclusions() {
        // given
        val request = RoundCreateRequest(
            title = "1차 술집",
            amount = BigDecimal("20000"),
            payerId = participant1Id,
            exclusions = listOf(ExclusionRequest(participant2Id, "술 안마심"))
        )

        // when
        val response = roundFacade.create(gatheringId, request)

        // then
        assertThat(response.exclusions).hasSize(1)
        assertThat(response.exclusions[0].participantId).isEqualTo(participant2Id)
        assertThat(response.exclusions[0].participantName).isEqualTo("김철수")
        assertThat(response.exclusions[0].reason).isEqualTo("술 안마심")
    }

    @Test
    @DisplayName("모임별 라운드 목록을 조회할 수 있다")
    fun findByGatheringId() {
        // given
        roundFacade.create(gatheringId, RoundCreateRequest("1차", BigDecimal("30000"), participant1Id, null))
        roundFacade.create(gatheringId, RoundCreateRequest("2차", BigDecimal("20000"), participant2Id, null))

        // when
        val rounds = roundFacade.findByGatheringId(gatheringId)

        // then
        assertThat(rounds).hasSize(2)
    }

    @Test
    @DisplayName("ID로 라운드를 조회할 수 있다")
    fun findById() {
        // given
        val created = roundFacade.create(
            gatheringId,
            RoundCreateRequest("1차 고기집", BigDecimal("30000"), participant1Id, null)
        )

        // when
        val response = roundFacade.findById(created.id)

        // then
        assertThat(response.id).isEqualTo(created.id)
        assertThat(response.title).isEqualTo("1차 고기집")
    }

    @Test
    @DisplayName("라운드를 수정할 수 있다")
    fun update() {
        // given
        val created = roundFacade.create(
            gatheringId,
            RoundCreateRequest("원래 제목", BigDecimal("10000"), participant1Id, null)
        )

        val updateRequest = RoundUpdateRequest(
            title = "수정된 제목",
            amount = BigDecimal("50000"),
            payerId = participant2Id,
            exclusions = listOf(ExclusionRequest(participant1Id, "먼저 귀가"))
        )

        // when
        val updated = roundFacade.update(created.id, updateRequest)

        // then
        assertThat(updated.title).isEqualTo("수정된 제목")
        assertThat(updated.amount).isEqualByComparingTo(BigDecimal("50000"))
        assertThat(updated.payerId).isEqualTo(participant2Id)
        assertThat(updated.exclusions).hasSize(1)
        assertThat(updated.exclusions[0].reason).isEqualTo("먼저 귀가")
    }

    @Test
    @DisplayName("라운드를 삭제할 수 있다")
    fun delete() {
        // given
        val created = roundFacade.create(
            gatheringId,
            RoundCreateRequest("삭제할 라운드", BigDecimal("10000"), participant1Id, null)
        )

        // when
        roundFacade.delete(created.id)

        // then
        assertThatThrownBy { roundFacade.findById(created.id) }
            .isInstanceOf(CoreException::class.java)
    }

    @Test
    @DisplayName("영수증 이미지 URL을 업데이트할 수 있다")
    fun updateReceiptImage() {
        // given
        val created = roundFacade.create(
            gatheringId,
            RoundCreateRequest("1차", BigDecimal("30000"), participant1Id, null)
        )

        // when
        val updated = roundFacade.updateReceiptImage(created.id, "/uploads/receipt.jpg")

        // then
        assertThat(updated.receiptImageUrl).isEqualTo("/uploads/receipt.jpg")
    }

    @Test
    @DisplayName("존재하지 않는 라운드 조회 시 예외가 발생한다")
    fun findByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { roundFacade.findById(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
    }
}
