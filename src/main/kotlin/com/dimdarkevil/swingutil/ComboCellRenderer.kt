package com.dimdarkevil.swingutil

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class ComboCellRenderer: DefaultListCellRenderer() {

	override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		val item = when (value) {
			null -> ""
			is Map<*,*> -> value.entries.joinToString { "${it.key}: ${it.value}" }
			is Class<*> -> value.simpleName
			else -> "$value"
		}
		return super.getListCellRendererComponent(list, item, index, isSelected, cellHasFocus)
	}
}