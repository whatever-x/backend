package com.whatever.caramel.domain.specialday.model

import com.whatever.caramel.domain.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "special_day_unique_idx_loc_date_and_date_name",
            columnNames = ["loc_date", "date_name"]
        )
    ]
)
class SpecialDay(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "special_day_type_enum", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    val type: SpecialDayType,

    @Column(nullable = false)
    var locDate: LocalDate,

    @Column(length = 100, nullable = false)
    var dateName: String,  // ex: "삼일절", "춘분"

    @Column(nullable = false)
    var isHoliday: Boolean,  // 공공기관 휴일 여부

    @Column(nullable = false)
    var sequence: Int,  // 월 내 순번
) : BaseEntity()
