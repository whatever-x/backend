package com.whatever.domain.calendarevent.model.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.ZoneId

@Converter
class ZonedIdConverter : AttributeConverter<ZoneId, String> {
    override fun convertToDatabaseColumn(attribute: ZoneId?): String? {
        return attribute?.id
    }

    override fun convertToEntityAttribute(dbData: String?): ZoneId? {
        return dbData?.let { ZoneId.of(it) }
    }
}
