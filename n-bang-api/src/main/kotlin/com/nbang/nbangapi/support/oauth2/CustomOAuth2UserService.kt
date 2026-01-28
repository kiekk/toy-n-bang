package com.nbang.nbangapi.support.oauth2

import com.nbang.nbangapi.domain.member.MemberService
import com.nbang.nbangapi.domain.member.OAuthProvider
import com.nbang.nbangapi.support.security.UserPrincipal
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService,
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val attributes = oAuth2User.attributes

        val userInfo = OAuth2UserInfoFactory.create(registrationId, attributes)
        val provider = OAuthProvider.from(registrationId)

        val member = memberService.createOrUpdate(
            email = userInfo.email,
            nickname = userInfo.nickname,
            profileImage = userInfo.profileImage,
            provider = provider,
            providerId = userInfo.id,
        )

        return UserPrincipal.from(member, attributes)
    }
}
