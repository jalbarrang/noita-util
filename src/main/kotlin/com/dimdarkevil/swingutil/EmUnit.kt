package com.dimdarkevil.swingutil

import javax.swing.JTextField

object EmUnit {
	val EM_SIZE = getEmSize()

	fun getEmSize() : Int {
		val tf = JTextField()
		val font = tf.font
		println("font is ${font.family} ${font.name}")
		val s = "\u2014"
		val fm = tf.getFontMetrics(font)
		val mwidth = fm.stringWidth(s)
		println("em dash is $s - width is $mwidth")
		return mwidth
	}

}