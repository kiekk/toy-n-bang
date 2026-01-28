package com.nbang.nbangapi.domain.gathering

import com.nbang.nbangapi.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "gatherings")
class Gathering(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,
) : BaseEntity() {

    fun update(name: String, startDate: LocalDate, endDate: LocalDate) {
        this.name = name
        this.startDate = startDate
        this.endDate = endDate
    }

    fun isOwnedBy(memberId: Long): Boolean = this.memberId == memberId
}
