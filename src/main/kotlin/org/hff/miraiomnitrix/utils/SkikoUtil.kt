package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.utils.AbstractExternalResource
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.sha1
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import java.io.InputStream
import java.util.*

object SkikoUtil {

    private val provider: TypefaceFontProvider = TypefaceFontProvider()
    private val fonts = listOfNotNull(provider, FontMgr.default) +
            ServiceLoader.load(FontMgr::class.java) +
            ServiceLoader.load(TypefaceFontProvider::class.java)

    fun getFont(family: String, style: FontStyle, size: Float) = Font(matchFamilyStyle(family, style), size)

    private fun matchFamilyStyle(familyName: String, style: FontStyle) =
        fonts.firstNotNullOfOrNull { it.matchFamily(familyName).matchStyle(style) }
}

enum class FontFamily(val value: String) {
    Tahoma("Tahoma"),
    NotoSansSC("Noto Sans SC"),
    NotoSerifSC("Noto Serif SC"),
}

class SkiaExternalResource(override val origin: Data, override val formatName: String) :
    ExternalResource, AbstractExternalResource({ origin.close() }) {
    constructor(image: Image, format: EncodedImageFormat) : this(
        origin = requireNotNull(image.encodeToData(format)) { "encode $format result null." },
        formatName = format.name.replace("JPEG", "JPG")
    )

    override val md5: ByteArray by lazy { origin.bytes.md5() }
    override val sha1: ByteArray by lazy { origin.bytes.sha1() }
    override val size: Long get() = origin.size.toLong()
    override fun inputStream0(): InputStream = origin.bytes.inputStream()
}
