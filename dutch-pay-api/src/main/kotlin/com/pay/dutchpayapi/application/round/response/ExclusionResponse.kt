package com.pay.dutchpayapi.application.round.response

data class ExclusionResponse(
    val id: Long,
    val participantId: Long,
    val participantName: String,
    val reason: String
)
