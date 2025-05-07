package com.whatever.domain.balancegame.model

import com.whatever.domain.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
class BalanceGame(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, unique = true)
    val gameDate: LocalDate,

    @Column(nullable = false)
    val question: String,

    @OneToMany(mappedBy = "balanceGame", fetch = FetchType.LAZY)
    val options: List<BalanceGameOption> = listOf()

) : BaseEntity()