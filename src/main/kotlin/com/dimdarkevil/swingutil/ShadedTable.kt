package com.dimdarkevil.swingutil

import java.awt.Color
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel

class ShadedTable(model: TableModel, val shadeColor: Color = Color(240, 240, 240)) : JTable(model) {
	override fun prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int): Component {
		val c = super.prepareRenderer(renderer, row, column)
		when {
			isCellSelected(row, column) -> c.background = selectionBackground
			(row % 2) == 0 -> c.background = shadeColor
			else -> c.background = background
		}
		return c
	}
}