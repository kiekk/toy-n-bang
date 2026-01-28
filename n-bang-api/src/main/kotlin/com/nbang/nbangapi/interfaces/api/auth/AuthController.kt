package com.nbang.nbangapi.interfaces.api.auth

import com.nbang.nbangapi.application.auth.AuthFacade
import com.nbang.nbangapi.application.auth.request.TokenRefreshRequest
import com.nbang.nbangapi.application.auth.response.MemberResponse
import com.nbang.nbangapi.application.auth.response.TokenResponse
import com.nbang.nbangapi.support.security.CurrentUser
import com.nbang.nbangapi.support.security.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authFacade: AuthFacade,
) {

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: TokenRefreshRequest): ResponseEntity<TokenResponse> {
        val response = authFacade.refreshToken(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getMe(@CurrentUser user: UserPrincipal): ResponseEntity<MemberResponse> {
        val response = authFacade.getMe(user.id)
        return ResponseEntity.ok(response)
    }
}
