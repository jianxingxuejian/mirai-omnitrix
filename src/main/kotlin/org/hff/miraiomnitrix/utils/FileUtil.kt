package org.hff.miraiomnitrix.utils

import org.hff.miraiomnitrix.exception.MyException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtil {

    fun getInputStream(path: String) = this::class.java.getResourceAsStream(path)

    fun getFileList(path: String): List<Path>? {
        val uri = this::class.java.getResource(path)?.toURI() ?: throw MyException("路径错误，未找到资源地址")
        return Files.list(Paths.get(uri)).toList()
    }

}