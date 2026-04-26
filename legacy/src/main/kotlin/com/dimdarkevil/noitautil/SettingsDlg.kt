package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.AppConfig
import com.dimdarkevil.swingutil.OkCancelModalDialog
import java.awt.BorderLayout
import java.io.File
import java.lang.RuntimeException
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileNameExtensionFilter

class SettingsDlg(private val config: AppConfig, owner: JFrame, title: String, errorMsg: String = "") : OkCancelModalDialog<AppConfig>(owner, title, errorMsg) {
	private val saveFolderLabel = JLabel(config.noitaSaveFolder)
	private val saveFolderBtn = JButton("select")
	private val exeLocationLabel = JLabel(config.noitaExeFile)
	private val exeLocationBtn = JButton("select")
	private val backupPathLabel = JLabel(config.noitaBackupFolder)
	private val backupPathBtn = JButton("select")
	private val warningMbSpinner = JSpinner(SpinnerNumberModel(config.memoryUseWarningMb, 500, 4000, 100))
	private val warningSecSpinner = JSpinner(SpinnerNumberModel(config.memoryUseEverySec, 1, 60, 1))

	override fun buildUi(): JPanel {
		val pathsPanel = JPanel()
		pathsPanel.layout = BoxLayout(pathsPanel, BoxLayout.Y_AXIS)
		pathsPanel.border = EmptyBorder(10,10,10,10)

		val savePanel = JPanel()
		savePanel.layout = BorderLayout(2, 2)
		savePanel.border = EmptyBorder(0, 5, 0, 5)
		savePanel.add(JLabel("game save00 path:"), BorderLayout.WEST)
		savePanel.add(saveFolderLabel, BorderLayout.CENTER)
		savePanel.add(saveFolderBtn, BorderLayout.EAST)

		val exePanel = JPanel()
		exePanel.layout = BorderLayout(2, 2)
		exePanel.border = EmptyBorder(5, 5, 5, 5)
		exePanel.add(JLabel("game exe location:"), BorderLayout.WEST)
		exePanel.add(exeLocationLabel, BorderLayout.CENTER)
		exePanel.add(exeLocationBtn, BorderLayout.EAST)

		val backupPanel = JPanel()
		backupPanel.layout = BorderLayout(2, 2)
		backupPanel.border = EmptyBorder(5, 5, 5, 5)
		backupPanel.add(JLabel("backup location:"), BorderLayout.WEST)
		backupPanel.add(backupPathLabel, BorderLayout.CENTER)
		backupPanel.add(backupPathBtn, BorderLayout.EAST)

		val warningMbPanel = JPanel()
		warningMbPanel.layout = BorderLayout(2, 2)
		warningMbPanel.border = EmptyBorder(5, 5, 5, 5)
		warningMbPanel.add(JLabel("mem usage before warning (Mb):"), BorderLayout.WEST)
		warningMbPanel.add(warningMbSpinner, BorderLayout.CENTER)

		val warningSecPanel = JPanel()
		warningSecPanel.layout = BorderLayout(2, 2)
		warningSecPanel.border = EmptyBorder(5, 5, 5, 5)
		warningSecPanel.add(JLabel("time between warnings (sec):"), BorderLayout.WEST)
		warningSecPanel.add(warningSecSpinner, BorderLayout.CENTER)

		pathsPanel.add(savePanel)
		pathsPanel.add(exePanel)
		pathsPanel.add(backupPanel)
		pathsPanel.add(warningMbPanel)
		pathsPanel.add(warningSecPanel)

		saveFolderBtn.addActionListener { onSaveFolderBtnClick() }
		exeLocationBtn.addActionListener { onExeLocationBtnClick() }
		backupPathBtn.addActionListener { onBackupLocationBtnClick() }

		return pathsPanel
	}

	override fun getResult(): AppConfig {
		val saveFile = File(saveFolderLabel.text)
		val exeFile = File(exeLocationLabel.text)
		val backupFile = File(backupPathLabel.text)
		if (!saveFile.exists()) {
			throw RuntimeException("save00 path ${saveFolderLabel.text} does not exist")
		}
		if (!saveFile.isDirectory) {
			throw RuntimeException("save00 path ${saveFolderLabel.text} is not a directory")
		}
		if (!exeFile.exists()) {
			throw RuntimeException("game exe location ${exeLocationLabel.text} does not exist")
		}
		if (!backupFile.exists() && !backupPathLabel.text.isEmpty()) backupFile.mkdirs()
		if (!backupFile.exists()) {
			throw RuntimeException("backup location ${backupPathLabel.text} does not exist")
		}
		if (!backupFile.isDirectory) {
			throw RuntimeException("backup path ${backupPathLabel.text} is not a directory")
		}
		return AppConfig(
			noitaSaveFolder = saveFolderLabel.text,
			noitaExeFile = exeLocationLabel.text,
			noitaBackupFolder = backupPathLabel.text,
			memoryUseWarningMb = warningMbSpinner.value as Int,
			memoryUseEverySec = warningSecSpinner.value as Int,
		)
	}


	private fun onSaveFolderBtnClick() {
		val selDlg = JFileChooser()
		selDlg.currentDirectory = if (config.noitaSaveFolder.isNotBlank()) File(config.noitaSaveFolder) else HOME
		selDlg.isFileHidingEnabled = false
		selDlg.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		selDlg.selectedFile = File(saveFolderLabel.text)
		if (selDlg.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
			val f = selDlg.selectedFile ?: return
			saveFolderLabel.text = f.path
		}
	}

	private fun onExeLocationBtnClick() {
		val selDlg = JFileChooser()
		selDlg.currentDirectory = if (config.noitaExeFile.isNotBlank()) File(config.noitaExeFile).parentFile else HOME
		selDlg.isFileHidingEnabled = false
		selDlg.fileSelectionMode = JFileChooser.FILES_ONLY
		selDlg.fileFilter = FileNameExtensionFilter("Exe file (.exe)", "exe")
		if (selDlg.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
			val f = selDlg.selectedFile ?: return
			if (f.name != "noita.exe" && f.name != "noita_dev.exe") {
				showError("File must be noita.exe or noita_dev.exe")
			}
			if (!f.exists()) {
				showError("noita.exe does not exist at chosen location")
				return
			}
			exeLocationLabel.text = f.path
		}
	}

	private fun onBackupLocationBtnClick() {
		val selDlg = JFileChooser()
		selDlg.currentDirectory = if (config.noitaBackupFolder.isNotBlank()) File(config.noitaBackupFolder) else HOME
		selDlg.isFileHidingEnabled = false
		selDlg.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		selDlg.selectedFile = File(backupPathLabel.text)
		if (selDlg.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
			val f = selDlg.selectedFile ?: return
			backupPathLabel.text = f.path
		}
	}

	private fun showError(msg: String) {
		JOptionPane.showMessageDialog(owner, msg, "Error", JOptionPane.ERROR_MESSAGE)
	}

	private fun showInfo(msg: String, title: String = "Info") {
		JOptionPane.showMessageDialog(owner, msg, title, JOptionPane.INFORMATION_MESSAGE)
	}

}