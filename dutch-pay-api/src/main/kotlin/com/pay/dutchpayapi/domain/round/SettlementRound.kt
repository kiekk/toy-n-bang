package com.pay.dutchpayapi.domain.round

import com.pay.dutchpayapi.domain.common.BaseEntity
import com.pay.dutchpayapi.domain.exclusion.Exclusion
import com.pay.dutchpayapi.domain.gathering.Gathering
import com.pay.dutchpayapi.domain.participant.Participant
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "settlement_rounds")
class SettlementRound(
    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    var payer: Participant,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null
) : BaseEntity() {

    @Column
    var receiptImageUrl: String? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    lateinit var gathering: Gathering

    @OneToMany(mappedBy = "round", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _exclusions: MutableSet<Exclusion> = mutableSetOf()
    val exclusions: Set<Exclusion> get() = _exclusions.toSet()

    fun update(title: String, amount: BigDecimal, payer: Participant) {
        this.title = title
        this.amount = amount
        this.payer = payer
    }

    fun updateReceiptImageUrl(url: String?) {
        this.receiptImageUrl = url
    }

    fun addExclusion(exclusion: Exclusion) {
        _exclusions.add(exclusion)
        exclusion.round = this
    }

    fun clearExclusions() {
        _exclusions.clear()
    }
}
