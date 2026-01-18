package com.pay.dutchpayapi.application.participant

import com.pay.dutchpayapi.domain.gathering.GatheringRepository
import com.pay.dutchpayapi.domain.participant.Participant
import com.pay.dutchpayapi.domain.participant.ParticipantRepository
import com.pay.dutchpayapi.support.error.BusinessException
import com.pay.dutchpayapi.support.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val gatheringRepository: GatheringRepository
) {

    @Transactional
    fun addToGathering(gatheringId: UUID, request: ParticipantCreateRequest): ParticipantResponse {
        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { BusinessException(ErrorCode.GATHERING_NOT_FOUND) }

        val participant = Participant(name = request.name)
        gathering.addParticipant(participant)

        val saved = participantRepository.save(participant)
        return ParticipantResponse.from(saved)
    }

    fun findByGatheringId(gatheringId: UUID): List<ParticipantResponse> {
        if (!gatheringRepository.existsById(gatheringId)) {
            throw BusinessException(ErrorCode.GATHERING_NOT_FOUND)
        }

        return participantRepository.findByGatheringId(gatheringId)
            .map { ParticipantResponse.from(it) }
    }

    @Transactional
    fun delete(id: UUID) {
        if (!participantRepository.existsById(id)) {
            throw BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND)
        }
        participantRepository.deleteById(id)
    }
}
