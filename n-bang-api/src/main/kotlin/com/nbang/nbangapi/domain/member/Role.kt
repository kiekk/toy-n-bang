package com.nbang.nbangapi.domain.member

enum class Role {
    USER,
    ADMIN;

    fun toAuthority(): String = "ROLE_$name"
}
