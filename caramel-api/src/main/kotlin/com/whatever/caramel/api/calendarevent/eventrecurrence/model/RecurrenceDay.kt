package com.whatever.caramel.api.calendarevent.eventrecurrence.model

data class RecurrenceDay(
    val day: WeekDay,
    val ordinal: Int? = null,
) {
    override fun toString(): String {
        return if (ordinal == null) day.name else "${ordinal}${day.name}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecurrenceDay) return false
        return day == other.day
    }

    override fun hashCode(): Int {
        return day.hashCode()
    }
}
