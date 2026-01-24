package com.pay.dutchpayapi.infrastructure.participant

import com.pay.dutchpayapi.domain.participant.Participant
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipantJpaRepository : JpaRepository<Participant, Long> {

    fun findByGatheringId(gatheringId: Long): List<Participant>
}
