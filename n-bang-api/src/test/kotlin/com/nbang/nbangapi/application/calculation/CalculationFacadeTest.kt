package com.nbang.nbangapi.application.calculation

import com.nbang.nbangapi.application.calculation.response.UserBalanceResponse
import com.nbang.nbangapi.application.gathering.GatheringFacade
import com.nbang.nbangapi.application.gathering.request.GatheringCreateRequest
import com.nbang.nbangapi.application.participant.response.ParticipantResponse
import com.nbang.nbangapi.application.round.SettlementRoundFacade
import com.nbang.nbangapi.application.round.request.ExclusionRequest
import com.nbang.nbangapi.application.round.request.RoundCreateRequest
import com.nbang.nbangapi.support.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate

class CalculationFacadeTest @Autowired constructor(
    private val calculationFacade: CalculationFacade,
    private val gatheringFacade: GatheringFacade,
    private val roundFacade: SettlementRoundFacade
) : IntegrationTest() {

    private var gatheringId: Long = 0L
    private var participant1Id: Long = 0L // 홍길동
    private var participant2Id: Long = 0L // 김철수
    private var participant3Id: Long = 0L // 이영희

    @BeforeEach
    fun setUp() {
        val gathering = gatheringFacade.create(
            GatheringCreateRequest(
                name = "제주도 여행",
                startDate = LocalDate.of(2025, 1, 15),
                endDate = LocalDate.of(2025, 1, 17),
                participantNames = listOf("홍길동", "김철수", "이영희")
            )
        )

        gatheringId = gathering.id

        val participants = gathering.participants
        participant1Id = findParticipantIdByName(participants, "홍길동")
        participant2Id = findParticipantIdByName(participants, "김철수")
        participant3Id = findParticipantIdByName(participants, "이영희")
    }

    private fun findParticipantIdByName(participants: List<ParticipantResponse>, name: String): Long {
        return participants.first { it.name == name }.id
    }

    @Test
    @DisplayName("정산 결과를 계산할 수 있다 - 기본 케이스")
    fun calculateBasic() {
        // given
        roundFacade.create(
            gatheringId,
            RoundCreateRequest(
                title = "1차 고기집",
                amount = BigDecimal("30000"),
                payerId = participant1Id,
                exclusions = null
            )
        )

        // when
        val result = calculationFacade.calculate(gatheringId)

        // then
        assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(result.balances).hasSize(3)

        val hong = findBalance(result.balances, "홍길동")
        assertThat(hong.totalPaid).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(hong.totalOwed).isEqualByComparingTo(BigDecimal("10000"))
        assertThat(hong.netBalance).isEqualByComparingTo(BigDecimal("20000"))

        val kim = findBalance(result.balances, "김철수")
        assertThat(kim.totalPaid).isEqualByComparingTo(BigDecimal("0"))
        assertThat(kim.totalOwed).isEqualByComparingTo(BigDecimal("10000"))
        assertThat(kim.netBalance).isEqualByComparingTo(BigDecimal("-10000"))

        val lee = findBalance(result.balances, "이영희")
        assertThat(lee.totalPaid).isEqualByComparingTo(BigDecimal("0"))
        assertThat(lee.totalOwed).isEqualByComparingTo(BigDecimal("10000"))
        assertThat(lee.netBalance).isEqualByComparingTo(BigDecimal("-10000"))

        assertThat(result.debts).hasSize(2)
    }

    @Test
    @DisplayName("정산 결과를 계산할 수 있다 - 제외 인원 포함")
    fun calculateWithExclusion() {
        // given
        roundFacade.create(
            gatheringId,
            RoundCreateRequest(
                title = "1차 술집",
                amount = BigDecimal("20000"),
                payerId = participant1Id,
                exclusions = listOf(ExclusionRequest(participant3Id, "술 안마심"))
            )
        )

        // when
        val result = calculationFacade.calculate(gatheringId)

        // then
        val hong = findBalance(result.balances, "홍길동")
        assertThat(hong.totalPaid).isEqualByComparingTo(BigDecimal("20000"))
        assertThat(hong.totalOwed).isEqualByComparingTo(BigDecimal("10000"))
        assertThat(hong.netBalance).isEqualByComparingTo(BigDecimal("10000"))

        val kim = findBalance(result.balances, "김철수")
        assertThat(kim.totalPaid).isEqualByComparingTo(BigDecimal("0"))
        assertThat(kim.totalOwed).isEqualByComparingTo(BigDecimal("10000"))

        val lee = findBalance(result.balances, "이영희")
        assertThat(lee.totalOwed).isEqualByComparingTo(BigDecimal("0"))
    }

    @Test
    @DisplayName("정산 결과를 계산할 수 있다 - 복잡한 케이스")
    fun calculateComplex() {
        // given
        roundFacade.create(
            gatheringId,
            RoundCreateRequest(
                title = "1차 고기집",
                amount = BigDecimal("60000"),
                payerId = participant1Id,
                exclusions = null
            )
        )

        roundFacade.create(
            gatheringId,
            RoundCreateRequest(
                title = "2차 카페",
                amount = BigDecimal("30000"),
                payerId = participant2Id,
                exclusions = null
            )
        )

        roundFacade.create(
            gatheringId,
            RoundCreateRequest(
                title = "3차 술집",
                amount = BigDecimal("15000"),
                payerId = participant3Id,
                exclusions = listOf(ExclusionRequest(participant1Id, "먼저 귀가"))
            )
        )

        // when
        val result = calculationFacade.calculate(gatheringId)

        // then
        assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal("105000"))

        val hong = findBalance(result.balances, "홍길동")
        assertThat(hong.totalPaid).isEqualByComparingTo(BigDecimal("60000"))
        assertThat(hong.totalOwed).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(hong.netBalance).isEqualByComparingTo(BigDecimal("30000"))

        val kim = findBalance(result.balances, "김철수")
        assertThat(kim.totalPaid).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(kim.totalOwed).isEqualByComparingTo(BigDecimal("37500"))
        assertThat(kim.netBalance).isEqualByComparingTo(BigDecimal("-7500"))

        val lee = findBalance(result.balances, "이영희")
        assertThat(lee.totalPaid).isEqualByComparingTo(BigDecimal("15000"))
        assertThat(lee.totalOwed).isEqualByComparingTo(BigDecimal("37500"))
        assertThat(lee.netBalance).isEqualByComparingTo(BigDecimal("-22500"))

        assertThat(result.debts).isNotEmpty()

        val totalDebt = result.debts.sumOf { it.amount }
        assertThat(totalDebt).isEqualByComparingTo(BigDecimal("30000"))
    }

    @Test
    @DisplayName("지출이 없는 경우 빈 정산 결과를 반환한다")
    fun calculateNoRounds() {
        // when
        val result = calculationFacade.calculate(gatheringId)

        // then
        assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(result.balances).hasSize(3)
        assertThat(result.debts).isEmpty()

        result.balances.forEach { balance ->
            assertThat(balance.totalPaid).isEqualByComparingTo(BigDecimal.ZERO)
            assertThat(balance.totalOwed).isEqualByComparingTo(BigDecimal.ZERO)
            assertThat(balance.netBalance).isEqualByComparingTo(BigDecimal.ZERO)
        }
    }

    private fun findBalance(balances: List<UserBalanceResponse>, name: String): UserBalanceResponse {
        return balances.first { it.name == name }
    }
}
