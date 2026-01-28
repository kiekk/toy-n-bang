package com.nbang.nbangapi.application.auth.response

import com.nbang.nbangapi.domain.member.Member

data class MemberResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val provider: String,
) {
    companion object {
        fun from(member: Member): MemberResponse {
            return MemberResponse(
                id = member.id!!,
                email = member.email,
                nickname = member.nickname,
                profileImage = member.profileImage,
                provider = member.provider.name,
            )
        }
    }
}
