package com.whatever.caramel.batch.config

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.batch.support.DatabaseType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime
import java.time.ZoneId
import javax.sql.DataSource

@Configuration
class FcmBatchConfig(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val firebaseService: FirebaseService,
) {
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

    // 겨우 이거떄문에 의존성 가져가야하는가?
    @Bean
    fun userItemReader(
        date: LocalDateTime = DateTimeUtil.localNow(TARGET_ZONE_ID),
    ): ItemReader<ScheduledNotification> {
        val scheduleList = scheduledNotificationService.getMatchedScheduledNotifications(date)
        return ListItemReader(scheduleList)
    }

    @Bean
    fun userItemWriter(): ItemWriter<ScheduledNotification> {
        return ItemWriter { /*no-op*/ }
    }

    @Bean
    fun compositeItemProcessor(): ItemProcessor<ScheduledNotification, ScheduledNotification> {
        return ItemProcessor<ScheduledNotification, ScheduledNotification> {
            val fcmNotification = FcmNotification(
                title = it.title,
                body = it.body,
            )
            firebaseService.sendNotification(
                setOf(it.targetUserId),
                fcmNotification
            )
            it
        }
    }

    @Bean
    fun step(
        whatEverJobRepository: JobRepository,
        @Qualifier("batchTransactionManager") batchTransactionManager: PlatformTransactionManager,
        itemReader: ItemReader<ScheduledNotification>,
        compositeItemProcessor: ItemProcessor<ScheduledNotification, ScheduledNotification>,
        itemWriter: ItemWriter<ScheduledNotification>,
    ): Step {
        return StepBuilder("step", whatEverJobRepository)
            .chunk<ScheduledNotification, ScheduledNotification>(10, batchTransactionManager)
            .reader(itemReader)
            .processor(compositeItemProcessor)
            .writer(itemWriter)
            .allowStartIfComplete(true)
            .build()
    }

    @Bean
    fun job(jobRepository: JobRepository, step: Step): Job {
        return JobBuilder("anniversary", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step)
            .build()
    }

    @Bean
    fun jobExecutionListener(): JobExecutionListener {
        return object : JobExecutionListener {
            override fun beforeJob(jobExecution: JobExecution) {
                super.beforeJob(jobExecution)
            }

            override fun afterJob(jobExecution: JobExecution) {
                if (jobExecution.status == BatchStatus.COMPLETED) {
                    println(" 배치 성공했으니 디비 전부 제거같은 것 수행도 가능")
                }
            }
        }
    }

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
