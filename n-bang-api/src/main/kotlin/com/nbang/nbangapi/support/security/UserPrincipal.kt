package com.nbang.nbangapi.support.security

import com.nbang.nbangapi.domain.member.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class UserPrincipal(
    val id: Long,
    val email: String,
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: Map<String, Any> = emptyMap(),
) : UserDetails, OAuth2User {

    override fun getName(): String = id.toString()

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String? = null

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    companion object {
        fun from(member: Member): UserPrincipal {
            return UserPrincipal(
                id = member.id!!,
                email = member.email,
                authorities = listOf(SimpleGrantedAuthority(member.role.toAuthority())),
            )
        }

        fun from(member: Member, attributes: Map<String, Any>): UserPrincipal {
            return UserPrincipal(
                id = member.id!!,
                email = member.email,
                authorities = listOf(SimpleGrantedAuthority(member.role.toAuthority())),
                attributes = attributes,
            )
        }
    }
}
