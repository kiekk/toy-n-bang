package com.nbang.nbangapi.application.gathering.request

import java.time.LocalDate

data class GatheringUpdateRequest(
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
