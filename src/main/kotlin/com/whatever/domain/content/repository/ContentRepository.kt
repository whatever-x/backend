package com.whatever.domain.content.repository

import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.whatever.domain.content.controller.dto.request.ContentListSortType
import com.whatever.domain.content.controller.dto.request.GetContentListQueryParameter
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentType
import com.whatever.util.CursorUtil
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

interface ContentRepository : JpaRepository<Content, Long>, ContentRepositoryCustom {

    @Query("SELECT c FROM Content c WHERE c.id = :id AND c.type = :type AND c.isDeleted = false")
    fun findContentByIdAndType(id: Long, type: ContentType): Content?
}

interface ContentRepositoryCustom {
    fun findByTypeWithCursor(
        type: ContentType,
        queryParameter: GetContentListQueryParameter
    ): List<Content>
}

@Repository
class ContentRepositoryCustomImpl(
    private val jpqlExecutor: KotlinJdslJpqlExecutor
) : ContentRepositoryCustom {

    override fun findByTypeWithCursor(
        type: ContentType,
        queryParameter: GetContentListQueryParameter
    ): List<Content> {
        return jpqlExecutor.findAll(queryParameter.toPageable()) {
            select(
                entity(Content::class)
            ).from(
                entity(Content::class)
            ).whereAnd(
                path(Content::type).equal(type),
                path(Content::isDeleted).equal(false),
                applyCursor(queryParameter)
            )
        }.filterNotNull()
    }
}

private fun Jpql.applyCursor(
    queryParameter: GetContentListQueryParameter
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