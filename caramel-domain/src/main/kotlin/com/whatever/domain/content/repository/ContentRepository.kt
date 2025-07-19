package com.whatever.domain.content.repository

import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.whatever.caramel.common.util.CursorUtil
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.vo.ContentType
import com.whatever.domain.content.tag.model.Tag
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.vo.ContentListSortType
import com.whatever.domain.content.vo.ContentQueryVo
import com.whatever.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

interface ContentRepository : JpaRepository<Content, Long>, ContentRepositoryCustom {

    @Query("SELECT c FROM Content c WHERE c.id = :id AND c.type = :type AND c.isDeleted = false")
    fun findContentByIdAndType(id: Long, type: ContentType): Content?

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update Content c
        set c.isDeleted = true
        where c.user.id = :userId
            and c.isDeleted = false
    """
    )
    fun softDeleteAllByUserIdInBulk(userId: Long): Int
}

interface ContentRepositoryCustom {
    fun findByTypeWithCursor(
        type: ContentType,
        queryParameter: ContentQueryVo,
        memberIds: List<Long>,
        tagId: Long?,
    ): List<Content>
}

@Repository
class ContentRepositoryCustomImpl(
    private val jpqlExecutor: KotlinJdslJpqlExecutor,
) : ContentRepositoryCustom {

    override fun findByTypeWithCursor(
        type: ContentType,
        queryParameter: ContentQueryVo,
        memberIds: List<Long>,
        tagId: Long?,
    ): List<Content> {
        return jpqlExecutor.findAll(queryParameter.toPageable()) {
            select(
                entity(Content::class)
            ).from(
                entity(Content::class),
                tagId?.let {
                    join(entity(TagContentMapping::class))
                        .on(path(TagContentMapping::content)(Content::id).equal(path(Content::id)))
                }
            ).whereAnd(
                path(Content::type).equal(type),
                path(Content::isDeleted).equal(false),
                path(Content::user)(User::id).`in`(memberIds),
                tagId?.let { path(TagContentMapping::tag)(Tag::id).equal(it) },
                applyCursor(queryParameter),
            )
        }.filterNotNull()
    }
}

private fun Jpql.applyCursor(
    queryParameter: ContentQueryVo,
): Predicate? {
    val cursor = queryParameter.cursor
    if (cursor.isNullOrBlank()) return null

    val cursors = CursorUtil.fromHash(cursor)
    return when (queryParameter.sortType) {
        ContentListSortType.ID_DESC -> {
            val cursorId = cursors.firstOrNull()?.toLongOrNull() ?: return null
            path(Content::id).lessThan(cursorId)
        }
    }
}
