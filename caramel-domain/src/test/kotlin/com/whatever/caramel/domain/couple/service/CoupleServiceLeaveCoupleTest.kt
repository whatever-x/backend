package com.whatever.caramel.domain.couple.service

import com.whatever.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.model.CoupleStatus.INACTIVE
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import com.whatever.caramel.domain.findByIdAndNotDeleted
import com.whatever.caramel.domain.user.model.UserStatus.COUPLED
import com.whatever.caramel.domain.user.model.UserStatus.SINGLE
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test

@CaramelDomainSpringBootTest
@RecordApplicationEvents
class CoupleServiceLeaveCoupleTest @Autowired constructor(
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

    @BeforeEach
    fun setUp() {
        tagContentMappingRepository.deleteAllInBatch()
        scheduleEventRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
    }

    @DisplayName("커플 멤버 중 한명이 나갈 경우 커플의 상태를 변경하고 나간 유저의 상태를 SINGLE로 변경한다.")
    @Test
    fun leaveCouple() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)

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
        fun memberLeave(couple: Couple, member: com.whatever.caramel.domain.user.model.User) {
            couple.removeMember(member)
            coupleRepository.save(couple)
            userRepository.save(member)
        }
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)
        memberLeave(savedCouple, partnerUser)

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
