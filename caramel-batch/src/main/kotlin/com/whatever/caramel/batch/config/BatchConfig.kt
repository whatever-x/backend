package com.whatever.caramel.batch.config

import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.DatabaseType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class BatchConfig {
    @Bean("batchTransactionManager")
    fun batchTransactionManager(dataSource: DataSource): PlatformTransactionManager {
        return JdbcTransactionManager(dataSource)
    }

    @Bean
    fun whatEverJobRepository(
        dataSource: DataSource,
        @Qualifier("batchTransactionManager") batchTransactionManager: PlatformTransactionManager,
    ): JobRepository {
        return JobRepositoryFactoryBean().apply {
            setDataSource(dataSource)
            setDatabaseType(DatabaseType.POSTGRES.name)
            transactionManager = batchTransactionManager
            afterPropertiesSet()
        }.`object`
    }
}
