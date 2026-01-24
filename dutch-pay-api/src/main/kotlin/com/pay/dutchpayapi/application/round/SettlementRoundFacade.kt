package com.pay.dutchpayapi.application.round

import com.pay.dutchpayapi.application.round.request.RoundCreateRequest
import com.pay.dutchpayapi.application.round.request.RoundUpdateRequest
import com.pay.dutchpayapi.application.round.response.ExclusionResponse
import com.pay.dutchpayapi.application.round.response.RoundResponse
import com.pay.dutchpayapi.domain.exclusion.ExclusionService
import com.pay.dutchpayapi.domain.gathering.GatheringService
import com.pay.dutchpayapi.domain.participant.ParticipantService
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

        val exclusionResponses = request.exclusions?.map { exclusionRequest ->
            val participant = participantService.findById(exclusionRequest.participantId)

            val exclusion = exclusionService.create(
                reason = exclusionRequest.reason,
                participantId = exclusionRequest.participantId,
                roundId = round.id!!
            )

            ExclusionResponse(
                id = exclusion.id!!,
                participantId = exclusion.participantId,
                participantName = participant.name,
                reason = exclusion.reason
            )
        } ?: emptyList()

        return RoundResponse.from(
            round = round,
            payerName = payer.name,
            exclusions = exclusionResponses
        )
    }

    fun findByGatheringId(gatheringId: Long): List<RoundResponse> {
        gatheringService.findById(gatheringId)

        val participants = participantService.findByGatheringId(gatheringId)
        val participantMap = participants.associateBy { it.id!! }

        return roundService.findByGatheringId(gatheringId).map { round ->
            val exclusions = exclusionService.findByRoundId(round.id!!)
            val exclusionResponses = exclusions.map { exclusion ->
                ExclusionResponse(
                    id = exclusion.id!!,
                    participantId = exclusion.participantId,
                    participantName = participantMap[exclusion.participantId]?.name ?: "",
                    reason = exclusion.reason
                )
            }
            RoundResponse.from(
                round = round,
                payerName = participantMap[round.payerId]?.name ?: "",
                exclusions = exclusionResponses
            )
        }
    }

    fun findById(id: Long): RoundResponse {
        val round = roundService.findById(id)
        val payer = participantService.findById(round.payerId)

        val participants = participantService.findByGatheringId(round.gatheringId)
        val participantMap = participants.associateBy { it.id!! }

        val exclusions = exclusionService.findByRoundId(round.id!!)
        val exclusionResponses = exclusions.map { exclusion ->
            ExclusionResponse(
                id = exclusion.id!!,
                participantId = exclusion.participantId,
                participantName = participantMap[exclusion.participantId]?.name ?: "",
                reason = exclusion.reason
            )
        }

        return RoundResponse.from(
            round = round,
            payerName = payer.name,
            exclusions = exclusionResponses
        )
    }

    @Transactional
    fun update(id: Long, request: RoundUpdateRequest): RoundResponse {
        val round = roundService.update(id, request.title, request.amount, request.payerId)
        val payer = participantService.findById(request.payerId)

        exclusionService.deleteByRoundId(round.id!!)

        val participants = participantService.findByGatheringId(round.gatheringId)
        val participantMap = participants.associateBy { it.id!! }

        val exclusionResponses = request.exclusions?.map { exclusionRequest ->
            val participant = participantService.findById(exclusionRequest.participantId)

            val exclusion = exclusionService.create(
                reason = exclusionRequest.reason,
                participantId = exclusionRequest.participantId,
                roundId = round.id!!
            )

            ExclusionResponse(
                id = exclusion.id!!,
                participantId = exclusion.participantId,
                participantName = participant.name,
                reason = exclusion.reason
            )
        } ?: emptyList()

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
        val payer = participantService.findById(round.payerId)

        val participants = participantService.findByGatheringId(round.gatheringId)
        val participantMap = participants.associateBy { it.id!! }

        val exclusions = exclusionService.findByRoundId(round.id!!)
        val exclusionResponses = exclusions.map { exclusion ->
            ExclusionResponse(
                id = exclusion.id!!,
                participantId = exclusion.participantId,
                participantName = participantMap[exclusion.participantId]?.name ?: "",
                reason = exclusion.reason
            )
        }

        return RoundResponse.from(
            round = round,
            payerName = payer.name,
            exclusions = exclusionResponses
        )
    }
}
