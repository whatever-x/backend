package com.whatever.caramel.domain.content.service

import com.whatever.CaramelDomainSpringBootTest
import com.whatever.caramel.common.global.cursor.Cursor
import com.whatever.caramel.common.util.CursorUtil
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.vo.ContentListSortType
import com.whatever.caramel.domain.content.vo.ContentQueryVo
import com.whatever.caramel.domain.content.vo.ContentResponseVo
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.UserStatus
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@CaramelDomainSpringBootTest
class ContentServicePaginationTest @Autowired constructor(
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val contentService: ContentService,
    private val tagRepository: TagRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
) {
    @AfterEach
    fun tearDown() {
        tagContentMappingRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("메모 목록 조회 시 커서를 사용하여 모든 페이지를 순차적으로 조회하고 마지막 페이지를 확인한다")
    @Test
    fun getContentList_Pagination_FetchAllPagesSequentially() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val totalItems = 11
        val pageSize = 4

        val allExpectedMemos = (1..totalItems).map {
            val owner = if (it % 2 == 0) myUser else partnerUser
            contentRepository.save(createContent(owner, ContentType.MEMO, "Memo $it"))
        }.sortedByDescending { it.id }

        val allFetchedContents = mutableListOf<ContentResponseVo>() // 조회된 모든 컨텐츠를 저장할 리스트
        var currentCursor: String? = null
        var pagesFetched = 0
        val maxPages = (totalItems + pageSize - 1) / pageSize // 예상되는 최대 페이지 수 (올림 계산)

        do {
            pagesFetched++
            // 예상보다 많은 페이지가 조회되면 테스트 중단
            if (pagesFetched > maxPages + 1) {
                throw IllegalStateException("예상보다 많은 페이지($pagesFetched)를 조회했습니다. 커서 로직 확인 필요.")
            }

            val queryParameter = ContentQueryVo(
                size = pageSize,
                cursor = currentCursor,
                sortType = ContentListSortType.ID_DESC,
                tagId = null
            )
            val response = contentService.getContentList(queryParameter, couple.id)

            allFetchedContents.addAll(response.list)
            currentCursor = response.cursor.next

            if (currentCursor != null) {
                assertThat(response.list).hasSize(pageSize)
                assertThat(response.cursor.next).isNotNull()
                assertThat(response.cursor.next).isEqualTo(CursorUtil.toHash(response.list.last().id))
            } else {
                // 마지막 페이지라면, 남은 아이템 수만큼 조회되어야 함
                val expectedLastPageSize = totalItems % pageSize
                assertThat(response.list).hasSize(expectedLastPageSize)
                assertThat(response.cursor.next as String?).isNull()
            }
        } while (currentCursor != null)

        assertThat(pagesFetched).isEqualTo(maxPages)
        assertThat(allFetchedContents).hasSize(totalItems)

        assertThat(allFetchedContents.map { it.id }).containsExactlyElementsOf(allExpectedMemos.map { it.id })
    }

    @DisplayName("메모 목록 조회 시 해당 커플의 메모가 없으면 빈 리스트를 반환한다")
    @Test
    fun getContentList_EmptyResult() {
        // given
        val (myUser, _, couple) = setUpCouple()

        contentRepository.save(createContent(myUser, ContentType.SCHEDULE, "Schedule Title 1"))

        val queryParameter = ContentQueryVo(
            size = 10,
            cursor = null,
            sortType = ContentListSortType.ID_DESC,
            tagId = null,
        )

        // when
        val result = contentService.getContentList(queryParameter, couple.id)

        // then
        assertThat(result.list).isEmpty()
        assertThat(result.cursor).isEqualTo(Cursor(null))
    }

    @DisplayName("메모 목록 조회 시 삭제된 컨텐츠는 조회되지 않는다")
    @Test
    fun getContentList_ExcludesDeletedContent() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()

        val memo1 = contentRepository.save(createContent(myUser, ContentType.MEMO, "Memo 1"))
        val memo2 = contentRepository.save(createContent(partnerUser, ContentType.MEMO, "Memo 2"))
        val memo3 = contentRepository.save(createContent(myUser, ContentType.MEMO, "Memo 3"))

        memo2.deleteEntity()
        contentRepository.save(memo2)

        val expectedContents = listOf(memo3, memo1).sortedByDescending { it.id }

        val queryParameter = ContentQueryVo(
            size = 10,
            cursor = null,
            sortType = ContentListSortType.ID_DESC,
            tagId = null,
        )

        // when
        val result = contentService.getContentList(queryParameter, couple.id)

        // then
        assertThat(result.list).hasSize(expectedContents.size)
        assertThat(result.list.map { it.id }).containsExactlyElementsOf(expectedContents.map { it.id })
        assertThat(result.list.map { it.id }).doesNotContain(memo2.id)
        assertThat(result.cursor).isEqualTo(Cursor(null))
    }

    private fun setUpCouple(
        myPlatformId: String = "me",
        partnerPlatformId: String = "partner",
    ): Triple<com.whatever.caramel.domain.user.model.User, com.whatever.caramel.domain.user.model.User, Couple> {
        val (myUser, partnerUser, couple) = createCouple(
            userRepository,
            coupleRepository,
            myPlatformId,
            partnerPlatformId
        )
        return Triple(myUser, partnerUser, couple)
    }

    @DisplayName("메모 목록 조회 시 tagId 가 주어지면 해당 태그가 포함된 컨텐츠만 내려온다")
    @Test
    fun getContentList_WithTagId_FiltersByTag() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val contentA = contentRepository.save(createContent(myUser, ContentType.MEMO, "A"))
        val contentB = contentRepository.save(createContent(myUser, ContentType.MEMO, "B"))

        val tag1 = tagRepository.save(Tag(label = "Tag1"))
        val tag2 = tagRepository.save(Tag(label = "Tag2"))

        tagContentMappingRepository.save(TagContentMapping(tag = tag1, content = contentA))
        tagContentMappingRepository.save(TagContentMapping(tag = tag2, content = contentB))

        val queryParameter = ContentQueryVo(
            size = 10,
            cursor = null,
            sortType = ContentListSortType.ID_DESC,
            tagId = tag1.id,
        )

        // when
        val result = contentService.getContentList(queryParameter, couple.id)

        // then
        assertThat(result.list).hasSize(1)
        assertThat(result.list.first().id).isEqualTo(contentA.id)
    }

    @DisplayName("메모 목록 조회 시 각 컨텐츠에 해당하는 태그들도 같이 내려온다")
    @Test
    fun getContentList_IncludesTags() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val content = contentRepository.save(createContent(myUser, ContentType.MEMO, "C"))

        val tag1 = tagRepository.save(Tag(label = "Tag1"))
        val tag2 = tagRepository.save(Tag(label = "Tag2"))

        tagContentMappingRepository.save(TagContentMapping(tag = tag1, content = content))
        tagContentMappingRepository.save(TagContentMapping(tag = tag2, content = content))

        val queryParameter = ContentQueryVo(
            size = 10,
            cursor = null,
            sortType = ContentListSortType.ID_DESC,
            tagId = tag1.id,
        )

        // when
        val result = contentService.getContentList(queryParameter, couple.id)

        // then
        val response = result.list.find { it.id == content.id }!!
        assertThat(response.tagList).hasSize(2)
        assertThat(response.tagList.map { it.id }).containsExactlyInAnyOrder(tag1.id, tag2.id)
        assertThat(response.tagList.map { it.label }).containsExactlyInAnyOrder(tag1.label, tag2.label)
    }
}

