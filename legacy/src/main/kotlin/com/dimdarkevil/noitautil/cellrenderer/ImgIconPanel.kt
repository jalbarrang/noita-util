package com.dimdarkevil.noitautil.cellrenderer

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JPanel

class ImgIconPanel(private val itemHeight: Int) : JPanel() {
	private var img: BufferedImage? = null

	init {
		isOpaque = true
		preferredSize = Dimension(itemHeight, itemHeight)
	}

	fun setImage(i: BufferedImage) {
		img = i
		repaint()
	}

	override fun paintComponent(gg: Graphics) {
		val g = gg as Graphics2D
		img?.let {
			val scale = itemHeight.toDouble() / it.height.toDouble()
			g.scale(scale, scale)
			g.drawImage(img, 0, 0, null)
		} ?: run {
			g.color = Color.YELLOW
			g.fillRect(0, 0, itemHeight, itemHeight)
		}
	}
}