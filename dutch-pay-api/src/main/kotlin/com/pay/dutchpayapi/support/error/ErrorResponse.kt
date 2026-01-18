package com.pay.dutchpayapi.support.error

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status.value(),
                error = errorCode.status.reasonPhrase,
                message = errorCode.message
            )
        }

        fun of(errorCode: ErrorCode, message: String): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status.value(),
                error = errorCode.status.reasonPhrase,
                message = message
            )
        }
    }
}
