package com.dimdarkevil.swingutil

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon

object IconLoader {
	fun svgToPng(svgStream: InputStream, pngStream: OutputStream, width: Int, height: Int) {
		val input = TranscoderInput(svgStream)
		val output = TranscoderOutput(pngStream)
		val cnv = PNGTranscoder()
		cnv.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width.toFloat())
		cnv.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height.toFloat())
		cnv.transcode(input, output)
	}

	fun svgToImage(svgStream: InputStream, width: Int, height: Int) : BufferedImage {
		ByteArrayOutputStream().use { pngStream ->
			svgToPng(svgStream, pngStream, width, height)
			return ImageIO.read(ByteArrayInputStream(pngStream.toByteArray()))
		}
	}

	fun img(icon: Ico, size: Int) =
		svgToImage(javaClass.getResourceAsStream("/icons/${icon.fname}"), size, size)

	fun imgIcon(icon: Ico, size: Int) =
		ImageIcon(img(icon, size))

}