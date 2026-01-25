package com.nbang.nbangapi.infrastructure.participant

import com.nbang.nbangapi.domain.participant.Participant
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipantJpaRepository : JpaRepository<Participant, Long> {

    fun findByGatheringId(gatheringId: Long): List<Participant>
}
