package com.pay.dutchpayapi.application.gathering

import com.pay.dutchpayapi.application.gathering.request.GatheringCreateRequest
import com.pay.dutchpayapi.application.gathering.request.GatheringUpdateRequest
import com.pay.dutchpayapi.application.gathering.response.GatheringResponse
import com.pay.dutchpayapi.application.participant.response.ParticipantResponse
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
class GatheringFacade(
    private val gatheringService: GatheringService,
    private val participantService: ParticipantService,
    private val roundService: SettlementRoundService,
    private val exclusionService: ExclusionService
) {

    @Transactional
    fun create(request: GatheringCreateRequest): GatheringResponse {
        val gathering = gatheringService.create(
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate
        )

        val participants = request.participantNames?.map { name ->
            participantService.create(name = name, gatheringId = gathering.id!!)
        } ?: emptyList()

        return GatheringResponse.from(
            gathering = gathering,
            participants = participants.map { ParticipantResponse.from(it) }
        )
    }

    fun findAll(): List<GatheringResponse> {
        return gatheringService.findAll()
            .map { GatheringResponse.from(it) }
    }

    fun findById(id: Long): GatheringResponse {
        val gathering = gatheringService.findById(id)

        val participants = participantService.findByGatheringId(id)
        val participantMap = participants.associateBy { it.id!! }

        val rounds = roundService.findByGatheringId(id)
        val roundResponses = rounds.map { round ->
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

        return GatheringResponse.from(
            gathering = gathering,
            participants = participants.map { ParticipantResponse.from(it) },
            rounds = roundResponses
        )
    }

    @Transactional
    fun update(id: Long, request: GatheringUpdateRequest): GatheringResponse {
        val gathering = gatheringService.update(id, request.name, request.startDate, request.endDate)
        return GatheringResponse.from(gathering)
    }

    @Transactional
    fun delete(id: Long) {
        gatheringService.delete(id)
    }
}
