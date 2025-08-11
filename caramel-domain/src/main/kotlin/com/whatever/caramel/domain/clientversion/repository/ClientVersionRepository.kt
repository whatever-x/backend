package com.whatever.caramel.domain.clientversion.repository

import com.whatever.caramel.domain.clientversion.model.ClientVersion
import com.whatever.caramel.domain.clientversion.model.OsType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ClientVersionRepository : JpaRepository<ClientVersion, Long> {

    @Query(
        """
        select cv from ClientVersion cv
        where cv.osType = :osType
        order by cv.code desc limit 1
    """
    )
    fun findLatestVersionByOsType(osType: OsType): ClientVersion?

    @Query(
        """
        select cv from ClientVersion cv
        where cv.isMinimum = true and cv.osType = :osType
        order by cv.code desc limit 1
    """
    )
    fun findMinimumVersionByOsType(osType: OsType): ClientVersion?
}