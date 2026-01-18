package com.pay.dutchpayapi.support.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid input value"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),

    // Gathering
    GATHERING_NOT_FOUND(HttpStatus.NOT_FOUND, "Gathering not found"),

    // Participant
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Participant not found"),

    // Round
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "Settlement round not found"),
}
