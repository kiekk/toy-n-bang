package com.nbang.nbangapi.support.utils

import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Table
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DatabaseCleanUp(
    @PersistenceContext private val entityManager: EntityManager,
) : InitializingBean {
    private val tableNames = mutableListOf<String>()

    override fun afterPropertiesSet() {
        entityManager.metamodel.entities
            .filter { entity -> entity.javaType.getAnnotation(Entity::class.java) != null }
            .mapNotNull { entity -> entity.javaType.getAnnotation(Table::class.java)?.name }
            .forEach { tableNames.add(it) }
    }

    @Transactional
    fun truncateAllTables() {
        entityManager.flush()
        entityManager.clear()
        tableNames.forEach { table ->
            entityManager.createNativeQuery("TRUNCATE TABLE \"$table\" CASCADE").executeUpdate()
        }
    }
}
