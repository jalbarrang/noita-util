package com.dimdarkevil.swingutil

import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener

interface ComponentListenerAdapter : ComponentListener {

	override fun componentMoved(evt: ComponentEvent) {
		//
	}

	override fun componentResized(evt: ComponentEvent) {
		//
	}

	override fun componentHidden(evt: ComponentEvent) {
	}

	override fun componentShown(evt: ComponentEvent) {
	}

}