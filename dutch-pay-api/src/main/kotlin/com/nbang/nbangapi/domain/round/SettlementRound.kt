package com.nbang.nbangapi.domain.round

import com.nbang.nbangapi.domain.common.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "settlement_rounds")
class SettlementRound(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal,

    @Column(name = "payer_id", nullable = false)
    var payerId: Long,

    @Column(name = "gathering_id", nullable = false)
    val gatheringId: Long,
) : BaseEntity() {

    @Column
    var receiptImageUrl: String? = null
        protected set

    fun update(title: String, amount: BigDecimal, payerId: Long) {
        this.title = title
        this.amount = amount
        this.payerId = payerId
    }

    fun updateReceiptImageUrl(url: String?) {
        this.receiptImageUrl = url
    }
}
