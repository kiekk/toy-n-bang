package com.pay.dutchpayapi.domain.round

import com.pay.dutchpayapi.domain.participant.Participant
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SettlementCalculator {

    companion object {
        private val EPSILON = BigDecimal("0.01")
    }

    fun calculate(
        participants: List<Participant>,
        rounds: List<SettlementRound>,
        exclusionsByRound: Map<Long, Set<Long>>
    ): SettlementResult {
        val totalAmount = rounds.sumOf { it.amount }
        val balances = calculateBalances(participants, rounds, exclusionsByRound)
        val debts = resolveDebts(balances)

        return SettlementResult(
            totalAmount = totalAmount,
            balances = balances,
            debts = debts
        )
    }

    private fun calculateBalances(
        participants: List<Participant>,
        rounds: List<SettlementRound>,
        exclusionsByRound: Map<Long, Set<Long>>
    ): List<ParticipantBalance> {
        val paidMap = mutableMapOf<Long, BigDecimal>()
        val owedMap = mutableMapOf<Long, BigDecimal>()

        participants.forEach { p ->
            paidMap[p.id!!] = BigDecimal.ZERO
            owedMap[p.id!!] = BigDecimal.ZERO
        }

        rounds.forEach { round ->
            val payerId = round.payerId
            paidMap[payerId] = paidMap[payerId]!! + round.amount

            val excludedIds = exclusionsByRound[round.id!!] ?: emptySet()
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
            ParticipantBalance(
                participantId = p.id!!,
                name = p.name,
                totalPaid = paid,
                totalOwed = owed,
                netBalance = paid - owed
            )
        }
    }

    private fun resolveDebts(balances: List<ParticipantBalance>): List<Debt> {
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

        val debts = mutableListOf<Debt>()
        var i = 0
        var j = 0

        while (i < debtors.size && j < creditors.size) {
            val debtor = debtors[i]
            val creditor = creditors[j]

            val amount = minOf(debtor.balance.abs(), creditor.balance)

            debts.add(
                Debt(
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

data class SettlementResult(
    val totalAmount: BigDecimal,
    val balances: List<ParticipantBalance>,
    val debts: List<Debt>
)

data class ParticipantBalance(
    val participantId: Long,
    val name: String,
    val totalPaid: BigDecimal,
    val totalOwed: BigDecimal,
    val netBalance: BigDecimal
)

data class Debt(
    val from: String,
    val to: String,
    val amount: BigDecimal
)
