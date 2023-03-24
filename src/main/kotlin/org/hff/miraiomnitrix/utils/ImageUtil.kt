package org.hff.miraiomnitrix.utils

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayInputStream
import java.io.InputStream

object ImageUtil {

    private val loader = ImmutableImage.loader()

    private fun load(bytes: ByteArray): ImmutableImage = loader.fromBytes(bytes)

    fun load(path: String) = load(ClassPathResource(path).inputStream.use { it.readBytes() })

    fun load(path: String, width: Int, height: Int): ImmutableImage = load(path).scaleTo(width, height)

    fun scaleTo(inputStream: InputStream, width: Int, height: Int): ImmutableImage =
        inputStream.use { loader.fromStream(it).scaleTo(width, height) }

    fun ImmutableImage.toStream() = this.toStream(PngWriter())

    fun ImmutableImage.toStream(write: ImageWriter) = ByteArrayInputStream(this.bytes(write))

    fun InputStream.toImmutableImage(width: Int, height: Int): ImmutableImage =
        use { loader.fromStream(this).scaleTo(width, height) }

    fun InputStream.toImmutableImage(): ImmutableImage = use { loader.fromStream(this) }

}

