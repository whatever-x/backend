package com.whatever.caramel.domain.sample.repository

import com.whatever.caramel.domain.user.model.UserSetting
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository

@Profile("dev", "local-mem")
interface SampleUserSettingRepository : CrudRepository<UserSetting, Long>
