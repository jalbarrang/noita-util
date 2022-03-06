package com.dimdarkevil.noitautil

import com.dimdarkevil.swingutil.BackgroundWorker
import oshi.SystemInfo

class MemMonitorWorker(
	private val warnAtMemSizeMb: Double,
	private val warnEverySec: Int,
	progressCallback: (String) -> Unit,
	doneCallback: (Boolean, String, Boolean?) -> Unit,
	abortCallback: () -> Boolean
) : BackgroundWorker<Boolean>(progressCallback, doneCallback, abortCallback) {
	val beep = Beep()

	override fun exec(shouldAbort: () -> Boolean): Boolean {
		var lastPlayedSound = 0L
		val systemInfo = SystemInfo()
		val os = systemInfo.operatingSystem
		var noitaProcess = os.processes.firstOrNull { it.name.lowercase() == "noita" }
		while (!shouldAbort()) {
			if (noitaProcess == null) {
				publish("looking for noita process...")
				Thread.sleep(1000)
				noitaProcess = os.processes.firstOrNull { it.name.lowercase() == "noita" }
			} else {
				try {
					noitaProcess = os.getProcess(noitaProcess.processID)
					val memUsageMb = noitaProcess.residentSetSize.toDouble() / 1048576.0
					val memUsageStr = String.format("%.2f", memUsageMb)
					publish("noita memory: ${memUsageStr}Mb")
					if (memUsageMb > warnAtMemSizeMb && System.currentTimeMillis() > (lastPlayedSound + (warnEverySec * 1000))) {
						beep.play()
						lastPlayedSound = System.currentTimeMillis()
					}
					Thread.sleep(1000)
				} catch (e: Exception) {
					publish("exception reading process info: ${e.message}")
					noitaProcess = null
				}
			}
		}
		return true
	}

}