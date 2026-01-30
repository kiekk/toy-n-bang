package com.nbang.nbangapi.application.auth.event

import com.nbang.nbangapi.domain.member.OAuthProvider

data class MemberLoginEvent(
    val memberId: Long,
    val provider: OAuthProvider,
    val ipAddress: String?,
    val userAgent: String?,
)
