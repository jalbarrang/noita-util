package com.dimdarkevil.noitautil.model

import java.awt.image.BufferedImage
import java.math.BigDecimal

data class Wand(
	var shuffle: Boolean = false,
	var spellsCast: Int = 0,
	var castDelay: BigDecimal = BigDecimal.ZERO,
	var rechargeTime: BigDecimal = BigDecimal.ZERO,
	var manaMax: Int = 0,
	var manaChargeSpeed: Int = 0,
	var capacity: Int = 0,
	var spread: BigDecimal = BigDecimal.ZERO,
	var alwaysCasts: List<Spell> = listOf(),
	var spells: List<Spell> = listOf(),
	var spriteFile: String = "",
	var image: BufferedImage? = null,
	var rotatedImage: BufferedImage? = null,
)
