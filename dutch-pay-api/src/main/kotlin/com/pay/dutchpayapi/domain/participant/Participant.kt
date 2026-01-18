package com.pay.dutchpayapi.domain.participant

import com.pay.dutchpayapi.domain.common.BaseEntity
import com.pay.dutchpayapi.domain.gathering.Gathering
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "participants")
class Participant(
    @Column(nullable = false)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    lateinit var gathering: Gathering

    fun updateName(name: String) {
        this.name = name
    }
}
