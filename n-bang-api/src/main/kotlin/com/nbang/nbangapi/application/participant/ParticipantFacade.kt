package com.nbang.nbangapi.application.participant

import com.nbang.nbangapi.application.participant.request.ParticipantCreateRequest
import com.nbang.nbangapi.application.participant.response.ParticipantResponse
import com.nbang.nbangapi.domain.gathering.GatheringService
import com.nbang.nbangapi.domain.participant.ParticipantService
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
