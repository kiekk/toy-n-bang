package com.nbang.nbangapi.infrastructure.member

import com.nbang.nbangapi.domain.member.Member
import com.nbang.nbangapi.domain.member.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<Member, Long> {
    fun findByProviderAndProviderId(provider: OAuthProvider, providerId: String): Member?
}
