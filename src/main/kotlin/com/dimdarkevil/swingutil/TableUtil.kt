package com.dimdarkevil.swingutil

import javax.swing.JTable
import kotlin.math.max
import kotlin.math.min

fun setupTableSizes(table: JTable) {
	val font = table.font
	val metrics = table.getFontMetrics(font)
	val desiredHeight = (metrics.height.toFloat() * 0.4f).toInt() + metrics.height
	if (desiredHeight > table.rowHeight) {
		table.rowHeight = desiredHeight
	}
}

fun resizeColumnWidths(table: JTable) {
	val columnModel = table.columnModel
	val font = table.font
	val metrics = table.getFontMetrics(font)
	val colNames = Array<String>(table.columnCount) { table.getColumnName(it) }
	val widths = IntArray(table.columnCount) { max(metrics.stringWidth(" ${colNames[it]} W"), 15) }
	val ww = metrics.stringWidth("W")
	val rowCnt = min(table.rowCount, 10000)
	for (row in 0 until rowCnt) {
		for (ci in 0 until table.columnCount) {
			//val colName = colNames[ci]
			val renderer = table.getCellRenderer(row, ci)
			val comp = table.prepareRenderer(renderer, row, ci)
			val width = min(300, comp.preferredSize.width + ww)
			if (width > widths[ci]) widths[ci] = width
		}
	}
	//table.preferredSize = Dimension(widths.sum(), table.preferredSize.height)
	for (ci in 0 until table.columnCount) {
		columnModel.getColumn(ci).preferredWidth = widths[ci]
	}
}
