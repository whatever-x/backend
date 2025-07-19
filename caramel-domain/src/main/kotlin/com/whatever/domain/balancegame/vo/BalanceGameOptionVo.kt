package com.whatever.domain.balancegame.vo

import com.whatever.domain.balancegame.model.BalanceGameOption

data class BalanceGameOptionVo(
    val id: Long = 0L,

    var optionText: String,
) {

    companion object {
        fun from(balanceGameOption: BalanceGameOption): BalanceGameOptionVo {
            return BalanceGameOptionVo(
                id = balanceGameOption.id,
                optionText = balanceGameOption.optionText,
            )
        }
    }
}
