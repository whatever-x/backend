package com.whatever.caramel.domain.couple.service.event

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.balancegame.service.event.UserChoiceOptionCleanupService
import com.whatever.caramel.domain.balancegame.service.event.UserChoiceOptionEventListener
import com.whatever.caramel.domain.calendarevent.service.event.ScheduleEventCleanupService
import com.whatever.caramel.domain.calendarevent.service.event.ScheduleEventListener
import com.whatever.caramel.domain.config.AsyncConfig
import com.whatever.caramel.domain.content.service.event.ContentCleanupService
import com.whatever.caramel.domain.content.service.event.ContentEventListener
import com.whatever.caramel.domain.content.tag.service.event.TagContentMappingCleanupService
import com.whatever.caramel.domain.content.tag.service.event.TagContentMappingEventListener
import com.whatever.caramel.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@CaramelDomainSpringBootTest
class CoupleMemberLeaveEventPublishTest @Autowired constructor(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ExcludeAsyncConfigBean() {

    @MockitoBean
    private lateinit var scheduleEventCleanupService: ScheduleEventCleanupService

    @MockitoBean
    private lateinit var contentCleanupService: ContentCleanupService

    @MockitoBean
    private lateinit var tagContentMappingCleanupService: TagContentMappingCleanupService

    @MockitoBean
    private lateinit var userChoiceOptionCleanupService: UserChoiceOptionCleanupService

    @DisplayName("CoupleMemberLeaveEvent가 발행되고, commit이 완료되면 각 도메인의 cleanup 서비스가 실행된다.")
    @Test
    @Transactional
    fun publishCoupleMemberLeaveEvent() {
        // given
        val coupleId = 0L
        val userId = 0L
        val leaveEvent = CoupleMemberLeaveEvent(coupleId, userId)

        // when
        applicationEventPublisher.publishEvent(leaveEvent)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        // then
        verify(scheduleEventCleanupService, times(1))
            .cleanupEntity(userId, ScheduleEventListener.ENTITY_NAME)
        verify(contentCleanupService, times(1))
            .cleanupEntity(userId, ContentEventListener.ENTITY_NAME)
        verify(tagContentMappingCleanupService, times(1))
            .cleanupEntity(userId, TagContentMappingEventListener.ENTITY_NAME)
        verify(userChoiceOptionCleanupService, times(1))
            .cleanupEntity(userId, UserChoiceOptionEventListener.ENTITY_NAME)
    }

    @DisplayName("CoupleMemberLeaveEvent가 발행되고, rollback이 진행된다면 cleanup을 진행하지 않는다.")
    @Test
    @Transactional
    fun publishCoupleMemberLeaveEvent_WithRollback() {
        // given
        val coupleId = 0L
        val userId = 0L
        val leaveEvent = CoupleMemberLeaveEvent(coupleId, userId)

        // when
        applicationEventPublisher.publishEvent(leaveEvent)
        TestTransaction.flagForRollback()
        TestTransaction.end()

        // then
        verify(scheduleEventCleanupService, never())
            .cleanupEntity(userId, ScheduleEventListener.ENTITY_NAME)
        verify(contentCleanupService, never())
            .cleanupEntity(userId, ContentEventListener.ENTITY_NAME)
        verify(tagContentMappingCleanupService, never())
            .cleanupEntity(userId, TagContentMappingEventListener.ENTITY_NAME)
        verify(userChoiceOptionCleanupService, never())
            .cleanupEntity(userId, UserChoiceOptionEventListener.ENTITY_NAME)
    }
}

@Profile("test")
@TestConfiguration
class SyncAsyncConfig {
    @Bean("taskExecutor")
    fun syncTaskExecutor(): TaskExecutor {
        return SyncTaskExecutor()
    }
}

@Import(SyncAsyncConfig::class)
abstract class ExcludeAsyncConfigBean {  // AsyncConfig를 비활성화 하기위해 Mocking
    @MockitoBean
    private lateinit var asyncConfig: AsyncConfig
}
