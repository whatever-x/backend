package com.whatever.caramel.batch.config

import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.DatabaseType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class BatchConfig {

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
