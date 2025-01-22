package com.whatever.domain.calendarevent.eventrecurrence.model

/**
 * 월 단위 또는 연 단뒤로 반복되는 이벤트의 설정 옵션
 */
enum class MonthlyAnnuallyOption {
    /**
     * 이벤트를 같은 날짜로 반복 설정합니다.
     *
     * ex) 이벤트가 15일에 시작되었다면, 매월(or 매년) 15일 반복에 반복합니다.
     */
    SAME_DAY,

    /**
     * 이벤트를 같은 요일로 반복 설정합니다.
     *
     * ex) 이벤트가 1째주 화요일에 시작되었다면, 매월(or 매년) 1째주 15일 반복에 반복합니다.
     */
    SAME_WEEKDAY,
}