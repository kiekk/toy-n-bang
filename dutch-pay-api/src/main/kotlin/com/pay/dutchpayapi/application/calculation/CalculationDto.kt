package com.pay.dutchpayapi.application.calculation

import java.math.BigDecimal
import java.util.*

data class UserBalanceResponse(
    val participantId: UUID,
    val name: String,
    val totalPaid: BigDecimal,
    val totalOwed: BigDecimal,
    val netBalance: BigDecimal
)

data class DebtResponse(
    val from: String,
    val to: String,
    val amount: BigDecimal
)

data class CalculationResponse(
    val gatheringId: UUID,
    val gatheringName: String,
    val totalAmount: BigDecimal,
    val balances: List<UserBalanceResponse>,
    val debts: List<DebtResponse>
)
