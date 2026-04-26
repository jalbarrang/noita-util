package com.dimdarkevil.noitautil.model

import com.dimdarkevil.noitautil.HOME
import java.io.File

data class AppConfig(
	var winX: Int = -1,
	var winY: Int = -1,
	var winWidth: Int = -1,
	var winHeight: Int = -1,
	var noitaSaveFolder : String = File(HOME, "AppData/LocalLow/Nolla_Games_Noita").path,
	var noitaBackupFolder: String = File(HOME, "Desktop/noita_saves").path,
	var noitaExeFile : String = File("C:/Program Files (x86)/Steam/steamapps/common/Noita/noita.exe").let {
		if (it.exists()) it.path else File(HOME, "noita.exe").path
	},
	var salakieliSplitterPosition: Int = -1,
	var qrefSplitterPosition: Int = -1,
	var spellTableColumnWidths: List<Int> = listOf(),
	var lastSeed: Long = 1948257554L,
	var memoryUseWarningMb: Int = 1400,
	var memoryUseEverySec: Int = 4,
)