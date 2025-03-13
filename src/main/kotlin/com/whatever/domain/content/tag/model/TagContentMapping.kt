package com.whatever.domain.content.tag.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.content.model.Content
import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["tag_id", "content_id"])
    ]
)
class TagContentMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(optional = false)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)
    val tag: Tag,

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false)
    val content: Content,
) : BaseEntity() {
}