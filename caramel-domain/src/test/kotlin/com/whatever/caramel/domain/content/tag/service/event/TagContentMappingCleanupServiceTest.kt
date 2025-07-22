package com.whatever.caramel.domain.content.tag.service.event

import com.whatever.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.calendarevent.service.event.ScheduleEventListener
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.service.event.createMemos
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.couple.service.makeCouple
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@CaramelDomainSpringBootTest
class TagContentMappingCleanupServiceTest @Autowired constructor(
    private val tagContentMappingCleanupService: TagContentMappingCleanupService,
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
) {

    @AfterEach
    fun tearDown() {
        tagContentMappingRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("TagContentMappingCleanupService.cleanupEntity() 호출 시 특정 사용자의 TagContentMapping이 모두 제거된다.")
    @Test
    fun cleanupTagContentMapping() {
        // given
        val (myUser, partnerUser, _) = makeCouple(userRepository, coupleRepository)

        val tagSize = 20
        val tags = createTags(tagRepository, tagSize)

        val myDataSize = 20
        val myMemos = createMemos(contentRepository, myUser, myDataSize)
        val myMappings = createTagContentMappings(tagContentMappingRepository, tags, myMemos)

        val partnerDataSize = 10
        val partnerMemos = createMemos(contentRepository, partnerUser, partnerDataSize)
        val partnerMappings = createTagContentMappings(tagContentMappingRepository, tags, partnerMemos)

        // when
        val deletedEntityCnt = tagContentMappingCleanupService.cleanupEntity(
            userId = myUser.id,
            entityName = ScheduleEventListener.ENTITY_NAME
        )

        // then
        assertThat(deletedEntityCnt).isEqualTo(myMappings.size)

        val remainingMappingIds = tagContentMappingRepository.findAll().filter { !it.isDeleted }.map { it.id }
        assertThat(remainingMappingIds).containsExactlyInAnyOrderElementsOf(partnerMappings.map { it.id })
    }
}

fun createTags(tagRepository: TagRepository, count: Int): List<Tag> {
    if (count == 0) return emptyList()
    val tagsToSave = mutableListOf<Tag>()
    for (i in 1..count) {
        tagsToSave.add(Tag(label = "Test Tag $i"))
    }
    return tagRepository.saveAll(tagsToSave)
}

/**
 * 각 content에 tags를 모두 할당하는 메서드
 */
fun createTagContentMappings(
    tagContentMappingRepository: TagContentMappingRepository,
    tags: List<Tag>,
    contents: List<Content>,
): List<TagContentMapping> {
    if (tags.isEmpty() || contents.isEmpty()) return emptyList()
    val mappingsToSave = mutableListOf<TagContentMapping>()
    contents.forEach { content ->
        val mappings = tags.map {
            TagContentMapping(
                tag = it,
                content = content
            )
        }
        mappingsToSave.addAll(mappings)
    }
    return tagContentMappingRepository.saveAll(mappingsToSave)
}
