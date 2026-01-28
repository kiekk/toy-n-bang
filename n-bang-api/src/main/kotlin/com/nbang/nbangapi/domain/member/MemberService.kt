package com.nbang.nbangapi.domain.member

import com.nbang.nbangapi.support.error.CoreException
import com.nbang.nbangapi.support.error.ErrorType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
) {

    fun getById(id: Long): Member {
        return memberRepository.findById(id)
            ?: throw CoreException(ErrorType.MEMBER_NOT_FOUND)
    }

    @Transactional
    fun createOrUpdate(
        email: String,
        nickname: String,
        profileImage: String?,
        provider: OAuthProvider,
        providerId: String,
    ): Member {
        val existingMember = memberRepository.findByProviderAndProviderId(provider, providerId)

        return if (existingMember != null) {
            existingMember.updateProfile(nickname, profileImage)
            existingMember
        } else {
            val newMember = Member.create(
                email = email,
                nickname = nickname,
                profileImage = profileImage,
                provider = provider,
                providerId = providerId,
            )
            memberRepository.save(newMember)
        }
    }
}
