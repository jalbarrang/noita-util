package com.dimdarkevil.noitautil.model

import java.awt.image.BufferedImage

data class Perk(
	var id: String = "",
	var ui_name: String = "",
	var ui_description: String = "",
	var ui_icon: String = "",
	var perk_icon: String = "",
	var english_name: String = "",
	var english_desc: String = "",
	var image: BufferedImage? = null,
)
