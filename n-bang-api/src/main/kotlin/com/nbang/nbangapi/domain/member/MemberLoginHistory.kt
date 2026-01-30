package com.nbang.nbangapi.domain.member

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "member_login_histories")
class MemberLoginHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: OAuthProvider,

    @Column(length = 45)
    val ipAddress: String? = null,

    @Column(length = 500)
    val userAgent: String? = null,

    @Column(nullable = false)
    val loginAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            memberId: Long,
            provider: OAuthProvider,
            ipAddress: String? = null,
            userAgent: String? = null,
        ): MemberLoginHistory {
            return MemberLoginHistory(
                memberId = memberId,
                provider = provider,
                ipAddress = ipAddress,
                userAgent = userAgent,
            )
        }
    }
}
