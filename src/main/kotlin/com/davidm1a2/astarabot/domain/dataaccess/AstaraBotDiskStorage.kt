package com.davidm1a2.astarabot.domain.dataaccess

import com.davidm1a2.astarabot.domain.persistent.Listings

data class AstaraBotDiskStorage(
    val listings: Listings
)
