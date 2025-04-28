package com.whatever.domain.calendarevent.specialday.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles
@SpringBootTest
class SpecialDayServiceTest {

    @Autowired
    private lateinit var specialDayService: SpecialDayService

}