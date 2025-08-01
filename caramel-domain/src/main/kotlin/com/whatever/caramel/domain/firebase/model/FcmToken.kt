package com.whatever.caramel.domain.firebase.model

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.DateTimeUtil.SYS_ZONE_ID
import com.whatever.caramel.domain.base.BaseEntity
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.infrastructure.firebase.exception.FcmIllegalArgumentException
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode.FCM_BLANK_TOKEN
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "fcm_token_unique_idx_user_id_device_id",
            columnNames = ["user_id", "device_id"],
        ),
//        UniqueConstraint(
//            name = "fcm_token_unique_idx_token",
//            columnNames = ["token"],
//        ),
    ]
)
class FcmToken(
    initialToken: String,

    @Column(nullable = false)
    val deviceId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "token", nullable = false)
    private var _token: String
    val token: String
        get() = _token

    init {
        if (initialToken.isBlank()) {
            throw FcmIllegalArgumentException(
                errorCode = FCM_BLANK_TOKEN,
                errorUi = ErrorUi.Toast("알림 정보 등록에 실패했어요.")
            )
        }
        _token = initialToken
    }

    fun updateToken(newToken: String) {
        if (newToken.isBlank()) {
            throw FcmIllegalArgumentException(
                errorCode = FCM_BLANK_TOKEN,
                errorUi = ErrorUi.Toast("알림 정보 업데이트에 실패했어요.")
            )
        }
        _token = newToken

        // newToken이 같더라도 updatedAt을 갱신하기위해 적용.
        // 해당 시점의 updatedAt은 반영되지 않음.
        updatedAt = DateTimeUtil.localNow(SYS_ZONE_ID)
    }

    fun isActiveToken(): Boolean {
        return DateTimeUtil.localNow() <= updatedAt.plusMonths(1)
    }
}
