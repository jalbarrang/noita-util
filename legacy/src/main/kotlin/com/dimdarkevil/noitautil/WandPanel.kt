package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.ActionType
import com.dimdarkevil.noitautil.model.AppConfig
import com.dimdarkevil.noitautil.model.FontChar
import com.dimdarkevil.noitautil.model.Wand
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JPanel
import kotlin.math.*

class WandPanel(val config: AppConfig) : JPanel() {
	val noitaExeFolder = File(config.noitaExeFile).parentFile
	val noitaSaveFolder = File(config.noitaSaveFolder)
	val iconShuffle = loadIcon("icon_gun_shuffle.png") // Shuffle
	val iconSpellsCast = loadIcon("icon_gun_actions_per_round.png") // Spells/Cast
	val iconCastDelay = loadIcon("icon_fire_rate_wait.png") // Cast delay
	val iconRechrgTime = loadIcon("icon_gun_reload_time.png") // Rechrg. Time
	val iconManaMax = loadIcon("icon_mana_max.png") // Mana max
	val iconManaChgSpd = loadIcon("icon_mana_charge_speed.png") // Mana chg.Spd
	val iconCapacity = loadIcon("icon_gun_capacity.png") // Capacity
	val iconSpread = loadIcon("icon_spread_degrees.png") // Spread
	val iconAlwaysCasts= loadIcon("icon_gun_permanent_actions.png") // always casts
	val fontImage = ImageIO.read(File(noitaExeFolder, "data/fonts/font_pixel_noshadow.png"))
	val fontMap = loadFont().associateBy { it.id }
	val spellBoxes = loadSpellBoxes()
	var curWand: Wand? = null
	val fontScale = 3
	val iconScale = 3
	val xoff = 20
	val yoff = 20
	val lineHeight = 8 * fontScale
	val icons = listOf(
		iconShuffle,
		iconSpellsCast,
		iconCastDelay,
		iconRechrgTime,
		iconManaMax,
		iconManaChgSpd,
		iconCapacity,
		iconSpread,
		null,
		iconAlwaysCasts
	)
	val labels = listOf(
		"Shuffle",
		"Spells/Cast",
		"Cast delay",
		"Rechrg. Time",
		"Mana max",
		"Mana chg. Spd",
		"Capacity",
		"Spread",
		null,
		"Always casts"
	)
	val values = listOf<(Wand) -> String>(
		{ w -> if (w.shuffle) "Yes" else "No" },
		{ w -> "${w.spellsCast}"},
		{ w -> "${w.castDelay} s" },
		{ w -> "${w.rechargeTime} s" },
		{ w -> "${w.manaMax}" },
		{ w -> "${w.manaChargeSpeed}"},
		{ w -> "${w.capacity}" },
		{ w -> "${w.spread} DEG"}
	)
	private val atf = AffineTransform()

	init {
		preferredSize = Dimension(308, 320)
		isOpaque = true
	}

