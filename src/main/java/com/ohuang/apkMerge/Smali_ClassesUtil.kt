package com.ohuang.apkMerge

import com.google.gson.Gson
import com.ohuang.replacePackage.FileUtils
import com.ohuang.replacePackage.copyFile
import com.ohuang.replacePackage.moveFile
import com.ohuang.replacePackage.readTextLine

import java.io.File
import java.util.LinkedList


private const val MB_DIV_B = 1024 * 1024  // 字节和MB转换
private const val temp_smali = "/temp_smali"

/**
 * dex限制65535
 */
fun limitDex_smali_class_Dir(baseSmail: String) {
    var findSmaliClass = findSmaliClassesDirSort(baseSmail)
    if (findSmaliClass.isNotEmpty()) {
        var tempFileDir = File(baseSmail + temp_smali)
        findSmaliClass.forEach { path ->
            var file = File(path)
            moveFile(path, tempFileDir.absolutePath + "/" + file.name)
        }
        copySmali_Class_ForDexLimit(oldPath = tempFileDir.absolutePath, newPath = baseSmail)
        FileUtils.delete(tempFileDir)
    }
}

/**
 * 限制单个smali_Class的大小
 */
fun limitMaxSize_smali_class_Dir(baseSmail: String, maxMB: Long) {
    if (maxMB > 0 && maxMB < 1000) {
        var findSmaliClass = findSmaliClassesDirSort(baseSmail)
        if (findSmaliClass.isNotEmpty()) {
            var tempFileDir = File(baseSmail + temp_smali)
            findSmaliClass.forEach { path ->
                var file = File(path)
                moveFile(path, tempFileDir.absolutePath + "/" + file.name)
            }
            copySmali_Class_forSize(oldPath = tempFileDir.absolutePath, newPath = baseSmail, maxMB = maxMB)
            FileUtils.delete(tempFileDir)
        }

    }
}

/**
 * 删除空的smali_class目录
 */
fun deleteEmpty_smali_class_Dir(baseSmail: String) {
    var findSmaliClassesDirSort = findSmaliClassesDirSort(baseSmail)
    var newPath = LinkedList<String>()
    findSmaliClassesDirSort.forEach {
        if (hasFile(it)) {
            if (newPath.isNotEmpty()) {
                var removeAt = newPath.removeAt(0)
                moveFile(it, removeAt)
                newPath.add(it)
            }
        } else {
            newPath.add(it)
            FileUtils.delete(File(it))
        }

    }
}

private fun hasFile(path: String): Boolean {
    var hasFile = false
    forEachAllFile(File(path)) {
        hasFile = true
        true
    }
    return hasFile
}


/**
 *   '
 *   删除smali文件
 */
fun deleteSmaliPath(baseSmail: String, deletePath: List<String>) {
    if (deletePath.isNotEmpty()) {
        var findSmaliClass = findSmaliClassesDirSort(baseSmail)
        if (findSmaliClass.isNotEmpty()) {
            println("开始删除指定smali文件")
            findSmaliClass.forEach { classDir ->
                var file = File(classDir)
                deletePath.forEach { delete ->
                    var path = file.absolutePath + if (delete.startsWith("/")) {
                        delete
                    } else {
                        "/$delete"
                    }
                    var deleteFile = File(path)
                    if (deleteFile.exists()) {
                        FileUtils.delete(deleteFile)
                    }
                }
            }
        }
    }
}

/**
 *
 * 删除同名的smali
 *
 */
fun deleteSameNameSmali(baseSmail: String) {
    println("开始删除同名不会被加载的smali")
    var findSmaliClass = findSmaliClassesDirSort(baseSmail)
    var smaliPath = HashSet<String>()
    var deletePath = HashSet<String>()
    findSmaliClass.forEach { classDir ->
        var file = File(classDir)
        var startIndex = file.absolutePath.length
        forEachAllFile(file) {
            if (it.name.endsWith(".smali")) {
                var path = it.absolutePath.substring(startIndex)
                if (smaliPath.contains(path)) {
                    deletePath.add(it.absolutePath)
                } else {
                    smaliPath.add(path)
                }
            }
            false
        }
    }
    deletePath.forEach { t ->
        FileUtils.delete(File(t))
    }
}

private const val dexMethodLimitSize = 50000 //   dex限制65535
private const val dexFieldSize = 30000 //   dex限制65535
private const val dexClassSize = 10000 //   dex限制65535
class SmaliInfo {
    var methodCount: Int = 0
    var classCount: Int = 0
    var fieldCount: Int = 0
    var annotationCount: Int = 0

    fun isExceedsLimit(smaliInfo: SmaliInfo): Boolean {
        if (methodCount + smaliInfo.methodCount > dexMethodLimitSize) {
            return true
        }
        if (classCount + smaliInfo.classCount > dexClassSize) {
            return true
        }
        if (fieldCount + smaliInfo.fieldCount > dexFieldSize) {
            return true
        }
        if (annotationCount + smaliInfo.annotationCount > dexFieldSize) {
            return true
        }
        return false
    }

