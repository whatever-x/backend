//package com.whatever.caramel.domain.calendarevent.scheduleevent.service
//
//import com.whatever.caramel.domain.calendarevent.exception.ScheduleIllegalArgumentException
//import com.whatever.caramel.domain.calendarevent.service.ScheduleValidator
//import org.assertj.core.api.Assertions.assertThatThrownBy
//import org.junit.jupiter.api.assertDoesNotThrow
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import java.time.LocalDateTime
//
//class ScheduleValidatorTest {
//
//    private val validator = ScheduleValidator()
//
//    // --- validateContentDetail Tests ---
//
//    @Test
//    @DisplayName("제목만 유효할 때 예외를 던지지 않음")
//    fun validateContentDetail_whenTitleIsValid_thenDoesNotThrowException() {
//        assertDoesNotThrow {
//            validator.validateContentDetail("Valid Title", null)
//        }
//    }
//
//    @Test
//    @DisplayName("본문만 유효할 때 예외를 던지지 않음")
//    fun validateContentDetail_whenDescriptionIsValid_thenDoesNotThrowException() {
//        assertDoesNotThrow {
//            validator.validateContentDetail(null, "Valid Description")
//        }
//    }
//
//    @Test
//    @DisplayName("제목과 본문이 모두 유효할 때 예외를 던지지 않음")
//    fun validateContentDetail_whenBothAreValid_thenDoesNotThrowException() {
//        assertDoesNotThrow {
//            validator.validateContentDetail("Valid Title", "Valid Description")
//        }
//    }
//
//    @Test
//    @DisplayName("제목과 본문이 모두 null일 때 예외를 던짐")
//    fun validateContentDetail_whenBothAreNull_thenThrowException() {
//        assertThatThrownBy {
//            validator.validateContentDetail(null, null)
//        }.isInstanceOf(ScheduleIllegalArgumentException::class.java)
//    }
//
//    @Test
//    @DisplayName("제목이 공백 문자열일 때 예외를 던짐")
//    fun validateContentDetail_whenTitleIsBlank_thenThrowException() {
//        assertThrows<ScheduleIllegalArgumentException> {
//            validator.validateContentDetail("   ", "Valid Description")
//        }
//    }
//
//    @Test
//    @DisplayName("본문이 공백 문자열일 때 예외를 던짐")
//    fun validateContentDetail_whenDescriptionIsBlank_thenThrowException() {
//        assertThrows<ScheduleIllegalArgumentException> {
//            validator.validateContentDetail("Valid Title", "   ")
//        }
//    }
//
//    // --- validateDuration Tests ---
//
//    private val now = LocalDateTime.now()
//
//    @Test
//    @DisplayName("시작일이 종료일보다 이전일 때 예외를 던지지 않음")
//    fun validateDuration_whenStartIsBeforeEnd_thenDoesNotThrowException() {
//        assertDoesNotThrow {
//            validator.validateDuration(now, now.plusDays(1))
//        }
//    }
//
//    @Test
//    @DisplayName("시작일과 종료일이 같을 때 예외를 던지지 않음")
//    fun validateDuration_whenStartEqualsEnd_thenDoesNotThrowException() {
//        assertDoesNotThrow {
//            validator.validateDuration(now, now)
//        }
//    }
//
//    @Test
//    @DisplayName("시작일이 종료일보다 이후일 때 예외를 던짐")
//    fun validateDuration_whenStartIsAfterEnd_thenThrowException() {
//        assertThatThrownBy {
//            validator.validateDuration(now, now.minusDays(1))
//        }.isInstanceOf(ScheduleIllegalArgumentException::class.java)
//    }
//
//    // --- validateUserAccess Tests ---
//
//    @Test
//    @DisplayName("사용자가 일정 소유자일 때 예외를 던지지 않음")
//    fun validateUserAccess_whenUserIsOwner_thenDoesNotThrowException() {
//        // given
//        val owner = User(id = 1L)
//        val schedule = createDummyScheduleEvent(owner)
//
//        // when & then
//        assertDoesNotThrow {
//            validator.validateUserAccess(schedule, currentUserId = 1L, currentUserCoupleId = 10L)
//        }
//    }
//
//    @Test
//    @DisplayName("사용자가 같은 커플의 파트너일 때 예외를 던지지 않음")
//    fun validateUserAccess_whenUserIsPartnerInSameCouple_thenDoesNotThrowException() {
//        // given
//        val owner = User(id = 1L)
//        val partner = User(id = 2L)
//        val couple = Couple(id = 10L)
//        couple.addMember(owner)
//        couple.addMember(partner)
//        val schedule = createDummyScheduleEvent(owner)
//
//        // when & then
//        assertDoesNotThrow {
//            validator.validateUserAccess(schedule, currentUserId = partner.id, currentUserCoupleId = couple.id)
//        }
//    }
//
//    @Test
//    @DisplayName("소유자가 싱글이고 다른 사용자가 접근할 때 예외를 던짐")
//    fun validateUserAccess_whenOwnerIsSingleAndUserIsNotOwner_thenThrowException() {
//        // given
//        val owner = User(id = 1L, userStatus = UserStatus.SINGLE)
//        val otherUser = User(id = 2L)
//        val schedule = createDummyScheduleEvent(owner)
//
//        // when & then
//        assertThatThrownBy {
//            validator.validateUserAccess(schedule, currentUserId = otherUser.id, currentUserCoupleId = 99L)
//        }.isInstanceOf(ScheduleAccessDeniedException::class.java)
//    }
//
//    @Test
//    @DisplayName("사용자가 다른 커플에 속해있을 때 예외를 던짐")
//    fun validateUserAccess_whenUserIsInDifferentCouple_thenThrowException() {
//        // given
//        val owner = User(id = 1L)
//        val ownerCouple = Couple(id = 10L)
//        ownerCouple.addMember(owner)
//        val schedule = createDummyScheduleEvent(owner)
//
//        val otherUser = User(id = 2L)
//        val otherCouple = Couple(id = 20L)
//        otherCouple.addMember(otherUser)
//
//        // when & then
//        assertThatThrownBy {
//            validator.validateUserAccess(schedule, currentUserId = otherUser.id, currentUserCoupleId = otherCouple.id)
//        }.isInstanceOf(ScheduleAccessDeniedException::class.java)
//    }
//
//    @Test
//    @DisplayName("소유자가 커플이지만 커플 정보가 null일 때 다른 사용자가 접근하면 예외를 던짐")
//    fun validateUserAccess_whenOwnerCoupleInfoIsNull_thenThrowException() {
//        // given
//        val owner = User(id = 1L, couple = null) // DB 데이터 정합성 문제 상황 가정
//        val otherUser = User(id = 2L)
//        val schedule = createDummyScheduleEvent(owner)
//
//        // when & then
//        assertThatThrownBy {
//            validator.validateUserAccess(schedule, currentUserId = otherUser.id, currentUserCoupleId = 99L)
//        }.isInstanceOf(ScheduleAccessDeniedException::class.java)
//    }
//
//    private fun createDummyScheduleEvent(owner: User): ScheduleEvent {
//        val content = Content(id = 100L, user = owner)
//        return ScheduleEvent(id = 1000L, content = content)
//    }
//}