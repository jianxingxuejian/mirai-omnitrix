package org.hff.miraiomnitrix.utils

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

object ImageUtil {

    private val loader = ImmutableImage.loader()

    fun load(file: File): ImmutableImage = loader.fromFile(file)

    fun scaleTo(inputStream: InputStream, width: Int, height: Int): ImmutableImage =
        loader.fromStream(inputStream).scaleTo(width, height)

    fun scaleTo(image: ByteArray, width: Int, height: Int): ImmutableImage = loader.fromBytes(image).scaleTo(width, height)

    fun ImmutableImage.overlayToStream(image: ImmutableImage, x: Int, y: Int) =
        ByteArrayInputStream(this.overlay(image, x, y).bytes(PngWriter()))

    fun ImmutableImage.toStream() = ByteArrayInputStream(this.bytes(PngWriter()))

}

