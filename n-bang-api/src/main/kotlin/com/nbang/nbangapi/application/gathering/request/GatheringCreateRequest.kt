package com.nbang.nbangapi.application.gathering.request

import java.time.LocalDate

data class GatheringCreateRequest(
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val participantNames: List<String>? = null
)
