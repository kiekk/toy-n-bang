package com.pay.dutchpayapi.application.round.request

data class ExclusionRequest(
    val participantId: Long,
    val reason: String
)
