package com.nbang.nbangapi.domain.member

import com.nbang.nbangapi.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "members")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false, length = 100)
    var nickname: String,

    @Column(length = 500)
    var profileImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: OAuthProvider,

    @Column(nullable = false)
    val providerId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role = Role.USER,
) : BaseEntity() {

    fun updateProfile(nickname: String, profileImage: String?) {
        this.nickname = nickname
        this.profileImage = profileImage
    }

    companion object {
        fun create(
            email: String,
            nickname: String,
            profileImage: String?,
            provider: OAuthProvider,
            providerId: String,
        ): Member {
            return Member(
                email = email,
                nickname = nickname,
                profileImage = profileImage,
                provider = provider,
                providerId = providerId,
                role = Role.USER,
            )
        }
    }
}