    fun add(smaliInfo: SmaliInfo) {
        methodCount += smaliInfo.methodCount
        classCount += smaliInfo.classCount
        fieldCount += smaliInfo.fieldCount
        annotationCount += smaliInfo.annotationCount
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}


fun printAllSmaliFileInfo(path: String) {
    var findSmaliClass = findSmaliClassesDirSort(path)
    if (findSmaliClass.isEmpty()) {
        var file = File(path)
        var smaliInfo = SmaliInfo()
        forEachAllFile(file) {
            var mysmaliInfo = getSmaliFileInfo(it.absolutePath)
            smaliInfo.add(mysmaliInfo)
            false
        }
        println("smali_class:$path smaliInfo:$smaliInfo")
    } else {
        findSmaliClass.forEach { classDir ->
            var file = File(classDir)
            var smaliInfo = SmaliInfo()
            forEachAllFile(file) {
                var mysmaliInfo = getSmaliFileInfo(it.absolutePath)
                smaliInfo.add(mysmaliInfo)
                false
            }
            println("smali_class:$classDir smaliInfo:$smaliInfo")
        }
    }
}

private fun getSmaliFileInfo(path: String): SmaliInfo {
    var smaliInfo = SmaliInfo()
    readTextLine(path) { string ->
        if (string.startsWith(".method")) {
            smaliInfo.methodCount++
        } else if (string.startsWith(".class")) {
            smaliInfo.classCount++
        } else if (string.startsWith(".field")) {
            smaliInfo.fieldCount++
        } else if (string.startsWith(".annotation")) {
            smaliInfo.annotationCount++
        }
        string
    }
    return smaliInfo
}


private fun copySmali_Class_ForDexLimit(oldPath: String, newPath: String) {
    var findSmaliClass = findSmaliClassesDirSort(oldPath)
    var class_count = 1
    var smaliInfo = SmaliInfo()
    println("限制smali_classes field和method数量")
    findSmaliClass.forEach { oldSmailDir ->
        var oldFile = File(oldSmailDir)
        println("开始复制:" + oldFile.absolutePath + " -> " + getSmailDirForNum(newPath, num = class_count))
        forEachAllFile(oldFile) { smailFile ->
            var myInfo = getSmaliFileInfo(smailFile.absolutePath)
            if (smaliInfo.isExceedsLimit(myInfo)) {
                println("smali_${class_count}:$smaliInfo")
                class_count++
                println("开始复制:" + oldFile.absolutePath + " -> " + getSmailDirForNum(newPath, num = class_count))
                smaliInfo = SmaliInfo() //重新赋值
            }
            smaliInfo.add(myInfo)
            var substring = smailFile.absolutePath.substring(oldFile.absolutePath.length)
            var rootPath = getSmailDirForNum(newPath, num = class_count)
            copyFile(smailFile.absolutePath, rootPath + substring, isCover = true)
            false
        }
        println("smali_${class_count}:$smaliInfo")
        class_count++
        smaliInfo = SmaliInfo()
    }
}

private fun copySmali_Class_forSize(oldPath: String, newPath: String, maxMB: Long) {
    var smailMaxMB = 0L
    if (maxMB <= 0) {
        smailMaxMB = 30
    } else {
        smailMaxMB = maxMB
    }
    println("限制smali_classes大小 $smailMaxMB MB")
    var findSmaliClass = findSmaliClassesDirSort(oldPath)
    var class_index = 1
    var maxFileSize = smailMaxMB * MB_DIV_B
    var totalSize: Long = 0
    findSmaliClass.forEach { oldSmailDir ->
        var oldFile = File(oldSmailDir)
        println("开始复制:" + oldFile.absolutePath + " -> " + getSmailDirForNum(newPath, num = class_index))
        forEachAllFile(oldFile) { smailFile ->
            var size = smailFile.length()
            if (totalSize + size > maxFileSize) {
                class_index++
                println("开始复制:" + oldFile.absolutePath + " -> " + getSmailDirForNum(newPath, num = class_index))
                totalSize = 0 //重新赋值
            }
            totalSize += size
            var substring = smailFile.absolutePath.substring(oldFile.absolutePath.length)
            var rootPath = getSmailDirForNum(newPath, num = class_index)
            copyFile(smailFile.absolutePath, rootPath + substring, isCover = true)
            false
        }
        class_index++
        totalSize = 0
    }
}

fun getSmailDirForIndex(path: String, index: Int): String {
    return getSmailDirForNum(path, index)
}

fun getNewSmailDir(path: String): String {
    var dirs = findSmaliClassesDirSort(path)
    return getSmailDirForNum(path, dirs.size + 1)
}

fun getSmailDirForNum(path: String, num: Int): String {
    if (num == 1) {
        return "$path/smali"
    } else {
        return "$path/smali_classes$num"
    }
}

/**
 * 获取smali_class目录  按照顺序获取
 */
fun findSmaliClassesDirSort(path: String): List<String> {
    val smailDirFilePaths = ArrayList<String>()
    var num = 1
    var cacheFile = File(getSmailDirForNum(path, num))
    while (cacheFile.exists() && cacheFile.isDirectory) {
        smailDirFilePaths.add(cacheFile.absolutePath)
        num++
        cacheFile = File(getSmailDirForNum(path, num))
    }
    return smailDirFilePaths
}

fun findDexFile(path: String): List<String> {
    val dexFilePaths = ArrayList<String>()
    var file = File(path)
    if (file.exists()) {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                if (it.isFile && it.name.endsWith(".dex")) {
                    dexFilePaths.add(it.absolutePath)
                }
            }
        } else {
            if (file.name.endsWith(".dex")) {
                dexFilePaths.add(file.absolutePath)
            }
        }

    }
    return dexFilePaths
}