package com.nbang.nbangapi.domain.member

interface MemberRepository {
    fun findById(id: Long): Member?
    fun findByProviderAndProviderId(provider: OAuthProvider, providerId: String): Member?
    fun save(member: Member): Member
}
