package com.dimdarkevil.swingutil

import java.awt.*
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

fun panelWith(c: Component) : JPanel {
	val p = JPanel()
	p.layout = BorderLayout()
	p.add(c, BorderLayout.CENTER)
	p.alignmentX = Component.LEFT_ALIGNMENT
	p.maximumSize = Dimension(p.maximumSize.width, p.preferredSize.height)
	return p
}

fun panelWithMult(cs: List<Component>) : JPanel {
	//val maxHeight = cs.maxBy { it.preferredSize.height }!!.height
	val p = JPanel()
	p.layout = BoxLayout(p, BoxLayout.X_AXIS)
	cs.forEach { c -> p.add(c) }
	p.alignmentX = Component.LEFT_ALIGNMENT
	p.maximumSize = Dimension(p.maximumSize.width, p.preferredSize.height)
	return p
}

fun panelWithLabel(label: String, comp: Component) : JPanel {
	val p = JPanel()
	p.layout = BoxLayout(p, BoxLayout.X_AXIS)
	p.add(JLabel(label))
	p.add(comp)
	p.alignmentX = Component.LEFT_ALIGNMENT
	p.maximumSize = Dimension(p.maximumSize.width, p.preferredSize.height)
	return p
}

fun northPanelWith(comp: Component) : JPanel {
	val p = JPanel(BorderLayout())
	p.add(comp, BorderLayout.NORTH)
	return p
}

fun flowPanelWith(c: Component) : JPanel {
	val p = JPanel()
	p.layout = FlowLayout(FlowLayout.LEADING, 2, 2)
	p.border = EmptyBorder(0, 5, 0, 5)
	p.add(c)
	return p
}

fun flowPanelWithMult(vararg cs: Component) : JPanel {
	val p = JPanel()
	p.layout = FlowLayout(FlowLayout.LEADING, 2, 2)
	p.border = EmptyBorder(0, 5, 0, 5)
	cs.forEachIndexed { i, c ->
		if (c is JLabel && i > 0) {
			c.border = EmptyBorder(0, 4, 0, 0)
		}
		p.add(c)
	}
	return p
}

fun flowPanelWithLabel(label: String, comp: Component) : JPanel {
	val p = JPanel()
	p.layout = FlowLayout(FlowLayout.LEADING, 2, 2)
	p.border = EmptyBorder(0, 5, 0, 5)
	val lbl = JLabel(label)
	lbl.font = lbl.font.deriveFont(Font.BOLD)
	p.add(lbl)
	p.add(comp)
	return p
}
