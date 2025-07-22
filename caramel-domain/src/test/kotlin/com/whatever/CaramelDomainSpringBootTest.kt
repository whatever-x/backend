package com.whatever

import com.whatever.caramel.domain.TestApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [TestApplication::class])//, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
annotation class CaramelDomainSpringBootTest
