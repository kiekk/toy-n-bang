package com.nbang.nbangapi.support.oauth2

import com.nbang.nbangapi.domain.member.OAuthProvider
import com.nbang.nbangapi.application.auth.event.MemberLoginEvent
import com.nbang.nbangapi.support.security.JwtTokenProvider
import com.nbang.nbangapi.support.security.UserPrincipal
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${app.oauth2.authorized-redirect-uri:http://localhost:3000/oauth2/redirect}")
    private val redirectUri: String,
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userPrincipal = authentication.principal as UserPrincipal
        val oAuth2Token = authentication as OAuth2AuthenticationToken
        val provider = OAuthProvider.from(oAuth2Token.authorizedClientRegistrationId)

        publishLoginEvent(userPrincipal.id, provider, request)

        val accessToken = jwtTokenProvider.createAccessToken(userPrincipal.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(userPrincipal.id)

        val targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString()

        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun publishLoginEvent(memberId: Long, provider: OAuthProvider, request: HttpServletRequest) {
        val ipAddress = extractClientIp(request)
        val userAgent = request.getHeader("User-Agent")

        eventPublisher.publishEvent(
            MemberLoginEvent(
                memberId = memberId,
                provider = provider,
                ipAddress = ipAddress,
                userAgent = userAgent,
            )
        )
    }

    private fun extractClientIp(request: HttpServletRequest): String? {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (!xForwardedFor.isNullOrBlank()) {
            xForwardedFor.split(",").firstOrNull()?.trim()
        } else {
            request.remoteAddr
        }
    }
}
