package com.example.todayvolleyball.util

import java.time.DayOfWeek
import java.time.LocalDate

object KoreanHolidays {
    private val holidayDates = setOf(
        // 2026 Holidays
        LocalDate.of(2026, 1, 1),   // 신정
        LocalDate.of(2026, 2, 16),  // 설날 연휴
        LocalDate.of(2026, 2, 17),  // 설날
        LocalDate.of(2026, 2, 18),  // 설날 연휴
        LocalDate.of(2026, 3, 1),   // 삼일절
        LocalDate.of(2026, 5, 5),   // 어린이날
        LocalDate.of(2026, 5, 24),  // 부처님오신날
        LocalDate.of(2026, 6, 6),   // 현충일
        LocalDate.of(2026, 8, 15),  // 광복절
        LocalDate.of(2026, 9, 24),  // 추석 연휴
        LocalDate.of(2026, 9, 25),  // 추석
        LocalDate.of(2026, 9, 26),  // 추석 연휴
        LocalDate.of(2026, 10, 3),  // 개천절
        LocalDate.of(2026, 10, 9),  // 한글날
        LocalDate.of(2026, 12, 25), // 성탄절

        // 2027 Holidays
        LocalDate.of(2027, 1, 1),   // 신정
        LocalDate.of(2027, 2, 6),   // 설날 연휴
        LocalDate.of(2027, 2, 7),   // 설날
        LocalDate.of(2027, 2, 8),   // 설날 연휴
        LocalDate.of(2027, 2, 9),   // 대체공휴일
        LocalDate.of(2027, 3, 1),   // 삼일절
        LocalDate.of(2027, 5, 5),   // 어린이날
        LocalDate.of(2027, 5, 13),  // 부처님오신날
        LocalDate.of(2027, 6, 6),   // 현충일
        LocalDate.of(2027, 8, 15),  // 광복절
        LocalDate.of(2027, 9, 14),  // 추석 연휴
        LocalDate.of(2027, 9, 15),  // 추석
        LocalDate.of(2027, 9, 16),  // 추석 연휴
        LocalDate.of(2027, 10, 3),  // 개천절
        LocalDate.of(2027, 10, 9),  // 한글날
        LocalDate.of(2027, 12, 25)  // 성탄절
    )

    fun isHoliday(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SUNDAY || holidayDates.contains(date)
    }

    fun isSaturday(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY
    }
}
