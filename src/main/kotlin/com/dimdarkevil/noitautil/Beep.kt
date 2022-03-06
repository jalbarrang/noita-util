package com.dimdarkevil.noitautil

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class Beep {
	val clipData = javaClass.getResourceAsStream("/beep.wav")?.use { sin ->
		sin.readAllBytes()
	}!!
	val clip = AudioSystem.getClip()

	fun play() {
		val frameSize = clipData.size / 4
		clip.open(AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 176400f, false), clipData, 0, clipData.size)
		clip.start()
		while (clip.framePosition < frameSize) {
			Thread.sleep(100)
		}
		clip.close()
	}
}