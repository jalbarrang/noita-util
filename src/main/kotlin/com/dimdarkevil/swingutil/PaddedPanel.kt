package com.dimdarkevil.swingutil

import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.UIManager

class PaddedPanel(bgColor: Color = UIManager.getColor("Panel.background") ?: Color.LIGHT_GRAY) : JPanel() {
	init {
		val em = EmUnit.EM_SIZE / 2
		val padding = BorderFactory.createEmptyBorder(em, em, em, em)
		this.border = padding
		this.background = bgColor
	}
}