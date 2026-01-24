package com.pay.dutchpayapi.infrastructure.gathering

import com.pay.dutchpayapi.domain.gathering.Gathering
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringJpaRepository : JpaRepository<Gathering, Long>
