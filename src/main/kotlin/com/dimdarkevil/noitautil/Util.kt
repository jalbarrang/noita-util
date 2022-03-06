package com.dimdarkevil.noitautil

import java.io.File

val HOME = File(System.getProperty("user.home"))
const val APP_NAME = "noita-util"

fun String.cleanup() : String {
	var ss = this.split(" --").first().trim()
	if (ss.startsWith('"')) ss = ss.substring(1)
	if (ss.endsWith(',')) ss = ss.substring(0, ss.length-1)
	if (ss.endsWith('"')) ss = ss.substring(0, ss.length-1)
	return ss
}

fun Sequence<String>.groupWithSep(sep: (String) -> Boolean) : Sequence<List<String>> {
	return sequence {
		val lst = mutableListOf<String>()
		forEach { s ->
			lst.add(s)
			if (sep(s)) {
				yield(lst)
				lst.clear()
			}
		}
	}
}

fun List<String>.filterLuaComments() : List<String> {
	val newLines = mutableListOf<String>()
	var inComment = false
	this.forEach { line ->
		val l = line.trim()
		when {
			inComment -> {
				if (l.endsWith("]]--")) {
					inComment = false
				}
			}
			else -> {
				if (l.startsWith("--[[")) {
					inComment = true
				} else {
					newLines.add(line)
				}
			}
		}
	}
	return newLines.filter { !it.trim().startsWith("--") }
}
