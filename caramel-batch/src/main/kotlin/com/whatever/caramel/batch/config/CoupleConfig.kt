package com.whatever.caramel.batch.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.whatever.caramel.domain.couple.repository"])
@EntityScan(basePackages = ["com.whatever.caramel.domain.couple.model"])
class CoupleConfig
