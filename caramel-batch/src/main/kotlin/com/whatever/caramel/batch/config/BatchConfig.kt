package com.whatever.caramel.batch.config

import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.repository.UserRepository
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import org.springframework.batch.core.Job
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.support.JdbcTransactionManager
import javax.sql.DataSource

@Configuration
class BatchConfig(
    // user repository는 임시로, 추후에 ScheduledNotification 테이블에대한 repository 생기면 추가해줄것
    private val userRepository: UserRepository,
    private val firebaseService: FirebaseService,
) {
    @Bean
    fun transactionManager(dataSource: DataSource): JdbcTransactionManager {
        return JdbcTransactionManager(dataSource)
    }

    @Bean
    fun whatEverJobRepository(dataSource: DataSource, transactionManager: JdbcTransactionManager): JobRepository {
        return JobRepositoryFactoryBean().apply {
            setDataSource(dataSource)
            setDatabaseType(DatabaseType.POSTGRES.name)
            setTransactionManager(transactionManager)
            afterPropertiesSet()
        }.`object`
    }

    @Bean
    fun userItemReader(): ItemReader<User> {
        val user = listOf(User(platform = LoginPlatform.KAKAO, platformUserId = "k"))
        // 여기서 repository 에서 리스트 요소 조회
        return ListItemReader(user)
    }

    @Bean
    fun userItemWriter(): ItemWriter<User> {
        return ItemWriter {
            userRepository.saveAll(it.items)
        }
    }

    @Bean
    fun compositeItemProcessor(): ItemProcessor<User, User> {
        return ItemProcessor<User, User> {
            val fcmNotification = FcmNotification(
                title = "배치 축하",
                body = "연인이 새로운 배치를 등록했어요!",
            )

            firebaseService.sendNotification(
                setOf(it.id),
                fcmNotification
            )
            it
        }
    }

    @Bean
    fun step(
        whatEverJobRepository: JobRepository,
        transactionManager: JdbcTransactionManager,
        itemReader: ItemReader<User>,
        compositeItemProcessor: ItemProcessor<User, User>,
        itemWriter: ItemWriter<User>,
    ): Step {
        return StepBuilder("step", whatEverJobRepository)
            .chunk<User, User>(10, transactionManager)
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
}
