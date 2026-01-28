package com.nbang.nbangapi.support.oauth2

interface OAuth2UserInfo {
    val id: String
    val email: String
    val nickname: String
    val profileImage: String?
}

class GoogleOAuth2UserInfo(
    private val attributes: Map<String, Any>
) : OAuth2UserInfo {

    override val id: String
        get() = attributes["sub"] as String

    override val email: String
        get() = attributes["email"] as String

    override val nickname: String
        get() = attributes["name"] as String

    override val profileImage: String?
        get() = attributes["picture"] as? String
}

class KakaoOAuth2UserInfo(
    private val attributes: Map<String, Any>
) : OAuth2UserInfo {

    override val id: String
        get() = attributes["id"].toString()

    // 카카오는 이메일 정책상 제공받지 않으므로 kakaoId 기반으로 생성
    override val email: String
        get() = "${id}@kakao.user"

    override val nickname: String
        get() {
            @Suppress("UNCHECKED_CAST")
            val properties = attributes["properties"] as? Map<String, Any>
            return properties?.get("nickname") as? String ?: "Unknown"
        }

    override val profileImage: String?
        get() {
            @Suppress("UNCHECKED_CAST")
            val properties = attributes["properties"] as? Map<String, Any>
            return properties?.get("profile_image") as? String
        }
}

object OAuth2UserInfoFactory {
    fun create(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            else -> throw IllegalArgumentException("Unsupported OAuth2 provider: $registrationId")
        }
    }
}
