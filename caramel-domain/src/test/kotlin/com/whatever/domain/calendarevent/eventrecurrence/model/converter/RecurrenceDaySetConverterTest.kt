// package com.whatever.domain.calendarevent.eventrecurrence.model.converter
//
// import com.whatever.domain.calendarevent.eventrecurrence.model.RecurrenceDay
// import com.whatever.domain.calendarevent.eventrecurrence.model.WeekDay
// import org.assertj.core.api.Assertions.assertThat
// import org.junit.jupiter.api.DisplayName
// import org.junit.jupiter.api.Test
//
// class RecurrenceDaySetConverterTest {
//
//     val converter = RecurrenceDaySetConverter()
//
//     @DisplayName("RecurrenceDay 리스트를 String으로 변환한다.")
//     @Test
//     fun convertToDatabaseColumn() {
//         // given
//         val recurrenceDays = setOf(
//             RecurrenceDay(WeekDay.MO, 1),
//             RecurrenceDay(WeekDay.WE),
//             RecurrenceDay(WeekDay.FR)
//         )
//         val expectedResult = "1MO,WE,FR"
//
//         // when
//         val result = converter.convertToDatabaseColumn(recurrenceDays)
//
//         // then
//         assertThat(result).isEqualTo(expectedResult)
//     }
//
//     @DisplayName("빈 리스트는 null 로 변환된다.")
//     @Test
//     fun convertToDatabaseColumn_WithEmptyList() {
//         // given
//         val emptyList = emptySet<RecurrenceDay>()
//
//         // when
//         val result = converter.convertToDatabaseColumn(emptyList)
//
//         // then
//         assertThat(result).isNull()
//     }
//
//     @DisplayName("COMMA로 구분된 문자열을 RecurrenceDay 리스트로 변환한다.")
//     @Test
//     fun convertToEntityAttribute() {
//         // given
//         val strRecurrenceDay = "1SU,MO,TU,3WE"
//         val expectedResult = setOf(
//             RecurrenceDay(WeekDay.SU, 1),
//             RecurrenceDay(WeekDay.MO),
//             RecurrenceDay(WeekDay.TU),
//             RecurrenceDay(WeekDay.WE, 3),
//         )
//
//         // when
//         val result = converter.convertToEntityAttribute(strRecurrenceDay)
//
//         // then
//         assertThat(result).isEqualTo(expectedResult)
//     }
//
//     @DisplayName("양식에 어긋나는 문자열을 RecurrenceDay 리스트로 변환하면 제외된다.")
//     @Test
//     fun convertToEntityAttribute_WithIllegalFormat() {
//         // given
//         val strRecurrenceDay = "1SSU,11MO,TTU,WE"
//         val expectedResult = setOf(
//             RecurrenceDay(WeekDay.MO, 11),
//             RecurrenceDay(WeekDay.WE),
//         )
//
//         // when
//         val result = converter.convertToEntityAttribute(strRecurrenceDay)
//
//         // then
//         assertThat(result).isEqualTo(expectedResult)
//     }
//
//     @DisplayName("모두 양식에 어긋나는 문자열을 RecurrenceDay 리스트로 변환하면 빈 리스트를 반환한다.")
//     @Test
//     fun convertToEntityAttribute_WithAllIllegalFormat() {
//         // given
//         val strRecurrenceDay = "1SSU,-1MO,TTU,W"
//
//         // when
//         val result = converter.convertToEntityAttribute(strRecurrenceDay)
//
//         // then
//         assertThat(result).isEmpty()
//     }
//
//     @DisplayName("빈 문자열 입력 시 빈 리스트를 반환한다.")
//     @Test
//     fun convertToEntityAttribute_WithEmptyString() {
//         // given
//         val dbData = ""
//
//         // when
//         val result = converter.convertToEntityAttribute(dbData)
//
//         // then
//         assertThat(result).isEmpty()
//     }
// }
