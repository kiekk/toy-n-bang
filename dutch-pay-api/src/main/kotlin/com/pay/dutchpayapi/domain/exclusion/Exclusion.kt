package com.pay.dutchpayapi.domain.exclusion

import com.pay.dutchpayapi.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "exclusions")
class Exclusion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val reason: String,

    @Column(name = "participant_id", nullable = false)
    val participantId: Long,

    @Column(name = "round_id", nullable = false)
    val roundId: Long,
) : BaseEntity()
