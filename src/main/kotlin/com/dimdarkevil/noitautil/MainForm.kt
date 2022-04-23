package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.cellrenderer.ImgCellRenderer
import com.dimdarkevil.noitautil.tablemodel.io.NoitaData
import com.dimdarkevil.noitautil.model.*
import com.dimdarkevil.noitautil.tablemodel.BoneWandTableModel
import com.dimdarkevil.noitautil.tablemodel.FileTableModel
import com.dimdarkevil.noitautil.tablemodel.SpellTableModel
import com.dimdarkevil.swingutil.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ComponentEvent
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import javax.swing.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionEvent

class MainForm(private val config: AppConfig, private val frame: JFrame, private val noitaData: NoitaData) : ComponentListenerAdapter {
	val mainPanel = JPanel()
	private val toolBar = JToolBar()
	private val tabPane = JTabbedPane()
	private val restoreBtn = JButton("restore")
	private val backupBtn = JButton("backup")
	private val refreshBtn = JButton("refresh")
	private val deleteBtn = JButton("delete")
	private val logBtn = JButton("log")
	private val runBtn = JButton("run seed")
	private val settingsBtn = JButton("settings")
	private val exitBtn = JButton("exit")

	private val fileTableModel = FileTableModel()
	private val fileTable = ShadedTable(fileTableModel)
	private val fileScrollPane = JScrollPane(fileTable)
	private val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm")

