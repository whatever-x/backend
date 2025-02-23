package com.whatever.domain.calendarevent.taskevent.repository

import com.whatever.domain.calendarevent.taskevent.model.TaskEvent
import org.springframework.data.jpa.repository.JpaRepository

interface TaskEventRepository : JpaRepository<TaskEvent, Long> {
}