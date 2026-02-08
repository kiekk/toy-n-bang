package com.nbang.nbangapi.application.gathering.request

import com.nbang.nbangapi.domain.gathering.GatheringType
import java.time.LocalDate

data class GatheringCreateRequest(
    val name: String,
    val type: GatheringType = GatheringType.OTHER,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val participantNames: List<String>? = null
)
