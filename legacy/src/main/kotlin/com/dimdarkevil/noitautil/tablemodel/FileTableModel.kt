package com.dimdarkevil.noitautil.tablemodel

import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.table.AbstractTableModel
import javax.swing.text.NumberFormatter

class FileTableModel : AbstractTableModel() {
	var items = listOf<File>()
	private val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")
	private val numberFormatter : NumberFormatter = NumberFormatter()

	override fun getRowCount() : Int {
		return items.size
	}

	override fun getColumnCount() : Int {
		return 3
	}

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
		val f = items[rowIndex]
		return when (columnIndex) {
			0 -> f.name
			1 ->  LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()),
				TimeZone.getDefault().toZoneId()).format(dateFormatter)
			2 -> f.length().toHumanSize()
			else -> "[unknown]"
		}
	}

	override fun getColumnName(column: Int): String {
		return when (column) {
			0 -> "name"
			1 -> "modified"
			2 -> "size"
			else -> "[unknown]"
		}
	}

	fun setFiles(lst: List<File>) {
		items = lst
		this.fireTableDataChanged()
	}

	private val KB = 1024L
	private val MB = KB * KB
	private val GB = MB * KB

	fun Long.toHumanSize() = when {
		(this < KB) -> "$this bytes"
		(this < MB) -> String.format("%.2f Kb", this.toDouble() / KB.toDouble())
		(this < GB) -> String.format("%.2f Mb", this.toDouble() / MB.toDouble())
		else -> String.format("%.2f Gb", this.toDouble() / GB.toDouble())
	}

}