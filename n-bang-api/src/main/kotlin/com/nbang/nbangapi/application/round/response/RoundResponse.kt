package com.nbang.nbangapi.application.round.response

import com.nbang.nbangapi.domain.round.SettlementRound
import java.math.BigDecimal

data class RoundResponse(
    val id: Long,
    val title: String,
    val amount: BigDecimal,
    val payerId: Long,
    val payerName: String,
    val receiptImageUrl: String?,
    val exclusions: List<ExclusionResponse>
) {
    companion object {
        fun from(
            round: SettlementRound,
            payerName: String,
            exclusions: List<ExclusionResponse> = emptyList()
        ): RoundResponse {
            return RoundResponse(
                id = round.id!!,
                title = round.title,
                amount = round.amount,
                payerId = round.payerId,
                payerName = payerName,
                receiptImageUrl = round.receiptImageUrl,
                exclusions = exclusions
            )
        }
    }
}
