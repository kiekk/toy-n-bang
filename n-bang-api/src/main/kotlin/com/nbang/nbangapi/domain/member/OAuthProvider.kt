package com.nbang.nbangapi.domain.member

enum class OAuthProvider {
    GOOGLE,
    KAKAO;

    companion object {
        fun from(registrationId: String): OAuthProvider {
            return entries.find { it.name.equals(registrationId, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown OAuth provider: $registrationId")
        }
    }
}
