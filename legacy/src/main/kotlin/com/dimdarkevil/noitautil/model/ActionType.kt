package com.dimdarkevil.noitautil.model

enum class ActionType(val bgFilename: String) {
	ACTION_TYPE_PROJECTILE("item_bg_projectile.png"),
	ACTION_TYPE_STATIC_PROJECTILE("item_bg_static_projectile.png"),
	ACTION_TYPE_MATERIAL("item_bg_material.png"),
	ACTION_TYPE_UTILITY("item_bg_utility.png"),
	ACTION_TYPE_DRAW_MANY("item_bg_draw_many.png"),
	ACTION_TYPE_MODIFIER("item_bg_modifier.png"),
	ACTION_TYPE_OTHER("item_bg_other.png"),
	ACTION_TYPE_PASSIVE("item_bg_passive.png"),
}
