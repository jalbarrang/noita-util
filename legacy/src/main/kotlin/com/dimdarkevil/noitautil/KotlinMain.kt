package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.AppConfig
import com.dimdarkevil.swingutil.loadConfig
import com.dimdarkevil.swingutil.saveConfig
import org.slf4j.LoggerFactory
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

object KotlinMain {
	private val log = LoggerFactory.getLogger("KotlinMain")

	@JvmStatic
	fun main(args: Array<String>) {
		val config : AppConfig = loadConfig(APP_NAME, AppConfig())
		try {
			System.setProperty("apple.awt.application.name", APP_NAME)
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName())
		} catch (e: UnsupportedLookAndFeelException) {
			// handle exception
		} catch (e: ClassNotFoundException) {
			// handle exception
		} catch (e: InstantiationException) {
			// handle exception
		} catch (e: IllegalAccessException) {
			// handle exception
		}
		val screenSize = Toolkit.getDefaultToolkit().screenSize

		if (config.winWidth < 0) config.winWidth = 1600
		if (config.winHeight < 0) config.winHeight = 1000
		if (config.winX < 0) config.winX = (screenSize.width - config.winWidth) / 2
		if (config.winY < 0) config.winY = (screenSize.height - config.winHeight) / 2

		val icons = listOf(
			ImageIO.read(ResourceLoader.load("/noita-util-16.png")),
			ImageIO.read(ResourceLoader.load("/noita-util-32.png")),
			ImageIO.read(ResourceLoader.load("/noita-util-64.png")),
			ImageIO.read(ResourceLoader.load("/noita-util-128.png")),
			ImageIO.read(ResourceLoader.load("/noita-util-256.png")),
		)

		UIManager.put("FileChooser.readOnly", true)
		val pframe = JFrame("${APP_NAME} ${Version.version}")
		pframe.setSize(480, 180)
		pframe.setLocationRelativeTo(null)
		pframe.setLocation((screenSize.width - 480)/2, (screenSize.height - 180) / 2)
		val loadingScreen = LoadingScreen(pframe, config)
		pframe.contentPane = loadingScreen.mainPanel
		pframe.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
		EventQueue.invokeLater {
			pframe.isVisible = true
		}
		while (!loadingScreen.done) {
			//println("sleeping...")
			Thread.sleep(500)
		}
		val success = loadingScreen.succeeded
		val noitaData = loadingScreen.noitaData
		//println("noitaData is $noitaData")
		pframe.dispose()

		if (!success) {
			val sframe = JFrame("${APP_NAME} ${Version.version}")
			sframe.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
			val (ssuccess, newCfg) = SettingsDlg(config, sframe, "$APP_NAME settings", loadingScreen.failureMessage).showModal()
			if (ssuccess && newCfg != null) {
				config.noitaSaveFolder = newCfg.noitaSaveFolder
				config.noitaExeFile = newCfg.noitaExeFile
				config.noitaBackupFolder = newCfg.noitaBackupFolder
				config.memoryUseWarningMb = newCfg.memoryUseWarningMb
				config.memoryUseEverySec = newCfg.memoryUseEverySec
				saveConfig(APP_NAME, config)
				JOptionPane.showMessageDialog(sframe, "Settings saved. Run noita-util again.")
			}
			sframe.dispose()
		} else {
			val frame = JFrame("${APP_NAME} ${Version.version}")
			val mainForm = try {
				MainForm(config, frame, noitaData!!)
			} catch (e: Exception) {
				log.error(e.message, e)
				throw e
			}
			frame.contentPane = mainForm.mainPanel

			frame.setSize(config.winWidth, config.winHeight)
			frame.setLocationRelativeTo(null)
			frame.setLocation(config.winX, config.winY)
			frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
			frame.iconImages = icons
			//frame.iconImage = ImageIcon(ResourceLoader.load("/pipewrench.png")).image
			frame.addWindowListener(object: WindowAdapter() {
				override fun windowClosing(e: WindowEvent) {
					mainForm.updateConfig()
					saveConfig(APP_NAME, config)
				}
			})
			EventQueue.invokeLater {frame.isVisible = true}
		}
	}
}