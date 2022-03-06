package com.dimdarkevil.noitautil

import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// credits: https://www.lightbourn.net/games/Noita/editor.html

object SalakieliUtil {

	data class SalakieliFile (
		val fileName: String,
		val keyStr: String,
		val ivStr: String,
	)

	val files = mapOf(
		"stats" to SalakieliFile("stats/_stats.salakieli", "536563726574734f66546865416c6c53", "54687265654579657341726557617463"),
		"magic_numbers" to SalakieliFile("magic_numbers.salakieli", "4b6e6f776c6564676549735468654869", "57686f576f756c646e74476976654576"),
		"session_numbers" to SalakieliFile("session_numbers.salakieli", "4b6e6f776c6564676549735468654869", "57686f576f756c646e74476976654576"),
		"internal_alchemy_list" to SalakieliFile("?", "31343439363631363932313933343032", "38313632343338393133393638333733"),
	)

	fun decryptAll(noitaSaveFolder: File) : Map<String,String> {
		return files.mapNotNull { (key, sf) ->
			try {
				key to decryptSalakieliFile(key, noitaSaveFolder)
			} catch (e: Exception) {
				println("${sf.fileName} error: ${e.message}")
				null
			}
		}.toMap()
	}

	fun decryptSalakieliFile(name: String, noitaSaveFolder: File) : String {
		val sf = files[name]!!
		val f = File(noitaSaveFolder, "/save00/${sf.fileName}")
		val content = f.readBytes()
		val keyVal = hexStrToByteArray(sf.keyStr)
		val ivVal = hexStrToByteArray(sf.ivStr)

		val key = SecretKeySpec(keyVal, "AES")
		val ivSpec = IvParameterSpec(ivVal)

		val cipher = Cipher.getInstance("AES/CTR/NoPadding")
		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
		val decrypted = cipher.doFinal(content)
		return String(decrypted, Charsets.UTF_8)
	}

	private fun hexStrToByteArray(s: String) : ByteArray {
		return (s.indices step 2).map { i ->
			s.substring((i..i+1)).toInt(16).toByte()
		}.toByteArray()
	}

}