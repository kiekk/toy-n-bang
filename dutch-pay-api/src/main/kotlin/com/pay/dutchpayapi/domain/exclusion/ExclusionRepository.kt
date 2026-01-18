package com.pay.dutchpayapi.domain.exclusion

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ExclusionRepository : JpaRepository<Exclusion, UUID>
