package com.nbang.nbangapi.application.share.response

import com.nbang.nbangapi.application.calculation.response.DebtResponse
import com.nbang.nbangapi.application.calculation.response.UserBalanceResponse
import com.nbang.nbangapi.application.round.response.RoundResponse
import java.math.BigDecimal

data class SharedSettlementResponse(
    val gatheringName: String,
    val gatheringType: String,
    val totalAmount: BigDecimal,
    val balances: List<UserBalanceResponse>,
    val debts: List<DebtResponse>,
    val rounds: List<RoundResponse>,
    val expiresAt: String
)
