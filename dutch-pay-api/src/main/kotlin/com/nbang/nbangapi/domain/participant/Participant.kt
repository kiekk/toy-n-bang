package com.nbang.nbangapi.domain.participant

import com.nbang.nbangapi.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "participants")
class Participant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(name = "gathering_id", nullable = false)
    val gatheringId: Long,
) : BaseEntity() {

    fun updateName(name: String) {
        this.name = name
    }
}
