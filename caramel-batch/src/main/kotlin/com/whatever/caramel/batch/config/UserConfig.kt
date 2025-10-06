package com.whatever.caramel.batch.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.whatever.caramel.domain.user.repository"])
@EntityScan(basePackages = ["com.whatever.caramel.domain.user.model"])
class UserConfig
