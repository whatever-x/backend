package com.whatever.caramel.api.calendarevent.scheduleevent.service.event

import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.caramel.api.couple.service.makeCouple
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.caramel.common.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class ScheduleEventCleanupServiceTest @Autowired constructor(
    private val scheduleEventCleanupService: ScheduleEventCleanupService,
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val contentRepository: ContentRepository,
) {

    @AfterEach
    fun tearDown() {
        scheduleEventRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("ScheduleEventCleanupService.cleanupEntity() 호출 시 특정 사용자의 스케줄이 모두 제거된다.")
    @Test
    fun cleanupSchedule() {
        // given
        val (myUser, partnerUser, _) = makeCouple(userRepository, coupleRepository)

        val myDataSize = 20
        createSchedules(scheduleEventRepository, contentRepository, myUser, myDataSize)
        val partnerDataSize = 10
        val partnerSchedule = createSchedules(scheduleEventRepository, contentRepository, partnerUser, partnerDataSize)

        // when
        val deletedEntityCnt = scheduleEventCleanupService.cleanupEntity(
            userId = myUser.id,
            entityName = ScheduleEventListener.ENTITY_NAME
        )

        // then
        assertThat(deletedEntityCnt).isEqualTo(myDataSize)

        val remainingScheduleIds = scheduleEventRepository.findAll().filter { !it.isDeleted }.map { it.id }
        assertThat(remainingScheduleIds).containsExactlyInAnyOrderElementsOf(partnerSchedule.map { it.id })
    }
}

fun createSchedules(
    scheduleEventRepository: ScheduleEventRepository,
    contentRepository: ContentRepository,
    user: User,
    count: Int,
): List<ScheduleEvent> {
    if (count == 0) return emptyList()
    val contentsToSave = mutableListOf<Content>()
    val now = DateTimeUtil.localNow()
    for (i in 1..count) {
        val contentDetail = ContentDetail(title = "Test Schedule Title $i", description = "Test Schedule Text $i")
        contentsToSave.add(
            Content(
                user = user,
                contentDetail = contentDetail,
                type = ContentType.SCHEDULE
            )
        )
    }
    val savedContents = contentRepository.saveAll(contentsToSave)

    val schedulesToSave = mutableListOf<ScheduleEvent>()
    savedContents.forEachIndexed { index, content ->
        schedulesToSave.add(
            ScheduleEvent(
                uid = UUID.randomUUID().toString(),
                startDateTime = now.plusHours(index.toLong() + 1), // ensure unique times if needed
                endDateTime = now.plusHours(index.toLong() + 2),
                startTimeZone = DateTimeUtil.UTC_ZONE_ID,
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = content
            )
        )
    }
    return scheduleEventRepository.saveAll(schedulesToSave)
}
