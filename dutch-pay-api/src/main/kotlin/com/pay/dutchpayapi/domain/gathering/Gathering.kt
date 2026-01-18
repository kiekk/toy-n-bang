package com.pay.dutchpayapi.domain.gathering

import com.pay.dutchpayapi.domain.common.BaseEntity
import com.pay.dutchpayapi.domain.participant.Participant
import com.pay.dutchpayapi.domain.round.SettlementRound
import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "gatherings")
class Gathering(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null
) : BaseEntity() {

    @OneToMany(mappedBy = "gathering", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _participants: MutableSet<Participant> = mutableSetOf()
    val participants: Set<Participant> get() = _participants.toSet()

    @OneToMany(mappedBy = "gathering", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _rounds: MutableSet<SettlementRound> = mutableSetOf()
    val rounds: Set<SettlementRound> get() = _rounds.toSet()

    fun update(name: String, startDate: LocalDate, endDate: LocalDate) {
        this.name = name
        this.startDate = startDate
        this.endDate = endDate
    }

    fun addParticipant(participant: Participant) {
        _participants.add(participant)
        participant.gathering = this
    }

    fun addRound(round: SettlementRound) {
        _rounds.add(round)
        round.gathering = this
    }
}
