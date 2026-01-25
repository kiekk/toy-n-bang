package com.nbang.nbangapi.application.round.request

import java.math.BigDecimal

data class RoundCreateRequest(
    val title: String,
    val amount: BigDecimal,
    val payerId: Long,
    val exclusions: List<ExclusionRequest>? = null
)
