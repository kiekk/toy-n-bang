package com.nbang.nbangapi.domain.round

import com.nbang.nbangapi.domain.gathering.GatheringService
import com.nbang.nbangapi.domain.participant.ParticipantService
import com.nbang.nbangapi.support.IntegrationTest
import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate

class SettlementRoundServiceTest @Autowired constructor(
    private val roundService: SettlementRoundService,
    private val gatheringService: GatheringService,
    private val participantService: ParticipantService
) : IntegrationTest() {

    private val testMemberId = 1L
    private var gatheringId: Long = 0L
    private var payerId: Long = 0L

    @BeforeEach
    fun setUp() {
        val gathering = gatheringService.create(
            memberId = testMemberId,
            name = "테스트 모임",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )
        gatheringId = gathering.id!!

        val participant = participantService.create("홍길동", gatheringId)
        payerId = participant.id!!
    }

    @Test
    @DisplayName("정산 라운드를 생성할 수 있다")
    fun create() {
        // given
        val title = "1차 고기집"
        val amount = BigDecimal("30000")

        // when
        val round = roundService.create(title, amount, payerId, gatheringId)

        // then
        assertThat(round.id).isNotNull()
        assertThat(round.title).isEqualTo(title)
        assertThat(round.amount).isEqualByComparingTo(amount)
        assertThat(round.payerId).isEqualTo(payerId)
        assertThat(round.gatheringId).isEqualTo(gatheringId)
    }

    @Test
    @DisplayName("ID로 라운드를 조회할 수 있다")
    fun findById() {
        // given
        val created = roundService.create("1차", BigDecimal("30000"), payerId, gatheringId)

        // when
        val found = roundService.findById(created.id!!)

        // then
        assertThat(found.id).isEqualTo(created.id)
        assertThat(found.title).isEqualTo(created.title)
    }

    @Test
    @DisplayName("존재하지 않는 라운드 조회 시 예외가 발생한다")
    fun findByIdNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { roundService.findById(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.ROUND_NOT_FOUND)
    }

    @Test
    @DisplayName("모임별 라운드 목록을 조회할 수 있다")
    fun findByGatheringId() {
        // given
        roundService.create("1차", BigDecimal("30000"), payerId, gatheringId)
        roundService.create("2차", BigDecimal("20000"), payerId, gatheringId)
        roundService.create("3차", BigDecimal("10000"), payerId, gatheringId)

        // when
        val rounds = roundService.findByGatheringId(gatheringId)

        // then
        assertThat(rounds).hasSize(3)
        assertThat(rounds.map { it.title }).containsExactlyInAnyOrder("1차", "2차", "3차")
    }

    @Test
    @DisplayName("라운드 정보를 수정할 수 있다")
    fun update() {
        // given
        val created = roundService.create("원래 제목", BigDecimal("10000"), payerId, gatheringId)

        val newPayer = participantService.create("김철수", gatheringId)
        val newTitle = "수정된 제목"
        val newAmount = BigDecimal("50000")

        // when
        val updated = roundService.update(created.id!!, newTitle, newAmount, newPayer.id!!)

        // then
        assertThat(updated.title).isEqualTo(newTitle)
        assertThat(updated.amount).isEqualByComparingTo(newAmount)
        assertThat(updated.payerId).isEqualTo(newPayer.id)
    }

    @Test
    @DisplayName("존재하지 않는 라운드 수정 시 예외가 발생한다")
    fun updateNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy {
            roundService.update(nonExistentId, "제목", BigDecimal("10000"), payerId)
        }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.ROUND_NOT_FOUND)
    }

    @Test
    @DisplayName("영수증 이미지를 업데이트할 수 있다")
    fun updateReceiptImage() {
        // given
        val created = roundService.create("1차", BigDecimal("30000"), payerId, gatheringId)
        val imageUrl = "/uploads/receipt.jpg"

        // when
        val updated = roundService.updateReceiptImage(created.id!!, imageUrl)

        // then
        assertThat(updated.receiptImageUrl).isEqualTo(imageUrl)
    }

    @Test
    @DisplayName("라운드를 삭제할 수 있다")
    fun delete() {
        // given
        val created = roundService.create("삭제할 라운드", BigDecimal("10000"), payerId, gatheringId)

        // when
        roundService.delete(created.id!!)

        // then
        assertThatThrownBy { roundService.findById(created.id!!) }
            .isInstanceOf(CoreException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 라운드 삭제 시 예외가 발생한다")
    fun deleteNotFound() {
        // given
        val nonExistentId = 999999L

        // when & then
        assertThatThrownBy { roundService.delete(nonExistentId) }
            .isInstanceOf(CoreException::class.java)
            .extracting("errorType")
            .isEqualTo(ErrorType.ROUND_NOT_FOUND)
    }

    @Test
    @DisplayName("라운드 존재 여부를 확인할 수 있다")
    fun existsById() {
        // given
        val created = roundService.create("1차", BigDecimal("30000"), payerId, gatheringId)

        // when & then
        assertThat(roundService.existsById(created.id!!)).isTrue()
        assertThat(roundService.existsById(999999L)).isFalse()
    }
}
