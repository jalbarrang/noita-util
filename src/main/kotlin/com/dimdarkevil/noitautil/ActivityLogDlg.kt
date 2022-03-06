package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.Activity
import com.dimdarkevil.swingutil.setupTableSizes
import java.awt.BorderLayout
import javax.swing.*

class ActivityLogDlg(owner: JFrame, title: String, val activities: List<Activity>) : JDialog(owner, title, true) {
	private val okBtn = JButton("OK")
	private val activityTableModel = ActivityTableModel()
	private val activityTable = JTable(activityTableModel)
	private val scrollPane = JScrollPane(activityTable)

	private fun createUiComponents() {
		okBtn.addActionListener { okBtnActionPerformed() }

		val btnPanel = JPanel()
		btnPanel.add(okBtn)

		activityTableModel.setActivities(activities)
		contentPane.add(scrollPane, BorderLayout.CENTER)
		contentPane.add(btnPanel, BorderLayout.SOUTH)

		setupTableSizes(activityTable)
		pack()
		setLocationRelativeTo(null)
		defaultCloseOperation = DISPOSE_ON_CLOSE
	}

	private fun okBtnActionPerformed() {
		isVisible = false
	}

	fun showModal()  {
		createUiComponents()
		isVisible = true
	}

}