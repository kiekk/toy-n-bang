package com.nbang.nbangapi.application.auth

import com.nbang.nbangapi.application.auth.request.TokenRefreshRequest
import com.nbang.nbangapi.application.auth.response.MemberResponse
import com.nbang.nbangapi.application.auth.response.TokenResponse
import com.nbang.nbangapi.domain.member.MemberService
import com.nbang.nbangapi.support.security.JwtProperties
import com.nbang.nbangapi.support.security.JwtTokenProvider
import org.springframework.stereotype.Component

@Component
class AuthFacade(
    private val memberService: MemberService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
) {

    fun refreshToken(request: TokenRefreshRequest): TokenResponse {
        jwtTokenProvider.validateTokenOrThrow(request.refreshToken)

        val memberId = jwtTokenProvider.getMemberIdFromToken(request.refreshToken)
        val member = memberService.getById(memberId)
        val id = member.id!!

        val accessToken = jwtTokenProvider.createAccessToken(id)
        val refreshToken = jwtTokenProvider.createRefreshToken(id)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000,
        )
    }

    fun getMe(memberId: Long): MemberResponse {
        val member = memberService.getById(memberId)
        return MemberResponse.from(member)
    }
}
