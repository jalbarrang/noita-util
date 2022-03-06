package com.dimdarkevil.noitautil.tablemodel.io

import com.dimdarkevil.noitautil.cleanup
import com.dimdarkevil.noitautil.filterLuaComments
import com.dimdarkevil.noitautil.groupWithSep
import com.dimdarkevil.noitautil.model.*
import com.dimdarkevil.swingutil.BackgroundWorker
import org.apache.commons.csv.CSVFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileReader
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class DataLoader(
	private val config: AppConfig,
	progressCallback: (String) -> Unit,
	doneCallback: (Boolean, String, NoitaData?) -> Unit,
	abortCallback: () -> Boolean
) : BackgroundWorker<NoitaData>(progressCallback, doneCallback, abortCallback)
{
	override fun exec(shouldAbort: () -> Boolean): NoitaData {
		if (!File(config.noitaExeFile).exists()) {
			throw RuntimeException("Noita exe file not found")
		}
		publish("loading translations|")
		val translations = loadTranslations(config) {
			publish("loading translations|$it")
		}
		publish("loading spells|")
		val spells = loadSpells(config, translations) {
			publish("loading spells|$it")
		}
		publish("loading perks|")
		val perks = loadPerks(config, translations) {
			publish("loading perks|$it")
		}
		publish("loading bone wands|")
		val boneWands = loadBoneWands(config, spells) {
			publish("loading bone wands|$it")
		}
		publish("done")
		return NoitaData(
			translations = translations,
			spells = spells,
			perks = perks,
			boneWands = boneWands,
		)
	}
}

data class NoitaData(
	val translations: Map<String, String>,
	val spells: List<Spell>,
	val perks: List<Perk>,
	val boneWands: List<BoneWand>
)


fun loadTranslations(config: AppConfig, cb: ((String) -> Unit)? = null) : Map<String,String> {
	val transFile = getTranslationsFile(config)
	val trans = FileReader(transFile).use { rdr ->
		CSVFormat.DEFAULT.parse(rdr).records.associate { rec ->
			val id = rec[0].trim()
			val desc = rec[1].trim()
			cb?.invoke(id)
			"\$${id}" to desc
		}
	}
	return trans
}

fun loadSpells(config: AppConfig, translations: Map<String,String>, cb: ((String) -> Unit)? = null) : List<Spell> {
	val f = getGunActionsFile(config)
	val re = Regex("\\s+")
	val lines = f.readLines(Charsets.UTF_8).filterLuaComments()
	val spellChunks = lines.asSequence().groupWithSep { it.trim() == "}," }.map { lst ->
		lst.map { it.replace(re, " ") }
	}
	var inFunc = false
	val spells = spellChunks.map { lst ->
		val spell = Spell()
		lst.forEach { line ->
			if (inFunc) {
				if (line.trim() == "end,") {
					inFunc = false
				}
				spell.action.add(line)
			} else {
				if (line.contains("=")) {
					val (prop, value) = line.split("=").let { Pair(it[0].trim(), it[1].trim().cleanup()) }
					when (prop) {
						"id" -> spell.id = value
						"name" -> {
							spell.name = value
							spell.english_name = translations[value] ?: spell.name
						}
						"description" -> {
							spell.description = value
							spell.english_desc = translations[value] ?: spell.description
						}
						"type" -> spell.type = ActionType.valueOf(value)
						"sprite" -> spell.sprite = value
						"sprite_unidentified" -> spell.sprite_unidentified = value
						"related_projectiles" -> spell.related_projectiles = value
						"spawn_level" -> spell.spawn_level = value.split(",").map { it.trim().toInt() }
						"spawn_probability" -> spell.spawn_probability = value.split(",").map { it.trim().toDouble() }
						"price" -> spell.price = value.toInt()
						"mana" -> spell.mana = value.toInt()
						"max_uses" -> spell.max_uses = value.toInt()
						"custom_xml_file" -> spell.custom_xml_file = value
						"action" -> {
							spell.action.add(value)
							inFunc = true
						}
						"never_unlimited" -> spell.never_unlimited = value.toBoolean()
						"spawn_requires_flag" -> spell.spawn_requires_flag = value
						"sound_loop_tag" -> spell.sound_loop_tag = value
						"spawn_manual_unlock" -> spell.spawn_manual_unlock = value.toBoolean()
						"recursive" -> spell.recursive = value.toBoolean()
						"ai_never_uses" -> spell.ai_never_uses = value.toBoolean()
						"related_extra_entities" -> spell.related_extra_entities = value
						"is_dangerous_blast" -> spell.is_dangerous_blast = value.toBoolean()
					}
				}
			}
		}
		spell
	}.toList()
	val saveFolder = File(config.noitaSaveFolder)
	spells.forEach { spell ->
		if (spell.sprite.isNotEmpty()) {
			val bi = ImageIO.read(File(saveFolder, spell.sprite))
			val bb = BufferedImage(bi.width, bi.height, BufferedImage.TYPE_INT_ARGB)
			val g = bb.createGraphics()
			g.color = Color.BLACK
			g.fillRect(0, 0, bb.width, bb.height)
			g.drawImage(bi, 0, 0, null)
			g.dispose()
			spell.image = bb
		}
	}
	return spells.sortedBy { it.english_name }
}

