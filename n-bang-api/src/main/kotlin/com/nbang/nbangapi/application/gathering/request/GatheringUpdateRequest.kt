package com.nbang.nbangapi.application.gathering.request

import com.nbang.nbangapi.domain.gathering.GatheringType
import java.time.LocalDate

data class GatheringUpdateRequest(
    val name: String,
    val type: GatheringType,
    val startDate: LocalDate,
    val endDate: LocalDate
)
