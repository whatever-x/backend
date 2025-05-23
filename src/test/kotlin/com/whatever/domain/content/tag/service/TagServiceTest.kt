package com.whatever.domain.content.tag.service

import com.whatever.domain.content.tag.model.Tag
import com.whatever.domain.content.tag.repository.TagRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class TagServiceTest @Autowired constructor(
    private val tagService: TagService,
    private val tagRepository: TagRepository,
) {

    @AfterEach
    fun tearDown() {
        tagRepository.deleteAllInBatch()
    }

    @DisplayName("삭제되지 않은 모든 태그들이 조회된다.")
    @Test
    fun getTags() {
        // given
        val tagCnt = 40
        val allCreatedTags = (1..tagCnt).mapNotNull { idx ->
            createTag("test${idx}")
        }
        allCreatedTags.filter { (it.id % 2).toInt() == 0 }.forEach {  // 짝수 id를 가진 태그만 제거
            it.deleteEntity()
            tagRepository.save(it)
        }
        val activeTags = allCreatedTags.filter { (it.id % 2).toInt() != 0 }


        // when
        val result = tagService.getTags()

        // then
        assertThat(result.tagList.map { it.tagId })
            .containsExactlyElementsOf(activeTags.map { it.id })
    }

    @DisplayName("등록된 태그가 없을경우 빈 리스트가 반환된다.")
    @Test
    fun getTags_WithEmptyList() {
        // given, when
        val result = tagService.getTags()

        // then
        assertThat(result.tagList).isEmpty()
    }

    private fun createTag(label: String): Tag {
        return tagRepository.save(Tag(label = label))
    }

}