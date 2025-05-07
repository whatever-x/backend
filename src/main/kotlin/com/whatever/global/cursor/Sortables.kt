package com.whatever.global.cursor

import org.springframework.data.domain.Sort

interface Sortables {
    val sortables: List<Sortable>

    fun toSort() = sortables.map { it.toOrder() }.let { Sort.by(it) }
}

interface Sortable {
    val property: String
    val direction: Sort.Direction

    fun toOrder(): Sort.Order {
        return Sort.Order(direction, property)
    }
}

data class AscOrder(
    override val property: String,
) : Sortable {
    override val direction = Sort.Direction.ASC
}

data class DescOrder(
    override val property: String,
) : Sortable {
    override val direction = Sort.Direction.DESC
}