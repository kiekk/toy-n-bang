package com.nbang.nbangapi.domain.participant

import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.springframework.stereotype.Service

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository
) {

    fun create(name: String, gatheringId: Long): Participant {
        return participantRepository.save(
            Participant(
                name = name,
                gatheringId = gatheringId
            )
        )
    }

    fun findById(id: Long): Participant {
        return participantRepository.findById(id)
            .orElseThrow { CoreException(ErrorType.PARTICIPANT_NOT_FOUND) }
    }

    fun findByGatheringId(gatheringId: Long): List<Participant> {
        return participantRepository.findByGatheringId(gatheringId)
    }

    fun delete(id: Long) {
        if (!participantRepository.existsById(id)) {
            throw CoreException(ErrorType.PARTICIPANT_NOT_FOUND)
        }
        participantRepository.deleteById(id)
    }

    fun existsById(id: Long): Boolean {
        return participantRepository.existsById(id)
    }
}
