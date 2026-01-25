package com.nbang.nbangapi.application.participant.response

import com.nbang.nbangapi.domain.participant.Participant

data class ParticipantResponse(
    val id: Long,
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
