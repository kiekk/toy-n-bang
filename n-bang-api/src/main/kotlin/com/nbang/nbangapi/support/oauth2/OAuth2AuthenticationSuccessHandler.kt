package com.nbang.nbangapi.support.oauth2

import com.nbang.nbangapi.support.security.JwtTokenProvider
import com.nbang.nbangapi.support.security.UserPrincipal
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${app.oauth2.authorized-redirect-uri:http://localhost:3000/oauth2/redirect}")
    private val redirectUri: String,
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userPrincipal = authentication.principal as UserPrincipal

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
}
