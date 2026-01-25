package com.nbang.nbangapi.application.round.request

data class ExclusionRequest(
    val participantId: Long,
    val reason: String
)
