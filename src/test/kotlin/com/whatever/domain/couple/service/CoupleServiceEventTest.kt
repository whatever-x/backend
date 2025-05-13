package com.whatever.domain.couple.service

import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.model.CoupleStatus.INACTIVE
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus.COUPLED
import com.whatever.domain.user.model.UserStatus.SINGLE
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.findByIdAndNotDeleted
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
@RecordApplicationEvents
class CoupleServiceEventTest @Autowired constructor(
    private val coupleService: CoupleService,
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val contentRepository: ContentRepository,
) {

    // autowired 경고가 나오지만 문제 없음
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var events: ApplicationEvents

    private lateinit var securityUtilMock: AutoCloseable

    @BeforeEach
    fun setUp() {
        securityUtilMock = mockStatic(SecurityUtil::class.java)

        tagContentMappingRepository.deleteAllInBatch()
        scheduleEventRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
    }

    @AfterEach
    fun tearDown() {
        securityUtilMock.close()
    }

    @DisplayName("커플 멤버 중 한명이 나갈 경우 커플의 상태를 변경하고 나간 유저의 상태를 SINGLE로 변경한다.")
    @Test
    fun leaveCouple() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }
        // when
        coupleService.leaveCouple(savedCouple.id, myUser.id)

        // then - 커플 나가기 이후 상태 변경 확인
        val inactiveCouple = coupleRepository.findByIdWithMembers(savedCouple.id)
        require(inactiveCouple != null)
        assertThat(inactiveCouple.status).isEqualTo(INACTIVE)
        assertThat(inactiveCouple.members.map { it.id })
            .doesNotContain(myUser.id)
            .containsOnly(partnerUser.id)

        val leavedMyUser = userRepository.findByIdAndNotDeleted(myUser.id)
        val remainingPartnerUser = userRepository.findByIdAndNotDeleted(partnerUser.id)
        assertThat(leavedMyUser!!.userStatus).isEqualTo(SINGLE)
        assertThat(remainingPartnerUser!!.userStatus).isEqualTo(COUPLED)

        val eventPublishCnt = events.stream(CoupleMemberLeaveEvent::class.java).count()
        assertThat(eventPublishCnt).isOne
    }

    @DisplayName("마지막 남은 커플 멤버가 나갈경우 커플을 삭제하고 나간 유저의 상태를 SINGLE로 변경한다.")
    @Test
    fun leaveCouple_WithAllMemberLeave() {
        fun memberLeave(couple: Couple, member: User) {
            couple.removeMember(member)
            coupleRepository.save(couple)
            userRepository.save(member)
        }
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        memberLeave(savedCouple, partnerUser)

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).doReturn(myUser.id)
        }

        // when
        coupleService.leaveCouple(savedCouple.id, myUser.id)

        // then - 커플 나가기 이후 상태 변경 확인
        val inactiveCouple = coupleRepository.findByIdAndNotDeleted(savedCouple.id)
        val leavedMyUser = userRepository.findByIdAndNotDeleted(myUser.id)
        assertThat(inactiveCouple).isNull()
        assertThat(leavedMyUser!!.userStatus).isEqualTo(SINGLE)

        val eventPublishCnt = events.stream(CoupleMemberLeaveEvent::class.java).count()
        assertThat(eventPublishCnt).isOne
    }
}
