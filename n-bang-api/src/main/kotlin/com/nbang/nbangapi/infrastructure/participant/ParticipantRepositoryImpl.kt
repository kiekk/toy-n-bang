package com.nbang.nbangapi.infrastructure.participant

import com.nbang.nbangapi.domain.participant.Participant
import com.nbang.nbangapi.domain.participant.ParticipantRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ParticipantRepositoryImpl(
    private val participantJpaRepository: ParticipantJpaRepository
) : ParticipantRepository {

    override fun save(participant: Participant): Participant {
        return participantJpaRepository.save(participant)
    }

    override fun findById(id: Long): Optional<Participant> {
        return participantJpaRepository.findById(id)
    }

    override fun findByGatheringId(gatheringId: Long): List<Participant> {
        return participantJpaRepository.findByGatheringId(gatheringId)
    }

    override fun existsById(id: Long): Boolean {
        return participantJpaRepository.existsById(id)
    }

    override fun deleteById(id: Long) {
        participantJpaRepository.deleteById(id)
    }
}
