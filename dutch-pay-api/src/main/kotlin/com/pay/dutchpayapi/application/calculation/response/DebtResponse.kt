package com.pay.dutchpayapi.application.calculation.response

import java.math.BigDecimal

data class DebtResponse(
    val from: String,
    val to: String,
    val amount: BigDecimal
)
