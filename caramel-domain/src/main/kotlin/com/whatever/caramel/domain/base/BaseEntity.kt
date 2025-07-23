package com.whatever.caramel.domain.base

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseEntity : BaseTimeEntity() {

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    fun deleteEntity() {
        this.isDeleted = true
    }

    fun restoreEntity() {
        this.isDeleted = false
    }
}
