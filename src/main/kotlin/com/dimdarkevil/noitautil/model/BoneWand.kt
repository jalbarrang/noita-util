package com.dimdarkevil.noitautil.model

import java.time.LocalDateTime

data class BoneWand(
	val fileName: String,
	val lastModified: LocalDateTime,
	val wand: Wand
)
