package com.whatever.domain.firebase.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.user.model.User
import com.whatever.util.DateTimeUtil
import com.whatever.util.DateTimeUtil.SYS_ZONE_ID
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
        UniqueConstraint(columnNames = ["user_id", "device_id"]),
    ]
)
class FcmToken(
    initialToken: String,

    @Column(nullable = false)
    val deviceId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "token", unique = true, nullable = false)
    private var _token: String
    val token: String
        get() = _token

    init {
        if (initialToken.isBlank()) {
            throw IllegalArgumentException()  // TODO(준용) custom exception
        }
        _token = initialToken
    }

    fun updateToken(newToken: String) {
        if (newToken.isBlank()) {
            throw IllegalArgumentException()  // TODO(준용) custom exception
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