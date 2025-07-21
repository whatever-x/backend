package com.whatever.domain.content.service.event

import com.whatever.CaramelDomainSpringBootTest
import com.whatever.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.domain.calendarevent.scheduleevent.service.event.createSchedules
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.couple.service.makeCouple
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@CaramelDomainSpringBootTest
class ContentCleanupServiceTest @Autowired constructor(
    private val contentCleanupService: ContentCleanupService,
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

    @DisplayName("ContentCleanupService.cleanupEntity() 호출 시 특정 사용자의 콘텐츠가 모두 제거된다.")
    @Test
    fun cleanupContent() {
        // given
        val (myUser, partnerUser, _) = makeCouple(userRepository, coupleRepository)

        val myDataSize = 20
        val mySchedules = createSchedules(
            scheduleEventRepository,
            contentRepository,
            myUser,
            myDataSize
        )  // Schedule, Content(Schedule)
        val myMemos = createMemos(contentRepository, myUser, myDataSize)  // Content(Memo)

        val partnerDataSize = 10
        val partnerMemos = createMemos(contentRepository, partnerUser, partnerDataSize)

        // when
        val deletedEntityCnt = contentCleanupService.cleanupEntity(
            userId = myUser.id,
            entityName = ContentEventListener.ENTITY_NAME
        )

        // then
        assertThat(deletedEntityCnt).isEqualTo(mySchedules.size + myMemos.size)

        val remainingContentIds = contentRepository.findAll().filter { !it.isDeleted }.map { it.id }
        assertThat(remainingContentIds).containsExactlyInAnyOrderElementsOf(partnerMemos.map { it.id })

        val remainingScheduleIds = scheduleEventRepository.findAll().filter { !it.isDeleted }.map { it.id }
        assertThat(remainingScheduleIds).containsExactlyInAnyOrderElementsOf(mySchedules.map { it.id })
    }
}

fun createMemos(contentRepository: ContentRepository, user: User, count: Int): List<Content> {
    if (count == 0) return emptyList()
    val memosToSave = mutableListOf<Content>()
    for (i in 1..count) {
        val contentDetail = ContentDetail(title = "Test Memo Title $i", description = "Test Memo Text $i")
        memosToSave.add(
            Content(
                user = user,
                contentDetail = contentDetail
            )
        )
    }
    return contentRepository.saveAll(memosToSave)
}
