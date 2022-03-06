package com.dimdarkevil.swingutil

import java.awt.*
import kotlin.math.max

class VerticalLayout(private val spacing: Int) : LayoutManager2 {
	private val comps = mutableListOf<Component>()

	override fun layoutContainer(container: Container) {
		val ins = container.insets
		//println("layoutContainer - insets are ${ins.left} ${ins.right} ${ins.top} ${ins.bottom}")
		val w = container.width - (ins.left + ins.right)
		val x = ins.left
		var y = ins.top
		container.components.forEach { comp ->
			//println("layoutContainer - comp height is ${comp.height}")
			val ph = comp.preferredSize.height
			comp.location = Point(x, y)
			comp.setSize(w, ph)
			y += (ph + spacing)
		}
	}

	override fun preferredLayoutSize(container: Container): Dimension {
		println("preferredLayoutSize")
		//val prefWidth = comps.maxBy { it.preferredSize.width }?.preferredSize.width ?: 0
		//val prefWidth = container.preferredSize.width

		val prefWidth = container.parent.width
		val prefHeight = max(container.components.sumBy {
			//println("preferredLayoutSize - comp height is ${it.height}, ${it.preferredSize.height}")
			it.preferredSize.height
		}, 100) + container.insets.top + container.insets.bottom + (spacing * container.components.size)
		//println("preferredLayoutSize returning $prefWidth $prefHeight")
		return Dimension(prefWidth, prefHeight)
	}

	override fun minimumLayoutSize(container: Container): Dimension {
		println("minimumLayoutSize")
		//val minWidth = comps.maxBy { it.minimumSize.width }?.minimumSize.width ?: 0
		//val minWidth = container.minimumSize.width
		val minWidth = 50
		val minHeight = max(container.components.sumBy { it.minimumSize.height }, 100)
		//println("minimumLayoutSize returning $minWidth $minHeight")
		return Dimension(minWidth, minHeight)
	}

	override fun addLayoutComponent(name: String, comp: Component) {
		println("adding component $comp (${comp.javaClass}) with name $name")
		comps.add(comp)
	}

	override fun addLayoutComponent(comp: Component, constraints: Any?) {
		println("addLayoutComponent ${comp} ${comp.javaClass}")
	}

	override fun removeLayoutComponent(comp: Component) {
		println("removing component $comp (${comp.javaClass})")
		comps.remove(comp)
	}

	override fun invalidateLayout(container: Container) {
		println("invalidateLayout")
	}

	override fun getLayoutAlignmentY(container: Container): Float {
		println("getLayoutAlignmentY")
		return 0f
	}

	override fun getLayoutAlignmentX(container: Container): Float {
		println("getLayoutAlignmentX")
		return 0f
	}

	override fun maximumLayoutSize(container: Container): Dimension {
		println("maximumLayoutSize")
		val psize = preferredLayoutSize(container)
		return Dimension(0, psize.height)
	}
}