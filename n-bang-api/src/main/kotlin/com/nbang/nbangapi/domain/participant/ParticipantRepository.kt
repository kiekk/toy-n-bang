package com.nbang.nbangapi.domain.participant

import java.util.*

interface ParticipantRepository {
    fun save(participant: Participant): Participant
    fun findById(id: Long): Optional<Participant>
    fun findByGatheringId(gatheringId: Long): List<Participant>
    fun existsById(id: Long): Boolean
    fun deleteById(id: Long)
}
