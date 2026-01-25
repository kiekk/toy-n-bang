package com.nbang.nbangapi.application.calculation

import com.nbang.nbangapi.application.calculation.response.CalculationResponse
import com.nbang.nbangapi.application.calculation.response.DebtResponse
import com.nbang.nbangapi.application.calculation.response.UserBalanceResponse
import com.nbang.nbangapi.domain.round.SettlementCalculator
import com.nbang.nbangapi.domain.exclusion.ExclusionService
import com.nbang.nbangapi.domain.gathering.GatheringService
import com.nbang.nbangapi.domain.participant.ParticipantService
import com.nbang.nbangapi.domain.round.SettlementRoundService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class CalculationFacade(
    private val gatheringService: GatheringService,
    private val participantService: ParticipantService,
    private val roundService: SettlementRoundService,
    private val exclusionService: ExclusionService,
    private val settlementCalculator: SettlementCalculator
) {

    fun calculate(gatheringId: Long): CalculationResponse {
        val gathering = gatheringService.findById(gatheringId)

        val participants = participantService.findByGatheringId(gatheringId)
        val rounds = roundService.findByGatheringId(gatheringId)

        val exclusionsByRound = rounds.associate { round ->
            round.id!! to exclusionService.findByRoundId(round.id!!).map { it.participantId }.toSet()
        }

        val result = settlementCalculator.calculate(participants, rounds, exclusionsByRound)

        val balanceResponses = result.balances.map { balance ->
            UserBalanceResponse(
                participantId = balance.participantId,
                name = balance.name,
                totalPaid = balance.totalPaid,
                totalOwed = balance.totalOwed,
                netBalance = balance.netBalance
            )
        }

        val debtResponses = result.debts.map { debt ->
            DebtResponse(
                from = debt.from,
                to = debt.to,
                amount = debt.amount
            )
        }

        return CalculationResponse(
            gatheringId = gathering.id!!,
            gatheringName = gathering.name,
            totalAmount = result.totalAmount,
            balances = balanceResponses,
            debts = debtResponses
        )
    }
}
