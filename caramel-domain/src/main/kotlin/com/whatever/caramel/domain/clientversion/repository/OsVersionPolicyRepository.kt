package com.whatever.caramel.domain.clientversion.repository

import com.whatever.caramel.domain.clientversion.model.OsVersionPolicy
import com.whatever.caramel.domain.clientversion.model.OsType
import org.springframework.data.jpa.repository.JpaRepository

interface OsVersionPolicyRepository : JpaRepository<OsVersionPolicy, OsType>