package com.pay.dutchpayapi.domain.exclusion

import com.pay.dutchpayapi.domain.common.BaseEntity
import com.pay.dutchpayapi.domain.participant.Participant
import com.pay.dutchpayapi.domain.round.SettlementRound
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "exclusions")
class Exclusion(
    @Column(nullable = false)
    val reason: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    val participant: Participant,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    lateinit var round: SettlementRound
}
