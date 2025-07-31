package com.whatever.caramel.infrastructure.firebase.model

import com.google.firebase.messaging.Notification

data class FcmNotification(
    val title: String,
    val body: String,
    val image: String? = null,
) {
    fun toNotification(): Notification {
        return Notification.builder()
            .setTitle(title)
            .setBody(body)
            .setImage(image)
            .build()
    }
}
