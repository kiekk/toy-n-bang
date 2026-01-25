package com.nbang.nbangapi.application.calculation.response

import java.math.BigDecimal

data class UserBalanceResponse(
    val participantId: Long,
    val name: String,
    val totalPaid: BigDecimal,
    val totalOwed: BigDecimal,
    val netBalance: BigDecimal
)
