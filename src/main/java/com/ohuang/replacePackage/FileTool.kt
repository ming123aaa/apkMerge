package com.ohuang.replacePackage

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


fun readTextLine(path: String, call: (String) -> String): String {
    val file = File(path)
    if (file.exists() && file.isFile) {
        val stringBuilder = StringBuilder()
        val bufferedReader = BufferedReader(FileReader(File(path)))
        bufferedReader.lines().forEach {
            stringBuilder.append(call(it)).append("\n")
        }
        if (stringBuilder.isNotEmpty()){
            stringBuilder.deleteCharAt(stringBuilder.length-1)
        }
        bufferedReader.close()
        return stringBuilder.toString()
    }
    return ""
}

fun changTextLine(path: String, call: (String) -> String) {
    val file = File(path)
    if (file.exists() && file.isFile) {
        val stringBuilder = StringBuilder()
        val bufferedReader = BufferedReader(FileReader(File(path)))
        bufferedReader.lines().forEach {
            stringBuilder.append(call(it)).append("\n")
        }
        if (stringBuilder.isNotEmpty()){
            stringBuilder.deleteCharAt(stringBuilder.length-1)
        }
        bufferedReader.close()
        FileUtils.writeText(File(path), stringBuilder.toString())
    }
}

fun moveFile(oldPath: String, newPath: String) {
    try {
        val old = Paths.get(oldPath).toAbsolutePath().normalize()
        val new = Paths.get(newPath).toAbsolutePath().normalize()

        // 检查源文件是否存在
        if (!Files.exists(old)) {
            return
        }

        // 确保目标目录存在
        Files.createDirectories(new.parent)

        // 尝试移动文件（支持跨磁盘）
        if (old.fileSystem == new.fileSystem) {
            // 同一文件系统，直接移动
            Files.move(old, new, StandardCopyOption.REPLACE_EXISTING)
        } else {
            // 跨文件系统，先复制再删除
            Files.copy(old, new, StandardCopyOption.REPLACE_EXISTING)
            Files.delete(old)
        }

    } catch (e: AccessDeniedException) {
        println("权限不足: ${e.message}")
    } catch (e: FileAlreadyExistsException) {
        println("目标文件已存在且无法替换: ${e.message}")
    } catch (e: IOException) {
        println("IO 错误: ${e.message}")
    }
}

