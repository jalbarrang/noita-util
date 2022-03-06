package com.dimdarkevil.swingutil

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

inline fun <reified T>loadConfig(appName: String, defVal: T) : T {
	val configDir = "${System.getProperty("user.home")}/.config/com.dimdarkevil/$appName"
	File(configDir).mkdirs()
	val configFile = File("$configDir/$appName-config.json")
	return if (configFile.exists()) {
		val om = ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		om.readValue(configFile)
	} else {
		defVal
	}
}

fun <T>saveConfig(appName: String, config: T) {
	val configDir = "${System.getProperty("user.home")}/.config/com.dimdarkevil/$appName"
	File(configDir).mkdirs()
	val configFile = File("$configDir/$appName-config.json")
	val om = ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.configure(SerializationFeature.INDENT_OUTPUT, true)
	om.writeValue(configFile, config)
}
