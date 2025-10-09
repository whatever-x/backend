package com.whatever.caramel.batch.config

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.DatabaseType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
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
    @ConfigurationProperties(prefix = "spring.datasource.batch")
    fun batchDataSource(): DataSource {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean
    fun whatEverJobRepository(
        batchDataSource: DataSource,
        transactionManager: PlatformTransactionManager
    ): JobRepository {
        return JobRepositoryFactoryBean().apply {
            setDataSource(batchDataSource)
            this.transactionManager = transactionManager
            setDatabaseType(DatabaseType.POSTGRES.name)
            afterPropertiesSet()
        }.`object`
    }
}
