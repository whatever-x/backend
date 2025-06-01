package com.whatever.domain.balancegame.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.user.model.User
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
            name = "user_choice_option_unique_idx_user_id_and_game_id",
            columnNames = ["user_id", "game_id"]
        )
    ],
)
class UserChoiceOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    val balanceGame: BalanceGame,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    val balanceGameOption: BalanceGameOption,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
) : BaseEntity()