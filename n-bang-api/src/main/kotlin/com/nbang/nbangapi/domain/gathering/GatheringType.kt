package com.nbang.nbangapi.domain.gathering

enum class GatheringType(
    val description: String
) {
    TRAVEL("국내/해외 여행"),
    DINING("회사/팀 회식"),
    MEETING("동창회, 동호회 등"),
    DATE("커플 데이트"),
    CEREMONY("축의금, 조의금 등"),
    HOBBY("운동, 게임 등"),
    OTHER("그 외");

    companion object {
        fun from(value: String): GatheringType {
            return entries.find { it.name == value.uppercase() }
                ?: throw IllegalArgumentException("Unknown gathering type: $value")
        }
    }
}
