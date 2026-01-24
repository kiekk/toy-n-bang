package com.pay.dutchpayapi.domain.gathering

import com.pay.dutchpayapi.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "gatherings")
class Gathering(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

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
}
