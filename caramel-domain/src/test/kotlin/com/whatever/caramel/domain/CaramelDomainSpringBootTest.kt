package com.whatever.caramel.domain

import com.whatever.caramel.TestApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("test")
annotation class CaramelDomainSpringBootTest
