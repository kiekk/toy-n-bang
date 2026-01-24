package com.pay.dutchpayapi.infrastructure.gathering

import com.pay.dutchpayapi.domain.gathering.Gathering
import com.pay.dutchpayapi.domain.gathering.GatheringRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class GatheringRepositoryImpl(
    private val gatheringJpaRepository: GatheringJpaRepository
) : GatheringRepository {

    override fun save(gathering: Gathering): Gathering {
        return gatheringJpaRepository.save(gathering)
    }

    override fun findAll(): List<Gathering> {
        return gatheringJpaRepository.findAll()
    }

    override fun findById(id: Long): Optional<Gathering> {
        return gatheringJpaRepository.findById(id)
    }

    override fun existsById(id: Long): Boolean {
        return gatheringJpaRepository.existsById(id)
    }

    override fun deleteById(id: Long) {
        gatheringJpaRepository.deleteById(id)
    }
}
