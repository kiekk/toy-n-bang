package com.pay.dutchpayapi.application.round

import com.pay.dutchpayapi.domain.exclusion.Exclusion
import com.pay.dutchpayapi.domain.round.SettlementRound
import java.math.BigDecimal
import java.util.*

data class ExclusionRequest(
    val participantId: UUID,
    val reason: String
)

data class ExclusionResponse(
    val id: UUID,
    val participantId: UUID,
    val participantName: String,
    val reason: String
) {
    companion object {
        fun from(exclusion: Exclusion): ExclusionResponse {
            return ExclusionResponse(
                id = exclusion.id!!,
                participantId = exclusion.participant.id!!,
                participantName = exclusion.participant.name,
                reason = exclusion.reason
            )
        }
    }
}

data class RoundCreateRequest(
    val title: String,
    val amount: BigDecimal,
    val payerId: UUID,
    val exclusions: List<ExclusionRequest>? = null
)

data class RoundUpdateRequest(
    val title: String,
    val amount: BigDecimal,
    val payerId: UUID,
    val exclusions: List<ExclusionRequest>? = null
)

data class RoundResponse(
    val id: UUID,
    val title: String,
    val amount: BigDecimal,
    val payerId: UUID,
    val payerName: String,
    val receiptImageUrl: String?,
    val exclusions: List<ExclusionResponse>
) {
    companion object {
        fun from(round: SettlementRound): RoundResponse {
            return RoundResponse(
                id = round.id!!,
                title = round.title,
                amount = round.amount,
                payerId = round.payer.id!!,
                payerName = round.payer.name,
                receiptImageUrl = round.receiptImageUrl,
                exclusions = round.exclusions.map { ExclusionResponse.from(it) }
            )
        }
    }
}
