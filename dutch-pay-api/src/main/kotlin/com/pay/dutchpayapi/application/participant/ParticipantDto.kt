package com.pay.dutchpayapi.application.participant

import com.pay.dutchpayapi.domain.participant.Participant
import java.util.*

data class ParticipantCreateRequest(
    val name: String
)

data class ParticipantResponse(
    val id: UUID,
    val name: String
) {
    companion object {
        fun from(participant: Participant): ParticipantResponse {
            return ParticipantResponse(
                id = participant.id!!,
                name = participant.name
            )
        }
    }
}
