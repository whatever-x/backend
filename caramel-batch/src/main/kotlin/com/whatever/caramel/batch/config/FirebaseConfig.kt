package com.whatever.caramel.batch.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "com.whatever.caramel.infrastructure.properties",
    ]
)
@EnableJpaRepositories(basePackages = ["com.whatever.caramel.domain.firebase.repository"])
@EntityScan(basePackages = ["com.whatever.caramel.domain.firebase.model"])
class FirebaseConfig