	private val salakielis = SalakieliUtil.decryptAll(File(config.noitaSaveFolder))
	private val salakieliTextArea = JTextArea()
	private val salakieliScrollPane = JScrollPane(salakieliTextArea)
	private val salakieliFileList = JList<String>(salakielis.keys.toTypedArray())
	private val salakieliSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, salakieliFileList, salakieliScrollPane)

	private val allBtns = mutableListOf<JButton>()
	private val statusBar = JPanel()
	private val statusLabel = JLabel()
	private var abortMonitor = false
	private val memMonitor = MemMonitorWorker(
		config.memoryUseWarningMb.toDouble(),
		config.memoryUseEverySec,
		::memMonitorProgressCallback,
		::memMonitorDoneCallback,
		::memMonitorAbortCallback
	)

	private val qrefTabPane = JTabbedPane()
	private val qrefInfoPanel = JPanel().apply {
		this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
	}
	private val qrefInfoScrollPane = JScrollPane(qrefInfoPanel)
	private val spellInfoUi = SpellInfoPanel(qrefInfoPanel)
	private val spellPanel = JPanel(BorderLayout())
	private val spellSearchTextField = JTextField()
	private val spellTableModel = SpellTableModel()
	private val spellTable = JTable(spellTableModel)
	private val spellScrollPane = JScrollPane(spellTable)
	private val qrefSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, qrefTabPane, qrefInfoScrollPane)

	private val boneWandTableModel = BoneWandTableModel()
	private val boneWandTable = JTable(boneWandTableModel)
	private val boneWandScrollPane = JScrollPane(boneWandTable)
	private val boneWandPanel = WandPanel(config)
	private val boneWandSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boneWandScrollPane, boneWandPanel)

	init {
		mainPanel.layout = BorderLayout(0, 0)
		mainPanel.add(toolBar, BorderLayout.NORTH)

		toolBar.isFloatable = false
		//toolBar.isRollover = true

		statusBar.layout = FlowLayout(FlowLayout.LEADING, 2, 2)
		statusBar.border = EmptyBorder(0, 5, 0, 5)
		statusLabel.text = "***"
		statusBar.add(statusLabel)
		mainPanel.add(statusBar, BorderLayout.SOUTH)

		tabPane.add("saves", fileScrollPane)
		tabPane.add("salakieli", salakieliSplitPane)
		tabPane.add("quickref", qrefSplitPane)
		tabPane.add("bone wands", boneWandSplitPane)

		mainPanel.add(tabPane, BorderLayout.CENTER)

		restoreBtn.icon = IconLoader.imgIcon(Ico.RESTORE, 32)
		backupBtn.icon = IconLoader.imgIcon(Ico.BACKUP, 32)
		refreshBtn.icon = IconLoader.imgIcon(Ico.REFRESH, 32)
		deleteBtn.icon = IconLoader.imgIcon(Ico.DELETE, 32)
		logBtn.icon = IconLoader.imgIcon(Ico.LOG, 32)
		runBtn.icon = IconLoader.imgIcon(Ico.RUN, 32)
		settingsBtn.icon = IconLoader.imgIcon(Ico.SETTINGS, 32)
		exitBtn.icon = IconLoader.imgIcon(Ico.EXIT, 32)
		addBtn(restoreBtn)
		addBtn(backupBtn)
		addBtn(refreshBtn)
		addBtn(deleteBtn)
		addBtn(logBtn)
		toolBar.add(Box.createHorizontalGlue())
		addBtn(runBtn)
		addBtn(settingsBtn)
		addBtn(exitBtn)
		setupBtnStyles(allBtns)

		restoreBtn.addActionListener { onRestore() }
		backupBtn.addActionListener { onBackup() }
		refreshBtn.addActionListener { loadBackupFiles() }
		deleteBtn.addActionListener { onDelete() }
		logBtn.addActionListener { showLog() }
		runBtn.addActionListener { onRunWithSeed() }
		settingsBtn.addActionListener { onSettings() }
		exitBtn.addActionListener { onExit() }
		salakieliFileList.addListSelectionListener { onSalakieliFileSelect(it) }


		qrefTabPane.addTab("spells", spellPanel)
		val spellSearchPanel = PaddedPanel()
		spellSearchPanel.layout = BoxLayout(spellSearchPanel, BoxLayout.X_AXIS)
		spellSearchPanel.add(JLabel("search:"))
		spellSearchPanel.add(spellSearchTextField)
		spellPanel.add(spellSearchPanel, BorderLayout.NORTH)
		spellPanel.add(spellScrollPane, BorderLayout.CENTER)
		spellTable.setDefaultRenderer(BufferedImage::class.java, ImgCellRenderer(32))
		spellTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
		spellTableModel.setSpells(noitaData.spells)
		spellTable.rowHeight = 32
		spellTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		spellTable.selectionModel.addListSelectionListener { onSelectSpell(it) }
		spellSearchTextField.addActionListener { onSpellSearchText(it) }

		boneWandTableModel.setBoneWands(noitaData.boneWands)
		boneWandTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		boneWandTable.selectionModel.addListSelectionListener { onSelectBoneWand(it) }

		if (config.salakieliSplitterPosition != -1) {
			salakieliSplitPane.dividerLocation = config.salakieliSplitterPosition
		}
		if (config.qrefSplitterPosition != -1) {
			qrefSplitPane.dividerLocation = config.qrefSplitterPosition
		}
		if (config.spellTableColumnWidths.isNotEmpty()) {
			config.spellTableColumnWidths.forEachIndexed { idx, width ->
				spellTable.columnModel.getColumn(idx).preferredWidth = width
			}
		}
		if (config.noitaBackupFolder.isNotEmpty() && !(File(config.noitaBackupFolder).exists())) {
			File(config.noitaBackupFolder).mkdirs()
		}
		fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
		setupTableSizes(fileTable)
		loadBackupFiles()

		frame.addComponentListener(this)
	}

	private fun addBtn(btn: JButton) {
		toolBar.add(btn)
		allBtns.add(btn)
	}

	private fun onRestore() {
		if (fileTable.selectedRow < 0) return
		val fileName = fileTableModel.items[fileTable.selectedRow].name
		//val fileName = fileList.selectedValue ?: return
		//println("restoring $fileName")
		try {
			val confirm = JOptionPane.showConfirmDialog(frame, "Backup first?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION)
			when (confirm) {
				JOptionPane.YES_OPTION -> onBackup()
				JOptionPane.CANCEL_OPTION -> return
			}
			val zipFile = File(config.noitaBackupFolder, fileName)
			val destFolder = File(config.noitaSaveFolder)
			val currentSaveFile = File(config.noitaSaveFolder, "save00")
			if (!currentSaveFile.exists()) throw RuntimeException("save file does not exist: ${currentSaveFile.path}")
			val backupSaveFile = File(config.noitaSaveFolder, "save00.bak")
			if (backupSaveFile.exists()) {
				backupSaveFile.deleteRecursively()
				if (backupSaveFile.exists()) throw RuntimeException("could not delete backup save: ${backupSaveFile.path}")
			}
			currentSaveFile.renameTo(backupSaveFile)
			if (currentSaveFile.exists()) throw RuntimeException("error renaming save file ${currentSaveFile.path}")
			unzipFile(zipFile, destFolder)
			if (!currentSaveFile.exists()) throw RuntimeException("save doesn't exist after unzip")
			backupSaveFile.deleteRecursively()
			ActivityLog.logAction(ActivityType.RESTORE, zipFile.name)
			showInfo("done restoring save ${zipFile.name}")
		} catch (e: Exception) {
			showError(e.message ?: "error")
		}
	}

	private fun onDelete() {
		if (fileTable.selectedRows.isEmpty()) return
		try {
			JOptionPane.showConfirmDialog(frame, "Delete these ${fileTable.selectedRows.size} backups?", "Confirmation", JOptionPane.YES_NO_OPTION).let {
				if (it == JOptionPane.NO_OPTION) return
			}
			fileTable.selectedRows.forEach { rowIdx ->
				val f = fileTableModel.items[rowIdx]
				println("-=-= row $rowIdx, file: ${f.name}")
				val fileName = f.name
				if (!f.delete()) throw RuntimeException("Unable to delete ${f.canonicalPath}")
				ActivityLog.logAction(ActivityType.DELETE, fileName)
			}
			loadBackupFiles()
		} catch (e: Exception) {
			showError(e.message ?: "error")
		}
	}

	private fun onBackup() {
		val defName = "save_${LocalDateTime.now().format(dateFormatter)}"
			.replace(" ", "_")
			.replace("-", "_")
			.replace(":", "_")
		val res = JOptionPane.showInputDialog(frame, "name your backup", defName) ?: return
		//println("option pane result $res")
		try {
			val inFile = File(config.noitaSaveFolder, "save00")
			if (!inFile.exists()) throw RuntimeException("can't find save folder at ${inFile.path}")
			val outFile = File(config.noitaBackupFolder, "${res}.zip")
			zipFolder(inFile, outFile)
			loadBackupFiles()
			ActivityLog.logAction(ActivityType.BACKUP, outFile.name)
			showInfo("done backing up to ${outFile.name}")
		} catch (e: Exception) {
			showError(e.message ?: "error")
		}
	}

	private fun showLog() {
		val activityList = ActivityLog.getActivityLog()
		val lst = activityList.activities.sortedByDescending { it.dateTime }
		ActivityLogDlg(frame, "Activity", lst).showModal()
	}

	private fun onRunWithSeed() {
		try {
			val nFile = File(config.noitaExeFile)
			if (!nFile.exists() || (nFile.name != "noita.exe" && nFile.name != "noita_dev.exe")) throw RuntimeException("Set correct exe location")
			val res = JOptionPane.showInputDialog(
				frame,
				"Enter a seed number",
				"Run with seed",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				"${config.lastSeed}"
			) ?: return
			val newSeed = "$res".toLong()
			config.lastSeed = newSeed

			val noitaExe = File(config.noitaExeFile)
			val seedFileContents = """
				<MagicNumbers
				WORLD_SEED="${newSeed}"
				_DEBUG_DONT_LOAD_OTHER_MAGIC_NUMBERS="1"
				_DEBUG_DONT_SAVE_MAGIC_NUMBERS="1" >
				</MagicNumbers>				
			""".trimIndent()
			val seedFile = File(noitaExe.parentFile, "magic.txt")
			//val outlog = File(noitaExe.parentFile, "stdoutlog.log")
			//val errlog = File(noitaExe.parentFile, "stderrlog.log")
			seedFile.writeText(seedFileContents)

			val pb = ProcessBuilder(noitaExe.path, "-no_logo_splashes", "-magic_numbers", seedFile.path)
				.directory(noitaExe.parentFile)
				.inheritIO()
			pb.start()
		} catch (e: Exception) {
			showError(e.message ?: "unknown error")
		}
	}

	private fun onSettings() {
		val (success, newCfg) = SettingsDlg(config, frame, "$APP_NAME settings").showModal()
		if (success && newCfg != null) {
			config.noitaSaveFolder = newCfg.noitaSaveFolder
			config.noitaExeFile = newCfg.noitaExeFile
			config.noitaBackupFolder = newCfg.noitaBackupFolder
			config.memoryUseWarningMb = newCfg.memoryUseWarningMb
			config.memoryUseEverySec = newCfg.memoryUseEverySec
			saveConfig(APP_NAME, config)
		}
	}

	fun onSalakieliFileSelect(e: ListSelectionEvent) {
		salakielis[salakieliFileList.selectedValue]?.let { txt ->
			salakieliTextArea.text = txt
			salakieliTextArea.caretPosition = 0
		}
	}

	private fun onSelectSpell(e: ListSelectionEvent) {
		if (!e.valueIsAdjusting) {
			spellTableModel.spellAt(spellTable.selectedRow)?.let { spell ->
				spellInfoUi.setFromVal(spell)
			}
		}
	}

	private fun onSpellSearchText(e: ActionEvent) {
		val txt = e.actionCommand.trim()
		if (txt.isEmpty()) {
			spellTableModel.setSpells(noitaData.spells)
		} else {
			spellTableModel.setSpells(noitaData.spells.filter {
				it.name.contains(txt, true) ||
					it.description.contains(txt, true) ||
					it.english_name.contains(txt, true)
			})
		}
	}

	private fun onSelectBoneWand(e: ListSelectionEvent) {
		if (!e.valueIsAdjusting) {
			boneWandTableModel.boneWandAt(boneWandTable.selectedRow)?.let { boneWand ->
				boneWandPanel.setWand(boneWand.wand)
			}
		}
	}

	private fun onExit() {
		frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
	}

	private fun loadBackupFiles() {
		//fileListModel.clear()
		val files = File(config.noitaBackupFolder).listFiles { f -> (f.isFile && f.extension == "zip") }
			?.sortedByDescending { it.lastModified() } ?: listOf()
		fileTableModel.setFiles(files)
	}

	override fun componentShown(evt: ComponentEvent) {
		setupBtnSizes(allBtns)
		memMonitor.execute()
	}

	private fun showError(msg: String) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE)
	}

	private fun showInfo(msg: String, title: String = "Info") {
		JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.INFORMATION_MESSAGE)
	}

	private fun setupBtnStyles(btns: List<JButton>) {
		btns.forEach { btn ->
			btn.horizontalTextPosition = SwingConstants.CENTER
			btn.verticalTextPosition = SwingConstants.BOTTOM
		}
	}

	private fun setupBtnSizes(btns: List<JButton>) {
		var maxWidth = 0
		var maxHeight = 0
		val margin = 4
		btns.forEach { btn ->
			if (btn.width > maxWidth) maxWidth = btn.width
			if (btn.height > maxHeight) maxHeight = btn.height
		}
		var x = margin
		btns.forEach { btn ->
			btn.setLocation(x, btn.y)
			btn.setSize(maxWidth, maxHeight)
			val size = Dimension(maxWidth, maxHeight)
			btn.minimumSize = size
			btn.maximumSize = size
			btn.preferredSize = size
			x += maxWidth + margin
		}
		toolBar.doLayout()
	}

	fun getSpellTableColumnWidths() = (0 until 4).map {
		spellTable.columnModel.getColumn(it).preferredWidth
	}

	fun updateConfig() {
		config.winX = frame.x
		config.winY = frame.y
		config.winWidth = frame.width
		config.winHeight = frame.height
		config.salakieliSplitterPosition = salakieliSplitPane.dividerLocation
		config.qrefSplitterPosition = qrefSplitPane.dividerLocation
		config.spellTableColumnWidths = getSpellTableColumnWidths()
	}

	fun appIsClosing() {
		abortMonitor = true
	}

	fun zipFolder(src: File, dst: File) {
		if (!src.isDirectory) throw RuntimeException("${src.path} is not a directory")
		ZipOutputStream(FileOutputStream(dst)).use { zos ->
			zipFile(src, src.name, zos)
		}
	}

	fun zipFile(fileToZip: File, fileName: String, zos: ZipOutputStream) {
		if (fileToZip.isDirectory) {
			if (fileName.endsWith("/")) {
				zos.putNextEntry(ZipEntry(fileName))
				zos.closeEntry()
			} else {
				zos.putNextEntry(ZipEntry("${fileName}/"))
			}
			fileToZip.listFiles()?.forEach { f ->
				zipFile(f, "$fileName/${f.name}", zos)
			}
			return
		}
		FileInputStream(fileToZip).use { fis ->
			val zipEntry = ZipEntry(fileName)
			zos.putNextEntry(zipEntry)
			val bytes = ByteArray(65536)
			var length = fis.read(bytes)
			while (length >= 0) {
				zos.write(bytes, 0, length)
				length = fis.read(bytes)
			}
		}
	}

	fun unzipFile(fileToUnzip: File, destFolder: File) {
		val buffer =  ByteArray(65536)
		ZipInputStream(FileInputStream(fileToUnzip)).use { zis ->
			var zipEntry = zis.nextEntry
			while (zipEntry != null) {
				val newFile = newFile(destFolder, zipEntry)
				if (zipEntry.isDirectory) {
					if (!newFile.isDirectory && !newFile.mkdirs()) {
						throw RuntimeException("Failed to create directory ${newFile.path}")
					}
				} else {
					val parent = newFile.parentFile
					if (!parent.isDirectory && !parent.mkdirs()) {
						throw RuntimeException("failed to create directory ${parent.path}")
					}
					FileOutputStream(newFile).use { fos ->
						var len = zis.read(buffer)
						while (len > 0) {
							fos.write(buffer, 0, len)
							len = zis.read(buffer)
						}
					}
				}
				zipEntry = zis.nextEntry
			}
		}
	}

	fun newFile(destFolder: File, zipEntry: ZipEntry) : File {
		val destFile = File(destFolder, zipEntry.name)
		val destDirPath = destFolder.canonicalPath
		val destFilePath = destFile.canonicalPath
		if (!destFilePath.startsWith("${destDirPath}${File.separator}")) {
			throw RuntimeException("entry outside target dir: ${zipEntry.name}")
		}
		return destFile
	}

	fun memMonitorProgressCallback(msg: String) {
		statusLabel.text = msg
	}

	fun memMonitorDoneCallback(success: Boolean, msg: String, res: Boolean?) {
	}

	fun memMonitorAbortCallback() : Boolean {
		return abortMonitor
	}

}