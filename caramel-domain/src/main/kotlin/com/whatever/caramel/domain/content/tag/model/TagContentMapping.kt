package com.whatever.caramel.domain.content.tag.model

import com.whatever.caramel.domain.base.BaseEntity
import com.whatever.caramel.domain.content.model.Content
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "tag_content_mapping_unique_idx_content_id_and_tag_id_when_not_deleted",
            columnNames = ["content_id", "tag_id"]
        )
    ],
)
class TagContentMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)
    val tag: Tag,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false)
    val content: Content,
) : BaseEntity() {
}
