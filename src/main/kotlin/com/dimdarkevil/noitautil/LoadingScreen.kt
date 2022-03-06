package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.tablemodel.io.DataLoader
import com.dimdarkevil.noitautil.tablemodel.io.NoitaData
import com.dimdarkevil.noitautil.model.AppConfig
import com.dimdarkevil.swingutil.ComponentListenerAdapter
import com.dimdarkevil.swingutil.panelWith
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class LoadingScreen(val frame: JFrame, val config: AppConfig) : ComponentListenerAdapter {
	val mainPanel = JPanel()
	var noitaData: NoitaData? = null
	var succeeded = false
	var done = false
	private val progressLabel = JLabel("loading...")
	private val progressBar = JProgressBar(0, 100)
	private val worker = DataLoader(config, ::progressCallback, ::doneCallback, ::abortCallback)

	init {
		mainPanel.preferredSize = Dimension(480, 160)
		mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
		mainPanel.border = EmptyBorder(4, 4, 4, 4)
		progressBar.isIndeterminate = true
		progressBar.isStringPainted = true
		progressBar.string = "what"
		progressBar.value = 0
		val tpanel = panelWith(progressLabel)
		tpanel.preferredSize = Dimension(tpanel.preferredSize.width, tpanel.preferredSize.height+8)
		val ppanel = panelWith(progressBar)
		ppanel.preferredSize = Dimension(tpanel.preferredSize.width, tpanel.preferredSize.height+8)
		mainPanel.add(tpanel, BorderLayout.NORTH)
		mainPanel.add(ppanel, BorderLayout.CENTER)
		frame.addComponentListener(this)
	}

	fun progressCallback(progress: String) {
		//println("progress: ${progress}")
		val labelParts = progress.split("|")
		progressLabel.text = labelParts[0]
		progressBar.string = if (labelParts.size > 1) labelParts[1] else ""
		//progressBar.value = progress.second
	}

	fun doneCallback(success: Boolean, msg: String, data: NoitaData?) {
		succeeded = success
		noitaData = data
		if (!success) {
			progressLabel.text = msg
			progressBar.value = 0
		}
		done = true
	}

	fun abortCallback() : Boolean {
		return false
	}

	override fun componentShown(evt: ComponentEvent) {
		worker.execute()
	}

}