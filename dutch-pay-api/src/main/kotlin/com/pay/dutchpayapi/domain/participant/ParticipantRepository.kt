package com.pay.dutchpayapi.domain.participant

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ParticipantRepository : JpaRepository<Participant, UUID> {

    fun findByGatheringId(gatheringId: UUID): List<Participant>
}
