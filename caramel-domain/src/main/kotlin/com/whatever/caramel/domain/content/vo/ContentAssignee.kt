package com.whatever.caramel.domain.content.vo

enum class ContentAssignee {
    ME,
    PARTNER,
    US,
}

fun ContentAssignee.fromRequestorPerspective(isContentOwnerSameAsRequester: Boolean): ContentAssignee {
    return if (isContentOwnerSameAsRequester) {
        this
    } else {
        when (this) {
            ContentAssignee.ME -> ContentAssignee.PARTNER
            ContentAssignee.PARTNER -> ContentAssignee.ME
            ContentAssignee.US -> ContentAssignee.US
        }
    }
} 