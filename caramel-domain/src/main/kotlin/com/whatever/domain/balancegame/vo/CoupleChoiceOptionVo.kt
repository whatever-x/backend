package com.whatever.domain.balancegame.vo

class CoupleChoiceOptionVo(
    val myChoice: UserChoiceOptionVo?,
    val partnerChoice: UserChoiceOptionVo?,
) {

    companion object {
        fun from(
            myChoice: UserChoiceOptionVo?,
            partnerChoice: UserChoiceOptionVo?,
        ): CoupleChoiceOptionVo {
            return CoupleChoiceOptionVo(
                myChoice = myChoice,
                partnerChoice = partnerChoice,
            )
        }
    }
}
