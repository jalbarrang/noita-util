package com.dimdarkevil.noitautil

import java.util.Properties

object Version : Versioner()

/**
 * Class that parses a version.properties file to easily get the version.
 *
 * Make an object that extends this, and use it's [version] property:
 *
 *     package com.example
 *     object Version: io.mfj.mfjkext.Version()
 */
open class Versioner {

	val properties:Properties by lazy {
		val cls = javaClass
		Properties().apply {
			ResourceLoader.load("version.properties")?.use { stream ->
				load(stream)
			}
		}
	}

	val version:String? by lazy {
		properties.getProperty("version")
	}

}