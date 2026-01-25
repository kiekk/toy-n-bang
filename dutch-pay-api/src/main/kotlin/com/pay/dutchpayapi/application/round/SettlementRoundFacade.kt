package com.pay.dutchpayapi.application.round

import com.pay.dutchpayapi.application.round.request.ExclusionRequest
import com.pay.dutchpayapi.application.round.request.RoundCreateRequest
import com.pay.dutchpayapi.application.round.request.RoundUpdateRequest
import com.pay.dutchpayapi.application.round.response.ExclusionResponse
import com.pay.dutchpayapi.application.round.response.RoundResponse
import com.pay.dutchpayapi.domain.exclusion.Exclusion
import com.pay.dutchpayapi.domain.exclusion.ExclusionService
import com.pay.dutchpayapi.domain.gathering.GatheringService
import com.pay.dutchpayapi.domain.participant.Participant
import com.pay.dutchpayapi.domain.participant.ParticipantService
import com.pay.dutchpayapi.domain.round.SettlementRound
import com.pay.dutchpayapi.domain.round.SettlementRoundService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class SettlementRoundFacade(
    private val roundService: SettlementRoundService,
    private val gatheringService: GatheringService,
    private val participantService: ParticipantService,
    private val exclusionService: ExclusionService
) {

    @Transactional
    fun create(gatheringId: Long, request: RoundCreateRequest): RoundResponse {
        gatheringService.findById(gatheringId)

        val payer = participantService.findById(request.payerId)

        val round = roundService.create(
            title = request.title,
            amount = request.amount,
            payerId = request.payerId,
            gatheringId = gatheringId
        )

        val exclusionResponses = createExclusions(request.exclusions, round.id!!)

        return RoundResponse.from(
            round = round,
            payerName = payer.name,
            exclusions = exclusionResponses
        )
    }

    fun findByGatheringId(gatheringId: Long): List<RoundResponse> {
        gatheringService.findById(gatheringId)

        val participantMap = getParticipantMap(gatheringId)

        return roundService.findByGatheringId(gatheringId).map { round ->
            toRoundResponse(round, participantMap)
        }
    }

    fun findById(id: Long): RoundResponse {
        val round = roundService.findById(id)
        val participantMap = getParticipantMap(round.gatheringId)
        return toRoundResponse(round, participantMap)
    }

    @Transactional
    fun update(id: Long, request: RoundUpdateRequest): RoundResponse {
        val round = roundService.update(id, request.title, request.amount, request.payerId)
        val payer = participantService.findById(request.payerId)

        exclusionService.deleteByRoundId(round.id!!)

        val exclusionResponses = createExclusions(request.exclusions, round.id!!)

        return RoundResponse.from(
            round = round,
            payerName = payer.name,
            exclusions = exclusionResponses
        )
    }

    @Transactional
    fun delete(id: Long) {
        exclusionService.deleteByRoundId(id)
        roundService.delete(id)
    }

    @Transactional
    fun updateReceiptImage(id: Long, imageUrl: String): RoundResponse {
        val round = roundService.updateReceiptImage(id, imageUrl)
        val participantMap = getParticipantMap(round.gatheringId)
        return toRoundResponse(round, participantMap)
    }

    private fun getParticipantMap(gatheringId: Long): Map<Long, Participant> {
        return participantService.findByGatheringId(gatheringId).associateBy { it.id!! }
    }

    private fun toRoundResponse(round: SettlementRound, participantMap: Map<Long, Participant>): RoundResponse {
        val exclusionResponses = toExclusionResponses(
            exclusionService.findByRoundId(round.id!!),
            participantMap
        )
        return RoundResponse.from(
            round = round,
            payerName = participantMap[round.payerId]?.name ?: "",
            exclusions = exclusionResponses
        )
    }

    private fun toExclusionResponses(
        exclusions: List<Exclusion>,
        participantMap: Map<Long, Participant>
    ): List<ExclusionResponse> {
        return exclusions.map { exclusion ->
            ExclusionResponse(
                id = exclusion.id!!,
                participantId = exclusion.participantId,
                participantName = participantMap[exclusion.participantId]?.name ?: "",
                reason = exclusion.reason
            )
        }
    }

    private fun createExclusions(requests: List<ExclusionRequest>?, roundId: Long): List<ExclusionResponse> {
        return requests?.map { request ->
            val participant = participantService.findById(request.participantId)
            val exclusion = exclusionService.create(
                reason = request.reason,
                participantId = request.participantId,
                roundId = roundId
            )
            ExclusionResponse(
                id = exclusion.id!!,
                participantId = exclusion.participantId,
                participantName = participant.name,
                reason = exclusion.reason
            )
        } ?: emptyList()
    }
}