fun loadPerks(config: AppConfig, translations: Map<String, String>, cb: ((String) -> Unit)? = null) : List<Perk> {
	val f = getPerkListFile(config)
	val lines = f.readLines(Charsets.UTF_8).filterLuaComments()
	val perks = mutableListOf<Perk>()
	var curPerk = Perk()
	lines.forEach { line ->
		val l = line.trim()
		if (l == "},") {
			cb?.let { it("perk: ${curPerk.id}") }
			curPerk.english_name = translations[curPerk.ui_name] ?: curPerk.ui_name
			curPerk.english_desc = translations[curPerk.ui_description] ?: curPerk.ui_description
			perks.add(curPerk)
			curPerk = Perk()
		} else {
			when {
				l.startsWith("id") -> curPerk.id = l.getRightOfEqual()
				l.startsWith("ui_name") -> curPerk.ui_name = l.getRightOfEqual()
				l.startsWith("ui_description") -> curPerk.ui_description = l.getRightOfEqual()
				l.startsWith("ui_icon") -> curPerk.ui_icon = l.getRightOfEqual()
				l.startsWith("perk_icon") -> curPerk.perk_icon = l.getRightOfEqual()
			}
		}
	}
	val saveFolder = File(config.noitaSaveFolder)
	perks.forEach { perk ->
		if (perk.ui_icon.isNotEmpty()) {
			perk.image = ImageIO.read(File(saveFolder, perk.perk_icon))
		}
	}
	return perks.sortedBy { it.english_name }
}

fun loadBoneWands(config: AppConfig, spells: List<Spell>, cb: ((String) -> Unit)? = null) : List<BoneWand> {
	val spellMap = spells.associateBy { it.id }
	val boneFolder = File(File(config.noitaSaveFolder), "save00/persistent/bones_new")
	return boneFolder.listFiles()?.filter { it.extension.lowercase() == "xml" }?.map {
		cb?.invoke("bone wand: ${it.name}")
		val boneWand = loadBoneWand(it, spellMap)
		boneWand.wand.image = ImageIO.read(File(File(config.noitaSaveFolder), "${boneWand.wand.spriteFile}"))
		boneWand.wand.rotatedImage = rotateImageByDegrees(boneWand.wand.image!!, 270.0)
		boneWand.wand.alwaysCasts.forEach { spell ->
			spell.uiImage = ImageIO.read(File(File(config.noitaSaveFolder), "${spell.uiImageFilename}"))
		}
		boneWand.wand.spells.forEach { spell ->
			spell.uiImage = ImageIO.read(File(File(config.noitaSaveFolder), "${spell.uiImageFilename}"))
		}
		boneWand
	} ?: listOf()
}

