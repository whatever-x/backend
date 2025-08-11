package com.whatever.caramel.batch.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.DatabaseType
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class BatchConfig {
    @Bean
    @BatchTransactionManager
    fun batchTransactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }

    @Bean
    fun whatEverJobRepository(
        dataSource: DataSource,
        transactionManager: PlatformTransactionManager,
    ): JobRepository {
        return JobRepositoryFactoryBean().apply {
            setDataSource(dataSource)
            setDatabaseType(DatabaseType.POSTGRES.name)
            setTransactionManager(transactionManager)
            afterPropertiesSet()
        }.`object`
    }
}
