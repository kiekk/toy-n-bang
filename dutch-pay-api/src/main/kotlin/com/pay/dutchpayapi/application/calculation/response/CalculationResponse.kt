package com.pay.dutchpayapi.application.calculation.response

import java.math.BigDecimal

data class CalculationResponse(
    val gatheringId: Long,
    val gatheringName: String,
    val totalAmount: BigDecimal,
    val balances: List<UserBalanceResponse>,
    val debts: List<DebtResponse>
)