internal fun createCouple(
    userRepository: UserRepository,
    coupleRepository: CoupleRepository,
    myPlatformUserId: String = "me",
    partnerPlatformUserId: String = "partner",
): Triple<com.whatever.caramel.domain.user.model.User, com.whatever.caramel.domain.user.model.User, Couple> {
    val myUser = userRepository.save(createUser("my", myPlatformUserId, UserStatus.SINGLE))
    val partnerUser = userRepository.save(createUser("partner", partnerPlatformUserId, UserStatus.SINGLE))

    val startDate = DateTimeUtil.localNow().toLocalDate()
    val savedCouple = coupleRepository.save(
        Couple(
            startDate = startDate,
            sharedMessage = "test message for ${myUser.nickname}"
        )
    )
    savedCouple.addMembers(myUser, partnerUser)

    userRepository.save(myUser)
    userRepository.save(partnerUser)
    return Triple(myUser, partnerUser, savedCouple)
}

internal fun createUser(
    nickname: String,
    platformUserId: String,
    userStatus: UserStatus = UserStatus.COUPLED,
): com.whatever.caramel.domain.user.model.User {
    return com.whatever.caramel.domain.user.model.User(
        nickname = nickname,
        birthDate = DateTimeUtil.localNow().toLocalDate().minusYears(25),
        platform = LoginPlatform.KAKAO,
        platformUserId = platformUserId,
        userStatus = userStatus
    )
}

internal fun createContent(
    user: com.whatever.caramel.domain.user.model.User,
    type: ContentType,
    title: String = "Default Title",
    description: String = "Default Description",
): Content {
    return Content(
        user = user,
        contentDetail = ContentDetail(
            title = title,
            description = description,
            isCompleted = false
        ),
        type = type
    )
}
