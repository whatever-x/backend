package com.whatever.caramel.batch.config

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.DatabaseType
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class BatchJpaConfig {
    @Bean
    @BatchDataSource
    @ConfigurationProperties("spring.datasource")
    fun batchDataSource(): DataSource {
        return HikariDataSource()
    }

    @Bean
    fun entityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        batchDataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(batchDataSource)
            .packages(
                "com.whatever.caramel.batch",
                "com.whatever.caramel.domain.notification.model",
                "com.whatever.caramel.domain.couple.model",
                "com.whatever.caramel.domain.user.model",
                "com.whatever.caramel.domain.firebase.model",
            )
            .persistenceUnit("batch")
            .build()
    }

    @Bean
    @BatchTransactionManager
    fun transactionManager(
        entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }

    @Bean
    fun whatEverJobRepository(
        @BatchDataSource batchDataSource: DataSource,
        @BatchTransactionManager transactionManager: PlatformTransactionManager
    ): JobRepository {
        return JobRepositoryFactoryBean().apply {
            setDataSource(batchDataSource)
            this.transactionManager = transactionManager
            setDatabaseType(DatabaseType.POSTGRES.name)
            afterPropertiesSet()
        }.`object`
    }
}
