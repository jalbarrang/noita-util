package com.dimdarkevil.swingutil

import java.awt.BorderLayout
import javax.swing.*

abstract class OkCancelModalDialog<T>(owner: JFrame, title: String) :  JDialog(owner, title, true) {
	private val okBtn = JButton("OK")
	private val cancelBtn = JButton("Cancel")
	private var resOk = false
	private var res : T? = null

	private fun createUiComponents() {
		okBtn.addActionListener { okBtnActionPerformed() }
		cancelBtn.addActionListener { cancelBtnActionPerformed() }

		val btnPanel = JPanel()
		btnPanel.add(okBtn)
		btnPanel.add(cancelBtn)

		val contentPanel = buildUi()
		contentPane.add(contentPanel, BorderLayout.CENTER)
		contentPane.add(btnPanel, BorderLayout.SOUTH)

		pack()
		setLocationRelativeTo(null)
		defaultCloseOperation = DISPOSE_ON_CLOSE
	}

	// called during init to build the content (center) panel
	protected abstract fun buildUi() : JPanel
	// Gets the result of the dialog. Should throw an exception if something didn't work right.
	protected abstract fun getResult(): T

	fun showModal() : Pair<Boolean, T?> {
		createUiComponents()
		isVisible = true
		return Pair(resOk, res)
	}

	protected fun okBtnActionPerformed() {
		try {
			res = getResult()
		} catch (e: Exception) {
			JOptionPane.showMessageDialog(owner, e.message, "Error", JOptionPane.ERROR_MESSAGE)
			return
		}
		resOk = true
		isVisible = false
	}

	protected fun cancelBtnActionPerformed() {
		resOk = false
		isVisible = false
	}

}