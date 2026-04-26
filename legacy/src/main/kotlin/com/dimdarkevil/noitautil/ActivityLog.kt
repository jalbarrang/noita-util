package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.Activity
import com.dimdarkevil.noitautil.model.ActivityList
import com.dimdarkevil.noitautil.model.ActivityType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.time.LocalDateTime

object ActivityLog {
	private val om = jacksonObjectMapper()
		.registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
		.configure(SerializationFeature.INDENT_OUTPUT, true)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	private val configDir = "${System.getProperty("user.home")}/.config/com.dimdarkevil/$APP_NAME"
	private val logFile = File(configDir, "activity-log.json")

	fun logAction(activityType: ActivityType, fileName: String) {
		val activityList = getActivityLog()
		activityList.activities.add(
			Activity(LocalDateTime.now(), activityType, fileName)
		)
		om.writeValue(logFile, activityList)
	}

	fun getActivityLog() : ActivityList {
		if (!logFile.exists()) return ActivityList()
		return om.readValue<ActivityList>(logFile)
	}

}