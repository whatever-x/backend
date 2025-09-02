package com.whatever.caramel.batch.entity

import com.whatever.caramel.infrastructure.firebase.model.FcmNotification

data class BatchFcmNotification(
    val targetId: Long,
    val fcmNotification: FcmNotification,
)
