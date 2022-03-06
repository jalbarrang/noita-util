package com.dimdarkevil.noitautil.model

import java.awt.image.BufferedImage

data class FontChar(
	var id: Int = 0,
	var x: Int = 0,
	var y: Int = 0,
	var w: Int = 0,
	var h: Int = 0,
	var img: BufferedImage? = null,
)
