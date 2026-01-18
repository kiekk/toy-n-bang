package com.pay.dutchpayapi.application.calculation

import com.pay.dutchpayapi.domain.gathering.GatheringRepository
import com.pay.dutchpayapi.domain.participant.Participant
import com.pay.dutchpayapi.domain.round.SettlementRound
import com.pay.dutchpayapi.support.error.BusinessException
import com.pay.dutchpayapi.support.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
@Transactional(readOnly = true)
class CalculationService(
    private val gatheringRepository: GatheringRepository
) {

    companion object {
        private val EPSILON = BigDecimal("0.01")
    }

    fun calculate(gatheringId: UUID): CalculationResponse {
        val gathering = gatheringRepository.findByIdWithDetails(gatheringId)
            ?: throw BusinessException(ErrorCode.GATHERING_NOT_FOUND)

        val participants = gathering.participants.toList()
        val rounds = gathering.rounds.toList()

        val totalAmount = rounds.sumOf { it.amount }
        val balances = calculateBalances(participants, rounds)
        val debts = resolveDebts(balances)

        return CalculationResponse(
            gatheringId = gathering.id!!,
            gatheringName = gathering.name,
            totalAmount = totalAmount,
            balances = balances,
            debts = debts
        )
    }

    private fun calculateBalances(
        participants: List<Participant>,
        rounds: List<SettlementRound>
    ): List<UserBalanceResponse> {
        val paidMap = mutableMapOf<UUID, BigDecimal>()
        val owedMap = mutableMapOf<UUID, BigDecimal>()

        participants.forEach { p ->
            paidMap[p.id!!] = BigDecimal.ZERO
            owedMap[p.id!!] = BigDecimal.ZERO
        }

        rounds.forEach { round ->
            val payerId = round.payer.id!!
            paidMap[payerId] = paidMap[payerId]!! + round.amount

            val excludedIds = round.exclusions.map { it.participant.id!! }.toSet()
            val includedParticipants = participants.filter { it.id !in excludedIds }

            if (includedParticipants.isNotEmpty()) {
                val perPerson = round.amount.divide(
                    BigDecimal(includedParticipants.size),
                    2,
                    RoundingMode.HALF_UP
                )
                includedParticipants.forEach { p ->
                    owedMap[p.id!!] = owedMap[p.id!!]!! + perPerson
                }
            }
        }

        return participants.map { p ->
            val paid = paidMap[p.id!!]!!
            val owed = owedMap[p.id!!]!!
            UserBalanceResponse(
                participantId = p.id!!,
                name = p.name,
                totalPaid = paid,
                totalOwed = owed,
                netBalance = paid - owed
            )
        }
    }

    private fun resolveDebts(balances: List<UserBalanceResponse>): List<DebtResponse> {
        data class MutableBalance(val name: String, var balance: BigDecimal)

        val creditors = balances
            .filter { it.netBalance > EPSILON }
            .map { MutableBalance(it.name, it.netBalance) }
            .sortedByDescending { it.balance }
            .toMutableList()

        val debtors = balances
            .filter { it.netBalance < -EPSILON }
            .map { MutableBalance(it.name, it.netBalance) }
            .sortedBy { it.balance }
            .toMutableList()

        val debts = mutableListOf<DebtResponse>()
        var i = 0
        var j = 0

        while (i < debtors.size && j < creditors.size) {
            val debtor = debtors[i]
            val creditor = creditors[j]

            val amount = minOf(debtor.balance.abs(), creditor.balance)

            debts.add(
                DebtResponse(
                    from = debtor.name,
                    to = creditor.name,
                    amount = amount.setScale(0, RoundingMode.HALF_UP)
                )
            )

            debtor.balance += amount
            creditor.balance -= amount

            if (debtor.balance.abs() < EPSILON) i++
            if (creditor.balance.abs() < EPSILON) j++
        }

        return debts
    }
}
