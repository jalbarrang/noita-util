package com.dimdarkevil.noitautil.tablemodel

import com.dimdarkevil.noitautil.model.BoneWand
import java.time.format.DateTimeFormatter
import javax.swing.table.AbstractTableModel
import javax.swing.text.NumberFormatter

class BoneWandTableModel : AbstractTableModel() {
	var items = listOf<BoneWand>()
	private val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")
	private val numberFormatter : NumberFormatter = NumberFormatter()

	override fun getRowCount() : Int {
		return items.size
	}

	override fun getColumnCount() : Int {
		return 2
	}

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
		val w = items[rowIndex]
		return when (columnIndex) {
			0 -> w.fileName
			1 ->  w.lastModified.format(dateFormatter)
			else -> "[unknown]"
		}
	}

	override fun getColumnName(column: Int): String {
		return when (column) {
			0 -> "filename"
			1 -> "modified"
			else -> "[unknown]"
		}
	}

	fun setBoneWands(lst: List<BoneWand>) {
		items = lst
		this.fireTableDataChanged()
	}

	fun boneWandAt(row: Int) : BoneWand? {
		if (row < 0 || row >= items.size) return null
		return items[row]
	}
}