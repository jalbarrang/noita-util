package com.dimdarkevil.noitautil.model

import java.awt.image.BufferedImage

data class Spell(
	var id: String = "",
	var name: String = "",
	var description: String = "",
	var english_name: String = "",
	var english_desc: String = "",
	var type: ActionType = ActionType.ACTION_TYPE_OTHER,
	var spawn_level: List<Int> = listOf(),
	var spawn_probability: List<Double> = listOf(),
	var price: Int = 0,
	var mana: Int = 0,
	var max_uses: Int = 0,
	var never_unlimited: Boolean = false,
	var spawn_manual_unlock: Boolean = false,
	var recursive: Boolean = false,
	var ai_never_uses: Boolean = false,
	var is_dangerous_blast: Boolean = false,
	var action: MutableList<String> = mutableListOf(),
	var sprite: String = "",
	var sprite_unidentified: String = "",
	var related_projectiles: String = "",
	var custom_xml_file: String = "",
	var spawn_requires_flag: String = "",
	var sound_loop_tag: String = "",
	var related_extra_entities: String = "",
	var image: BufferedImage? = null,
	var uiImageFilename: String = "",
	var uiImage: BufferedImage? = null,
)
