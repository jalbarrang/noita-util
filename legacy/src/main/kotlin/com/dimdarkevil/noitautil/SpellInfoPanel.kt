package com.dimdarkevil.noitautil

import com.dimdarkevil.noitautil.model.Spell
import com.dimdarkevil.swingutil.flowPanelWithLabel
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea

class SpellInfoPanel(val p: JPanel) {
	val idComp = JLabel()
	val nameComp = JLabel()
	val descriptionComp = JLabel()
	val english_nameComp = JLabel()
	val english_descComp = JLabel()
	val typeComp = JLabel()
	val spawn_levelComp = JLabel()
	val spawn_probabilityComp = JLabel()
	val priceComp = JLabel()
	val manaComp = JLabel()
	val max_usesComp = JLabel()
	val never_unlimitedComp = JLabel()
	val spawn_manual_unlockComp = JLabel()
	val recursiveComp = JLabel()
	val ai_never_usesComp = JLabel()
	val is_dangerous_blastComp = JLabel()
	val actionComp = JTextArea()
	val spriteComp = JLabel()
	val sprite_unidentifiedComp = JLabel()
	val related_projectilesComp = JLabel()
	val custom_xml_fileComp = JLabel()
	val spawn_requires_flagComp = JLabel()
	val sound_loop_tagComp = JLabel()
	val related_extra_entitiesComp = JLabel()
	val uiImageFilenameComp = JLabel()

	init {
		p.add(flowPanelWithLabel("id: ", idComp))
		p.add(flowPanelWithLabel("name: ", nameComp))
		p.add(flowPanelWithLabel("description: ", descriptionComp))
		p.add(flowPanelWithLabel("english_name: ", english_nameComp))
		p.add(flowPanelWithLabel("english_desc: ", english_descComp))
		p.add(flowPanelWithLabel("type: ", typeComp))
		p.add(flowPanelWithLabel("spawn_level: ", spawn_levelComp))
		p.add(flowPanelWithLabel("spawn_probability: ", spawn_probabilityComp))
		p.add(flowPanelWithLabel("price: ", priceComp))
		p.add(flowPanelWithLabel("mana: ", manaComp))
		p.add(flowPanelWithLabel("max_uses: ", max_usesComp))
		p.add(flowPanelWithLabel("never_unlimited: ", never_unlimitedComp))
		p.add(flowPanelWithLabel("spawn_manual_unlock: ", spawn_manual_unlockComp))
		p.add(flowPanelWithLabel("recursive: ", recursiveComp))
		p.add(flowPanelWithLabel("ai_never_uses: ", ai_never_usesComp))
		p.add(flowPanelWithLabel("is_dangerous_blast: ", is_dangerous_blastComp))
		p.add(flowPanelWithLabel("action: ", actionComp))
		p.add(flowPanelWithLabel("sprite: ", spriteComp))
		p.add(flowPanelWithLabel("sprite_unidentified: ", sprite_unidentifiedComp))
		p.add(flowPanelWithLabel("related_projectiles: ", related_projectilesComp))
		p.add(flowPanelWithLabel("custom_xml_file: ", custom_xml_fileComp))
		p.add(flowPanelWithLabel("spawn_requires_flag: ", spawn_requires_flagComp))
		p.add(flowPanelWithLabel("sound_loop_tag: ", sound_loop_tagComp))
		p.add(flowPanelWithLabel("related_extra_entities: ", related_extra_entitiesComp))
		p.add(flowPanelWithLabel("uiImageFilename: ", uiImageFilenameComp))
	}

	fun setFromVal(v: Spell) {
		idComp.text = "${v.id}"
		nameComp.text = "${v.name}"
		descriptionComp.text = "${v.description}"
		english_nameComp.text = "${v.english_name}"
		english_descComp.text = "${v.english_desc}"
		typeComp.text = "${v.type}"
		spawn_levelComp.text = "${v.spawn_level}"
		spawn_probabilityComp.text = "${v.spawn_probability}"
		priceComp.text = "${v.price}"
		manaComp.text = "${v.mana}"
		max_usesComp.text = "${v.max_uses}"
		never_unlimitedComp.text = "${v.never_unlimited}"
		spawn_manual_unlockComp.text = "${v.spawn_manual_unlock}"
		recursiveComp.text = "${v.recursive}"
		ai_never_usesComp.text = "${v.ai_never_uses}"
		is_dangerous_blastComp.text = "${v.is_dangerous_blast}"
		actionComp.text = v.action.joinToString("\n")
		spriteComp.text = "${v.sprite}"
		sprite_unidentifiedComp.text = "${v.sprite_unidentified}"
		related_projectilesComp.text = "${v.related_projectiles}"
		custom_xml_fileComp.text = "${v.custom_xml_file}"
		spawn_requires_flagComp.text = "${v.spawn_requires_flag}"
		sound_loop_tagComp.text = "${v.sound_loop_tag}"
		related_extra_entitiesComp.text = "${v.related_extra_entities}"
		uiImageFilenameComp.text = "${v.uiImageFilename}"
	}

}
