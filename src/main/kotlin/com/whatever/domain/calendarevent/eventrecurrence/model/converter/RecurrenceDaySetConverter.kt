package com.whatever.domain.calendarevent.eventrecurrence.model.converter

import com.whatever.domain.calendarevent.eventrecurrence.model.RecurrenceDay
import com.whatever.domain.calendarevent.eventrecurrence.model.WeekDay
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class RecurrenceDaySetConverter : AttributeConverter<Set<RecurrenceDay>, String> {

    companion object {
        const val SEPARATOR = ","
        private val regex = Regex("""^(\d+)?([A-Z]+)$""")  // MO | 1SU | ...
    }

    override fun convertToDatabaseColumn(attribute: Set<RecurrenceDay>): String? {
        if (attribute.isEmpty())
            return null
        return attribute.joinToString(separator = SEPARATOR) { it.toString() }
    }

    override fun convertToEntityAttribute(dbData: String?): Set<RecurrenceDay> {
        if (dbData == null)
            return emptySet()

        return dbData.split(SEPARATOR).mapNotNull { rawRecurrenceDay ->
            val matchResult = regex.matchEntire(rawRecurrenceDay)
                ?: return@mapNotNull null

            val (ordinalStr, dayStr) = matchResult.destructured

            val ordinal = ordinalStr.toIntOrNull()
            val day = try {
                WeekDay.valueOf(dayStr)
            } catch (e: IllegalArgumentException) {
                return@mapNotNull null
            }

            RecurrenceDay(
                ordinal = ordinal,
                day = day
            )
        }.toSet()
    }
}