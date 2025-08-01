package com.whatever.caramel.domain.balancegame.vo

import com.whatever.caramel.domain.balancegame.model.UserChoiceOption

data class UserChoiceOptionVo(
    val id: Long = 0L,

    val balanceGameId: Long,

    val balanceGameOptionId: Long,

    val userId: Long,
) {

    companion object {
        fun from(userChoiceOption: UserChoiceOption): UserChoiceOptionVo {
            return UserChoiceOptionVo(
                id = userChoiceOption.id,
                balanceGameId = userChoiceOption.balanceGame.id,
                balanceGameOptionId = userChoiceOption.balanceGameOption.id,
                userId = userChoiceOption.user.id,
            )
        }
    }
}