	override fun paintComponent(gg: Graphics) {
		val g = gg as Graphics2D
		g.background = Color.BLACK
		g.fillRect(0, 0, this.width, this.height)
		val wand = curWand ?: return
		//g.color = Color.WHITE
		drawString(g, "WAND", xoff, yoff)
		var yy = yoff + (lineHeight * 2)
		icons.forEach { icon ->
			icon?.let {
				g.drawImage(it, xoff, yy, it.width*fontScale, it.height*fontScale, null)
			}
			yy += lineHeight
		}
		//g.color = Color.LIGHT_GRAY
		yy = (yoff + (lineHeight * 2)) - (lineHeight / 4)
		labels.forEach { label ->
			label?.let {
				drawString(g, it, xoff + (12 * fontScale), yy)
			}
			yy += lineHeight
		}
		yy = (yoff + (lineHeight * 2)) - (lineHeight / 4)
		values.forEach { value ->
			drawString(g, value(wand), xoff + (70 * fontScale), yy)
			yy += lineHeight
		}

		wand.rotatedImage?.let { wandImage ->
			val wy = (yoff + (lineHeight * 12)) - (wandImage.height * iconScale * 2)
			val wx = (150 * iconScale)
			g.drawImage(wandImage, wx - (wandImage.width * (iconScale / 2)), wy, wandImage.width * iconScale * 2, wandImage.height * iconScale * 2, null)
		}

		yy = (yoff + (lineHeight * 10))
		var xx = xoff + (65 * fontScale)
		if (wand.alwaysCasts.isNotEmpty()) {
			wand.alwaysCasts.forEach { ac ->
				spellBoxes[ac.type]?.let { box ->
					g.drawImage(box, xx, yy, box.width * iconScale, box.height * iconScale, null)
				}
				ac.uiImage?.let { img ->
					g.drawImage(img, xx + (2 * iconScale), yy + (2 * iconScale), img.width * iconScale, img.height * iconScale, null)
					xx += img.width
				}
			}
		} else {
			spellBoxes[ActionType.ACTION_TYPE_PROJECTILE]?.let { box ->
				g.drawImage(box, xx, yy, box.width * iconScale, box.height * iconScale, null)
			}
		}
		yy = (yoff + (lineHeight * 13))
		xx = xoff - (4 * iconScale)
		(0..9).forEach { idx ->
			if (idx < wand.capacity) {
				val box = (if (idx < wand.spells.size) spellBoxes[wand.spells[idx].type]!! else spellBoxes[ActionType.ACTION_TYPE_PROJECTILE]!!)
				g.drawImage(box, xx, yy, box.width * iconScale, box.height * iconScale, null)
				if (idx < wand.spells.size) {
					val img = wand.spells[idx].uiImage!!
					g.drawImage(img, xx + (2 * iconScale), yy + (2 * iconScale), img.width * iconScale, img.height * iconScale, null)
				}
				xx += ((box.width * iconScale) - (2 * iconScale))
			}
		}
		yy = (yoff + (lineHeight * 15) + (2 * iconScale))
		xx = xoff - (4 * iconScale)
		(10..19).forEach { idx ->
			if (idx < wand.capacity) {
				val box = (if (idx < wand.spells.size) spellBoxes[wand.spells[idx].type]!! else spellBoxes[ActionType.ACTION_TYPE_PROJECTILE]!!)
				g.drawImage(box, xx, yy, box.width * iconScale, box.height * iconScale, null)
				if (idx < wand.spells.size) {
					val img = wand.spells[idx].uiImage!!
					g.drawImage(img, xx + (2 * iconScale), yy + (2 * iconScale), img.width * iconScale, img.height * iconScale, null)
				}
				xx += ((box.width * iconScale) - (2 * iconScale))
			}
		}
		yy = (yoff + (lineHeight * 17) + (4 * iconScale))
		xx = xoff - (4 * iconScale)
		(20..25).forEach { idx ->
			if (idx < wand.capacity) {
				val box = (if (idx < wand.spells.size) spellBoxes[wand.spells[idx].type]!! else spellBoxes[ActionType.ACTION_TYPE_PROJECTILE]!!)
				g.drawImage(box, xx, yy, box.width * iconScale, box.height * iconScale, null)
				if (idx < wand.spells.size) {
					val img = wand.spells[idx].uiImage!!
					g.drawImage(img, xx + (2 * iconScale), yy + (2 * iconScale), img.width * iconScale, img.height * iconScale, null)
				}
				xx += ((box.width * iconScale) - (2 * iconScale))
			}
		}

	}

	fun loadIcon(filename: String) : BufferedImage {
		return ImageIO.read(File(noitaSaveFolder, "data/ui_gfx/inventory/$filename"))
	}

	fun loadFont() : List<FontChar> {
		val fontXmlDoc = Jsoup.parse(File(noitaExeFolder, "data/fonts/font_pixel_noshadow.xml"), "UTF-8", "", Parser.xmlParser())
		val chars = fontXmlDoc.select("QuadChar").map { el ->
			FontChar(
				id = el.attr("id").toInt(),
				x = el.attr("rect_x").toInt(),
				y = el.attr("rect_y").toInt(),
				w = el.attr("rect_w").toInt(),
				h = el.attr("rect_h").toInt(),
			)
		}
		chars.forEach { fc ->
			fc.img = fontImage.getSubimage(fc.x, fc.y, fc.w, fc.h)
		}
		return chars
	}

	fun loadSpellBoxes() : Map<ActionType, BufferedImage> {
		return ActionType.values().map {
			it to ImageIO.read(File(noitaSaveFolder, "data/ui_gfx/inventory/${it.bgFilename}"))
		}.toMap()
	}

	fun setWand(w: Wand?) {
		curWand = w
		repaint()
	}

	fun drawString(g: Graphics2D, s: String, x: Int, y: Int) {
		var xx = x
		s.forEach { c ->
			fontMap[c.code]?.let { fc ->
				g.drawImage(fc.img, xx, y, fc.w*fontScale, fc.h*fontScale, null)
				xx += fc.w * 3
			}
		}
	}


}