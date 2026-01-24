package com.pay.dutchpayapi.application.gathering.response

import com.pay.dutchpayapi.application.participant.response.ParticipantResponse
import com.pay.dutchpayapi.application.round.response.RoundResponse
import com.pay.dutchpayapi.domain.gathering.Gathering
import java.time.LocalDate

data class GatheringResponse(
    val id: Long,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val participants: List<ParticipantResponse>,
    val rounds: List<RoundResponse>
) {
    companion object {
        fun from(
            gathering: Gathering,
            participants: List<ParticipantResponse> = emptyList(),
            rounds: List<RoundResponse> = emptyList()
        ): GatheringResponse {
            return GatheringResponse(
                id = gathering.id!!,
                name = gathering.name,
                startDate = gathering.startDate,
                endDate = gathering.endDate,
                participants = participants,
                rounds = rounds
            )
        }
    }
}
