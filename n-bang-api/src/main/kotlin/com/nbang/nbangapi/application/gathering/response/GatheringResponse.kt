package com.nbang.nbangapi.application.gathering.response

import com.nbang.nbangapi.application.participant.response.ParticipantResponse
import com.nbang.nbangapi.application.round.response.RoundResponse
import com.nbang.nbangapi.domain.gathering.Gathering
import com.nbang.nbangapi.domain.gathering.GatheringType
import java.time.LocalDate

data class GatheringResponse(
    val id: Long,
    val name: String,
    val type: GatheringType,
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
                type = gathering.type,
                startDate = gathering.startDate,
                endDate = gathering.endDate,
                participants = participants,
                rounds = rounds
            )
        }
    }
}
