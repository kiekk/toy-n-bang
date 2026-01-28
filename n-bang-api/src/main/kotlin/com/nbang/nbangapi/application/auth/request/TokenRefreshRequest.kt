package com.nbang.nbangapi.application.auth.request

data class TokenRefreshRequest(
    val refreshToken: String,
)
