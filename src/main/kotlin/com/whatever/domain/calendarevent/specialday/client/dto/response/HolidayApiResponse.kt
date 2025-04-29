package com.whatever.domain.calendarevent.specialday.client.dto.response

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
    @JsonDeserialize(using = EmptyStringToEmptyListHolidayItemsContainerDeserializer::class)
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

private class BasicIsoDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
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

private class EmptyStringToEmptyListHolidayItemsContainerDeserializer : StdDeserializer<HolidayItemsContainer>(HolidayItemsContainer::class.java) {

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): HolidayItemsContainer {
        if (parser.currentToken == JsonToken.VALUE_STRING && parser.text.isNullOrBlank()) {
            return HolidayItemsContainer(item = emptyList())
        }
        if (parser !is ObjectMapper) {
            return parser.codec.readValue(parser, HolidayItemsContainer::class.java)
        }

        val objectMapper = parser.codec as ObjectMapper
        return objectMapper.readValue<HolidayItemsContainer>(parser)
    }
}
