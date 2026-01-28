package com.nbang.nbangapi.infrastructure.member

import com.nbang.nbangapi.domain.member.Member
import com.nbang.nbangapi.domain.member.MemberRepository
import com.nbang.nbangapi.domain.member.OAuthProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val memberJpaRepository: MemberJpaRepository,
) : MemberRepository {

    override fun findById(id: Long): Member? {
        return memberJpaRepository.findByIdOrNull(id)
    }

    override fun findByProviderAndProviderId(provider: OAuthProvider, providerId: String): Member? {
        return memberJpaRepository.findByProviderAndProviderId(provider, providerId)
    }

    override fun save(member: Member): Member {
        return memberJpaRepository.save(member)
    }
}
