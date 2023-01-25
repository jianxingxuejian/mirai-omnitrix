package org.hff.miraiomnitrix.utils

import com.google.common.cache.CacheBuilder
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

object ImageUtil {
    private val loader = ImmutableImage.loader()

    val imageCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Int, String>()

    fun scaleTo(file: File, width: Int, height: Int): ImmutableImage = loader.fromFile(file).scaleTo(width, height)

    fun scaleTo(inputStream: InputStream, width: Int, height: Int): ImmutableImage =
        loader.fromStream(inputStream).scaleTo(width, height)

    fun overlay(imageA: ImmutableImage, imageB: ImmutableImage, x: Int, y: Int) =
        ByteArrayInputStream(imageA.overlay(imageB, x, y).bytes(PngWriter()))

    fun getFormCache(message: MessageChain): Image? {
        val quote = message[QuoteReply.Key]
        return if (quote != null) {
            val imageId = imageCache.getIfPresent(quote.source.internalIds[0])
            if (imageId == null) null
            else Image(imageId)
        } else message[Image.Key]
    }
}