fun loadBoneWand(f: File, spells: Map<String, Spell>) : BoneWand {
	val fileModified = Instant.ofEpochMilli(f.lastModified())
		.atZone(ZoneId.systemDefault())
		.toLocalDateTime()
	val mc = MathContext(4, RoundingMode.HALF_UP)
	val doc = Jsoup.parse(f, "UTF-8", "", Parser.xmlParser())
	val abilityComp = doc.select("AbilityComponent").first() ?: throw RuntimeException("AbilityComponent not found")
	val gunConfig = abilityComp.select("gun_config").firstOrNull() ?: throw RuntimeException("gun_config not found")
	val gunactionConfig = abilityComp.select("gunaction_config").firstOrNull() ?: throw RuntimeException("gunaction_config not found")
	val wand = Wand(
		shuffle = (gunConfig.attr("shuffle_deck_when_empty") == "1"),
		spellsCast = gunConfig.attr("actions_per_round").toInt(),
		castDelay = BigDecimal(gunactionConfig.attr("fire_rate_wait")).setScale(4).divide(BigDecimal("60.0000"), mc).setScale(2, RoundingMode.HALF_UP),
		rechargeTime = BigDecimal(gunConfig.attr("reload_time")).setScale(4).divide(BigDecimal("60.0000"), mc).setScale(2, RoundingMode.HALF_UP),
		manaMax = abilityComp.attr("mana_max").toFloat().toInt(),
		manaChargeSpeed = abilityComp.attr("mana_charge_speed").toInt(),
		capacity = gunConfig.attr("deck_capacity").toInt(),
		spread = BigDecimal(gunactionConfig.attr("spread_degrees")).setScale(2),
		alwaysCasts = loadSpellsForWand(doc.root(), spells, "1"),
		spells = loadSpellsForWand(doc.root(), spells, "0"),
		spriteFile = abilityComp.attr("sprite_file")
	)
	return BoneWand(
		fileName = f.name,
		lastModified = fileModified,
		wand = wand
	)
}

private fun loadSpellsForWand(root: Element, spells: Map<String, Spell>, alwaysCastVal: String) : List<Spell> {
	return root.select("Entity").filter { it.attr("tags").contains("card_action") }.filter { el ->
		val itemComponent = el.select("ItemComponent").first() ?: throw RuntimeException("ItemComponent not found")
		val isAlwaysCast = itemComponent.attr("permanently_attached")
		isAlwaysCast == alwaysCastVal
	}.map { el ->
		val spellId = el.select("ItemActionComponent").first()?.attr("action_id") ?: throw RuntimeException("ItemActionComponent not found")
		val imageFilename = el.select("SpriteComponent").first()?.attr("image_file") ?: throw RuntimeException("SpriteComponent not found")
		spells[spellId]?.copy(uiImageFilename = imageFilename) ?: throw RuntimeException("Spell $spellId not found")
	}
}

fun String.getRightOfEqual() : String {
	return this.split("=").last().trim().unquote()
}

fun String.unquote() : String {
	return if (this.startsWith("\"") && this.endsWith("\"")) {
		this.substring((1..this.length-2))
	} else if (this.startsWith("\"") && this.endsWith("\",")) {
		this.substring((1..this.length-3))
	} else {
		this
	}
}

fun getGunActionsFile(config: AppConfig) : File {
	val gunActionsFile = File(File(config.noitaSaveFolder), "data/scripts/gun/gun_actions.lua")
	if (!gunActionsFile.exists()) {
		throw RuntimeException("spells file does not exist: ${gunActionsFile.canonicalPath}")
	}
	return gunActionsFile
}

fun getPerkListFile(config: AppConfig) : File {
	val perkListFile = File(File(config.noitaSaveFolder), "data/scripts/perks/perk_list.lua")
	if (!perkListFile.exists()) {
		throw RuntimeException("perks file does not exist: ${perkListFile.canonicalPath}")
	}
	return perkListFile
}

fun getTranslationsFile(config: AppConfig) : File {
	val transFile = File(File(config.noitaExeFile).parentFile, "data/translations/common.csv")
	if (!transFile.exists()) {
		throw RuntimeException("translations file does not exist: ${transFile.canonicalPath}")
	}
	return transFile
}

fun rotateImageByDegrees(img: BufferedImage, angle: Double): BufferedImage {
	val rads = Math.toRadians(angle)
	val sin = abs(sin(rads))
	val cos = abs(cos(rads))
	val w = img.width
	val h = img.height
	val newWidth = floor(w * cos + h * sin).toInt()
	val newHeight = floor(h * cos + w * sin).toInt()
	val rotated = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
	val g2d = rotated.createGraphics()
	val at = AffineTransform()
	at.translate(((newWidth - w) / 2).toDouble(), ((newHeight - h) / 2).toDouble())
	val x = w / 2
	val y = h / 2
	at.rotate(rads, x.toDouble(), y.toDouble())
	g2d.transform = at
	g2d.drawImage(img, 0, 0, null)
	//g2d.color = Color.RED
	//g2d.drawRect(0, 0, newWidth - 1, newHeight - 1)
	g2d.dispose()
	return rotated
}
