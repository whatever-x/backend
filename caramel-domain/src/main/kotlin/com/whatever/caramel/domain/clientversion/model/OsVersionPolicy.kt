package com.whatever.caramel.domain.clientversion.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class OsVersionPolicy(
    @Id
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val osType: OsType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "minimum_version_id", nullable = false)
    var minimumVersion: ClientVersion,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_version_id")
    var recommendedVersion: ClientVersion?,
)
