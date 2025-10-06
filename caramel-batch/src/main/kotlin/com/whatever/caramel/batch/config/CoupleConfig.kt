package com.whatever.caramel.batch.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration

@Configuration
@EntityScan(basePackages = ["com.whatever.caramel.domain.couple.model"])
class CoupleConfig
