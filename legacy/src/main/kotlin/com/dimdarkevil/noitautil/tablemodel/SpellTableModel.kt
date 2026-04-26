package com.dimdarkevil.noitautil.tablemodel

import com.dimdarkevil.noitautil.model.Spell
import java.awt.image.BufferedImage
import javax.swing.table.AbstractTableModel

class SpellTableModel : AbstractTableModel() {
	private var items = listOf<Spell>()

	override fun getRowCount() : Int {
		return items.size
	}

	override fun getColumnCount() : Int {
		return 4
	}

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
		val b = items[rowIndex]
		return when (columnIndex) {
			0 -> b.image
			1 -> b.id.ifEmpty { "" }
			2 -> b.english_name.ifEmpty { b.name }
			3 -> b.english_desc.ifEmpty { b.description }
			else -> "[unknown]"
		}
	}

	override fun getColumnName(column: Int): String {
		return when (column) {
			0 -> "img"
			1 -> "id"
			2 -> "name"
			3 -> "desc"
			else -> "[unknown]"
		}
	}

	override fun getColumnClass(columnIndex: Int): Class<*> {
		return when (columnIndex) {
			0 -> BufferedImage::class.java
			else -> String::class.java
		}
	}

	fun setSpells(lst: List<Spell>) {
		items = lst
		this.fireTableDataChanged()
	}

	fun spellAt(row: Int) : Spell? {
		if (row < 0 || row >= items.size) return null
		return items[row]
	}
}