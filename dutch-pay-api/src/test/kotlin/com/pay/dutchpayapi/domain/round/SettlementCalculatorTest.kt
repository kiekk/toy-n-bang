package com.pay.dutchpayapi.domain.round

import com.pay.dutchpayapi.domain.participant.Participant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SettlementCalculatorTest {

    private lateinit var calculator: SettlementCalculator
    private lateinit var participants: List<Participant>

    @BeforeEach
    fun setUp() {
        calculator = SettlementCalculator()
        participants = listOf(
            Participant(id = 1L, name = "홍길동", gatheringId = 1L),
            Participant(id = 2L, name = "김철수", gatheringId = 1L),
            Participant(id = 3L, name = "이영희", gatheringId = 1L)
        )
    }

    @Test
    @DisplayName("기본 정산을 계산할 수 있다")
    fun calculateBasic() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차", amount = BigDecimal("30000"), payerId = 1L, gatheringId = 1L)
        )
        val exclusionsByRound = emptyMap<Long, Set<Long>>()

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(result.balances).hasSize(3)

        val hong = result.balances.first { it.name == "홍길동" }
        assertThat(hong.totalPaid).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(hong.totalOwed).isEqualByComparingTo(BigDecimal("10000"))
        assertThat(hong.netBalance).isEqualByComparingTo(BigDecimal("20000"))

        val kim = result.balances.first { it.name == "김철수" }
        assertThat(kim.totalPaid).isEqualByComparingTo(BigDecimal("0"))
        assertThat(kim.totalOwed).isEqualByComparingTo(BigDecimal("10000"))
        assertThat(kim.netBalance).isEqualByComparingTo(BigDecimal("-10000"))
    }

    @Test
    @DisplayName("제외 인원을 포함하여 정산할 수 있다")
    fun calculateWithExclusion() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차 술집", amount = BigDecimal("20000"), payerId = 1L, gatheringId = 1L)
        )
        val exclusionsByRound = mapOf(1L to setOf(3L)) // 이영희 제외

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        val hong = result.balances.first { it.name == "홍길동" }
        assertThat(hong.totalOwed).isEqualByComparingTo(BigDecimal("10000"))

        val kim = result.balances.first { it.name == "김철수" }
        assertThat(kim.totalOwed).isEqualByComparingTo(BigDecimal("10000"))

        val lee = result.balances.first { it.name == "이영희" }
        assertThat(lee.totalOwed).isEqualByComparingTo(BigDecimal("0"))
    }

    @Test
    @DisplayName("여러 라운드의 정산을 계산할 수 있다")
    fun calculateMultipleRounds() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차", amount = BigDecimal("60000"), payerId = 1L, gatheringId = 1L),
            SettlementRound(id = 2L, title = "2차", amount = BigDecimal("30000"), payerId = 2L, gatheringId = 1L),
            SettlementRound(id = 3L, title = "3차", amount = BigDecimal("15000"), payerId = 3L, gatheringId = 1L)
        )
        val exclusionsByRound = mapOf(3L to setOf(1L)) // 3차에서 홍길동 제외

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal("105000"))

        val hong = result.balances.first { it.name == "홍길동" }
        assertThat(hong.totalPaid).isEqualByComparingTo(BigDecimal("60000"))
        assertThat(hong.totalOwed).isEqualByComparingTo(BigDecimal("30000")) // 1차 20000 + 2차 10000
        assertThat(hong.netBalance).isEqualByComparingTo(BigDecimal("30000"))

        val kim = result.balances.first { it.name == "김철수" }
        assertThat(kim.totalPaid).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(kim.totalOwed).isEqualByComparingTo(BigDecimal("37500")) // 1차 20000 + 2차 10000 + 3차 7500
    }

    @Test
    @DisplayName("지출이 없으면 빈 정산 결과를 반환한다")
    fun calculateNoRounds() {
        // given
        val rounds = emptyList<SettlementRound>()
        val exclusionsByRound = emptyMap<Long, Set<Long>>()

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

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

    @Test
    @DisplayName("참여자별 잔액을 정확히 계산한다")
    fun calculateBalancesAccurately() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차", amount = BigDecimal("45000"), payerId = 1L, gatheringId = 1L)
        )
        val exclusionsByRound = emptyMap<Long, Set<Long>>()

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        val hong = result.balances.first { it.name == "홍길동" }
        assertThat(hong.totalPaid).isEqualByComparingTo(BigDecimal("45000"))
        assertThat(hong.totalOwed).isEqualByComparingTo(BigDecimal("15000"))
        assertThat(hong.netBalance).isEqualByComparingTo(BigDecimal("30000"))
    }

    @Test
    @DisplayName("채무 관계를 최적화하여 생성한다")
    fun resolveDebtsOptimally() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차", amount = BigDecimal("30000"), payerId = 1L, gatheringId = 1L)
        )
        val exclusionsByRound = emptyMap<Long, Set<Long>>()

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        assertThat(result.debts).hasSize(2)

        val totalDebtAmount = result.debts.sumOf { it.amount }
        assertThat(totalDebtAmount).isEqualByComparingTo(BigDecimal("20000"))

        result.debts.forEach { debt ->
            assertThat(debt.to).isEqualTo("홍길동")
        }
    }

    @Test
    @DisplayName("소수점 금액을 올바르게 처리한다")
    fun handleDecimalAmounts() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차", amount = BigDecimal("10000"), payerId = 1L, gatheringId = 1L)
        )
        val exclusionsByRound = emptyMap<Long, Set<Long>>()

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        result.balances.forEach { balance ->
            assertThat(balance.totalOwed).isEqualByComparingTo(BigDecimal("3333.33"))
        }
    }

    @Test
    @DisplayName("총액을 정확히 계산한다")
    fun calculateTotalAmount() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차", amount = BigDecimal("30000"), payerId = 1L, gatheringId = 1L),
            SettlementRound(id = 2L, title = "2차", amount = BigDecimal("20000"), payerId = 2L, gatheringId = 1L),
            SettlementRound(id = 3L, title = "3차", amount = BigDecimal("15000"), payerId = 3L, gatheringId = 1L)
        )
        val exclusionsByRound = emptyMap<Long, Set<Long>>()

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal("65000"))
    }

    @Test
    @DisplayName("모든 참여자가 제외된 라운드를 처리한다")
    fun handleAllParticipantsExcluded() {
        // given
        val rounds = listOf(
            SettlementRound(id = 1L, title = "1차", amount = BigDecimal("30000"), payerId = 1L, gatheringId = 1L)
        )
        val exclusionsByRound = mapOf(1L to setOf(1L, 2L, 3L)) // 모든 참여자 제외

        // when
        val result = calculator.calculate(participants, rounds, exclusionsByRound)

        // then
        assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal("30000"))

        result.balances.forEach { balance ->
            assertThat(balance.totalOwed).isEqualByComparingTo(BigDecimal.ZERO)
        }

        // 홍길동만 지불했지만 아무도 빚지지 않음
        val hong = result.balances.first { it.name == "홍길동" }
        assertThat(hong.totalPaid).isEqualByComparingTo(BigDecimal("30000"))
        assertThat(hong.netBalance).isEqualByComparingTo(BigDecimal("30000"))
    }
}
