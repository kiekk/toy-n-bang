package com.pay.dutchpayapi.application.gathering

import com.pay.dutchpayapi.application.participant.ParticipantResponse
import com.pay.dutchpayapi.application.round.RoundResponse
import com.pay.dutchpayapi.domain.gathering.Gathering
import java.time.LocalDate
import java.util.*

data class GatheringCreateRequest(
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val participantNames: List<String>? = null
)

data class GatheringUpdateRequest(
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class GatheringResponse(
    val id: UUID,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val participants: List<ParticipantResponse>,
    val rounds: List<RoundResponse>
) {
    companion object {
        fun from(gathering: Gathering): GatheringResponse {
            return GatheringResponse(
                id = gathering.id!!,
                name = gathering.name,
                startDate = gathering.startDate,
                endDate = gathering.endDate,
                participants = gathering.participants.map { ParticipantResponse.from(it) },
                rounds = gathering.rounds.map { RoundResponse.from(it) }
            )
        }

        fun simpleFrom(gathering: Gathering): GatheringResponse {
            return GatheringResponse(
                id = gathering.id!!,
                name = gathering.name,
                startDate = gathering.startDate,
                endDate = gathering.endDate,
                participants = emptyList(),
                rounds = emptyList()
            )
        }
    }
}
