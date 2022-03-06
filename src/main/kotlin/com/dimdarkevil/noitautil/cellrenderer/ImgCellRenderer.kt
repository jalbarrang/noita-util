package com.dimdarkevil.noitautil.cellrenderer

import java.awt.Component
import java.awt.image.BufferedImage
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class ImgCellRenderer(itemHeight: Int) : DefaultTableCellRenderer() {
	private val panel = ImgIconPanel(itemHeight)

	override fun getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		val img = value as BufferedImage
		panel.setImage(img)
		return panel
	}
}