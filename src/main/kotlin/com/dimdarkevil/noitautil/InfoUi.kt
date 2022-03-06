package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.UiInfoComp
import com.dimdarkevil.swingutil.flowPanelWithLabel
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTextArea
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


class InfoUi<T : Any>(val cls: KClass<T>) {
	val comps: List<UiInfoComp>
	private val compMap: Map<String,UiInfoComp>

	init {
		val types = mutableSetOf<String>()
		comps = cls.constructors.first().parameters.mapNotNull { param ->
			val name = param.name ?: throw RuntimeException("param has no name")
			val comp = componentForType("${param.type}")
			if (comp != null) {
				UiInfoComp(
					name = name,
					panel = flowPanelWithLabel("$name: ", comp),
					comp = comp
				)
			} else {
				null
			}
		}
		compMap = comps.associateBy { it.name }
	}

	fun setFromVal(value: T) {
		value::class.memberProperties.forEach { prop ->
			compMap[prop.name]?.let { uiComp ->
				val v = prop.getter.call(value)
				when (uiComp.comp) {
					is JLabel -> uiComp.comp.text = "$v"
					is JTextArea -> uiComp.comp.text = (v as List<String>).joinToString("\n")
					else -> throw RuntimeException("unknown component ${uiComp.comp.javaClass.canonicalName}")
				}
			}
		}
	}

	private fun componentForType(type: String) : Component? {
		return when (type) {
			"kotlin.collections.MutableList<kotlin.String>",
			"kotlin.collections.List<kotlin.String>" -> JTextArea()
			"java.awt.image.BufferedImage?" -> null
			else -> JLabel()
		}
	}

}