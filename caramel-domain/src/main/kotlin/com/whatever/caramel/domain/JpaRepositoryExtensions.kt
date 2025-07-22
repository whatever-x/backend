package com.whatever.caramel.domain

import com.whatever.caramel.domain.base.BaseEntity
import org.springframework.data.jpa.repository.JpaRepository

fun <T : BaseEntity, ID : Any> JpaRepository<T, ID>.findByIdAndNotDeleted(id: ID): T? {
    return findById(id).orElse(null)?.takeUnless { it.isDeleted }
}
