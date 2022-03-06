package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.Activity
import java.time.format.DateTimeFormatter
import javax.swing.table.AbstractTableModel

class ActivityTableModel : AbstractTableModel() {
	var items = listOf<Activity>()
	private val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")

	override fun getRowCount() : Int {
		return items.size
	}

	override fun getColumnCount() : Int {
		return 3
	}

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
		val a = items[rowIndex]
		return when (columnIndex) {
			0 -> a.dateTime.format(dateFormatter)
			1 -> a.activityType.name
			2 -> a.fileName
			else -> "[unknown]"
		}
	}

	override fun getColumnName(column: Int): String {
		return when (column) {
			0 -> "date"
			1 -> "action"
			2 -> "filename"
			else -> "[unknown]"
		}
	}

	fun setActivities(lst: List<Activity>) {
		items = lst
		this.fireTableDataChanged()
	}

}