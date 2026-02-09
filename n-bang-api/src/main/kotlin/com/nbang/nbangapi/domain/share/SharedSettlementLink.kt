package com.nbang.nbangapi.domain.share

import com.nbang.nbangapi.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "shared_settlement_links")
class SharedSettlementLink(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, updatable = false)
    val uuid: String = UUID.randomUUID().toString(),

    @Column(name = "gathering_id", nullable = false)
    val gatheringId: Long,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,
) : BaseEntity() {

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
