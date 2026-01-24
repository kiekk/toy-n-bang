package com.pay.dutchpayapi.application.round.request

import java.math.BigDecimal

data class RoundUpdateRequest(
    val title: String,
    val amount: BigDecimal,
    val payerId: Long,
    val exclusions: List<ExclusionRequest>? = null
)
