package com.pay.dutchpayapi.application.participant

import com.pay.dutchpayapi.application.participant.request.ParticipantCreateRequest
import com.pay.dutchpayapi.application.participant.response.ParticipantResponse
import com.pay.dutchpayapi.domain.gathering.GatheringService
import com.pay.dutchpayapi.domain.participant.ParticipantService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ParticipantFacade(
    private val participantService: ParticipantService,
    private val gatheringService: GatheringService
) {

    @Transactional
    fun addToGathering(gatheringId: Long, request: ParticipantCreateRequest): ParticipantResponse {
        gatheringService.findById(gatheringId)

        val participant = participantService.create(
            name = request.name,
            gatheringId = gatheringId
        )

        return ParticipantResponse.from(participant)
    }

    fun findByGatheringId(gatheringId: Long): List<ParticipantResponse> {
        gatheringService.findById(gatheringId)

        return participantService.findByGatheringId(gatheringId)
            .map { ParticipantResponse.from(it) }
    }

    @Transactional
    fun delete(id: Long) {
        participantService.delete(id)
    }
}
