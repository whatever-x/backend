package com.whatever.domain.calendarevent.specialday.client.dto.response

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class HolidayApiResponse(
    val response: HolidayResponse?,
)

data class HolidayResponse(
    val header: HolidayHeader,
    val body: HolidayBody,
)

data class HolidayHeader(
    val resultCode: String,
    val resultMsg: String,
)

data class HolidayBody(
    val items: HolidayItemsContainer,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int,
)

data class HolidayItemsContainer(
    val item: List<HolidayItem> = arrayListOf(),
)

data class HolidayItem(
    val dateKind: String,  // ex) "01"
    val dateName: String,  // ex) "어린이날"
    val isHoliday: String,  // ex) "Y" or "N"
    @JsonDeserialize(using = BasicIsoDateDeserializer::class)
    val locdate: LocalDate,  // ex) 20250505 (YYYYMMDD format)
    val seq: Int,  // ex) 1
)

private class BasicIsoDateDeserializer : JsonDeserializer<LocalDate>() {
    companion object {
        private val FORMATTER = DateTimeFormatter.BASIC_ISO_DATE
    }

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate? {
        if (parser.text.isNullOrBlank()) {
            return null
        }

        return try {
            LocalDate.parse(parser.text, FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}