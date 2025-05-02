package com.whatever.domain.balancegame.repository

import com.whatever.domain.balancegame.model.UserChoiceOption
import org.springframework.data.jpa.repository.JpaRepository

interface UserChoiceOptionRepository : JpaRepository<UserChoiceOption, Long> {
}