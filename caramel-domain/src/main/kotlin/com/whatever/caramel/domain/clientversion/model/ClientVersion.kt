package com.whatever.caramel.domain.clientversion.model

import com.whatever.caramel.domain.base.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "client_version_unique_os_type_and_code",
            columnNames = ["os_type", "code"],

        )
    ]
)
class ClientVersion(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val osType: OsType,

    @Column(nullable = false)
    @field:Min(value = 10)  // client의 major 버전이 10으로 시작
    val major: Int,

    @Column(nullable = false)
    val minor: Int,

    @Column(nullable = false)
    val patch: Int,

    @Column(nullable = false)
    val build: Int,

    @Column(nullable = false)
    val isMinimum: Boolean = false,

    @Column(length = 100)
    val releaseNote: String? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(nullable = false)
    val code: Int = major * 1_00_00_00 + minor * 1_00_00 + patch * 1_00 + build
}
