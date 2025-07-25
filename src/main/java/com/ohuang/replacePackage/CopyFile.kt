package com.ohuang.replacePackage

import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * 复制文件夹内所有文件
 * targetRootPath为拷贝到的目录
 */
fun copyPathAllFile(
    sourceRootPath: String,
    targetRootPath: String,
    path: String = "",
    isCover: Boolean = false, isLog: Boolean = false
) {
    val targetFile = File(targetRootPath + path)
    val sourceRootFile = File(sourceRootPath + path)
    if (!sourceRootFile.exists()){
        return
    }
    if (sourceRootFile.isFile) {
        realCopyFile(sourceRootPath, targetRootPath, isCover, isLog)
        return
    }
    if (!targetFile.exists()) {
        targetFile.mkdirs()
    }

    sourceRootFile.listFiles()?.forEach {
        if (it.isDirectory) {
            var newPath = path + "/${it.name}"
            copyPathAllFile(sourceRootPath, targetRootPath, newPath, isCover, isLog)
        } else {
            realCopyFile(it.absolutePath, targetRootPath + path + "/" + it.name, isCover, isLog)
        }
    }
}

fun copyFile(sourcePath: String, targetPath: String, isCover: Boolean = false, isLog: Boolean = false){
    copyPathAllFile(sourcePath, targetPath,"", isCover, isLog)
}


private fun realCopyFile(sourcePath: String, targetPath: String, isCover: Boolean = false, isLog: Boolean = false) {
    if (sourcePath==targetPath){
        return
    }
    if (isLog) {
      println("copyFile:$sourcePath->$targetPath")
    }

    val file = File(sourcePath)
    if (file.exists()) {
        val file1 = File(targetPath)
        if (!file1.exists() || isCover) {
            file.copyTo(file1, overwrite = true)
        }
    }
}

