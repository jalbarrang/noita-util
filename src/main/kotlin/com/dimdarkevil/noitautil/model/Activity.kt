package com.dimdarkevil.noitautil.model

import java.time.LocalDateTime

enum class ActivityType {
	BACKUP,
	RESTORE,
	DELETE,
}

data class ActivityList(
	val activities: MutableList<Activity> = mutableListOf()
)

data class Activity(
	val dateTime: LocalDateTime,
	val activityType: ActivityType,
	val fileName: String
)
